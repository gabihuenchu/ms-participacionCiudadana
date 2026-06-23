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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_URI = "https://catastrofescl.cl/errors/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Error de validación en los datos enviados"
        );
        enrichProblem(problem, "validation-error", "Solicitud inválida", "VALIDATION_ERROR", request);

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(DonationNotFoundException.class)
    public ProblemDetail handleDonationNotFound(DonationNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, "donation-not-found", "Donación no encontrada",
                ex.getMessage(), "DONATION_NOT_FOUND", request);
    }

    @ExceptionHandler(DonationQuantityExceededException.class)
    public ProblemDetail handleDonationQuantityExceeded(DonationQuantityExceededException ex,
                                                          HttpServletRequest request) {
        return buildProblem(HttpStatus.BAD_REQUEST, "donation-quantity-exceeded",
                "Cantidad de donación excedida", ex.getMessage(), "DONATION_QUANTITY_EXCEEDED", request);
    }

    @ExceptionHandler(ItemNotNeededException.class)
    public ProblemDetail handleItemNotNeeded(ItemNotNeededException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.BAD_REQUEST, "item-not-needed",
                "Ítem no requerido", ex.getMessage(), "ITEM_NOT_NEEDED", request);
    }

    @ExceptionHandler(DonationAlreadyConfirmedException.class)
    public ProblemDetail handleDonationAlreadyConfirmed(DonationAlreadyConfirmedException ex,
                                                          HttpServletRequest request) {
        return buildProblem(HttpStatus.CONFLICT, "donation-already-confirmed",
                "Donación no puede confirmarse", ex.getMessage(), "DONATION_ALREADY_CONFIRMED", request);
    }

    @ExceptionHandler(NeedNotFoundException.class)
    public ProblemDetail handleNeedNotFound(NeedNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, "need-not-found", "Necesidad no encontrada",
                ex.getMessage(), "NEED_NOT_FOUND", request);
    }

    @ExceptionHandler(DuplicateNeedException.class)
    public ProblemDetail handleDuplicateNeed(DuplicateNeedException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.CONFLICT, "duplicate-need", "Necesidad duplicada",
                ex.getMessage(), "DUPLICATE_NEED", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.FORBIDDEN, "access-denied", "Acceso denegado",
                "No tiene permisos para realizar esta operación", "ACCESS_DENIED", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "unauthorized", "No autenticado",
                ex.getMessage(), "UNAUTHORIZED", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Error interno",
                ex.getMessage(), "INTERNAL_ERROR", request);
    }

    private ProblemDetail buildProblem(HttpStatus status, String typeSlug, String title, String detail,
                                       String errorCode, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        enrichProblem(problem, typeSlug, title, errorCode, request);
        return problem;
    }

    private void enrichProblem(ProblemDetail problem, String typeSlug, String title,
                               String errorCode, HttpServletRequest request) {
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_BASE_URI + typeSlug));
        problem.setProperty("instance", request.getRequestURI());
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("timestamp", Instant.now().toString());
    }
}
