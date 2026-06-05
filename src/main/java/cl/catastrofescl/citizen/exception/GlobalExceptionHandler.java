package cl.catastrofescl.citizen.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Error de validación en los datos enviados"
        );
        problem.setTitle("Solicitud inválida");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("instance", request.getRequestURI());
        problem.setProperty("errorCode", "VALIDATION_ERROR");

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(DonationNotFoundException.class)
    public ProblemDetail handleDonationNotFound(DonationNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, "Donación no encontrada", ex.getMessage(),
                "DONATION_NOT_FOUND", request);
    }

    @ExceptionHandler(DonationAlreadyConfirmedException.class)
    public ProblemDetail handleDonationAlreadyConfirmed(DonationAlreadyConfirmedException ex,
                                                          HttpServletRequest request) {
        return buildProblem(HttpStatus.CONFLICT, "Donación no puede confirmarse", ex.getMessage(),
                "DONATION_ALREADY_CONFIRMED", request);
    }

    @ExceptionHandler(DuplicateNeedException.class)
    public ProblemDetail handleDuplicateNeed(DuplicateNeedException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.CONFLICT, "Necesidad duplicada", ex.getMessage(),
                "DUPLICATE_NEED", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tiene permisos para realizar esta operación", "ACCESS_DENIED", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage(),
                "UNAUTHORIZED", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                ex.getMessage(), "INTERNAL_ERROR", request);
    }

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail,
                                       String errorCode, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setProperty("instance", request.getRequestURI());
        problem.setProperty("errorCode", errorCode);
        return problem;
    }
}
