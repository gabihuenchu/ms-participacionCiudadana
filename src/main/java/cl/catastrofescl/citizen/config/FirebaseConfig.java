package cl.catastrofescl.citizen.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials-path}")
    private Resource credentialsResource;

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            if (!credentialsResource.exists()) {
                throw new IllegalStateException(
                        "firebase.enabled=true pero no se encontró el archivo de credenciales: "
                                + credentialsResource
                );
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsResource.getInputStream()))
                    .setProjectId(projectId)
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK inicializado para ms-citizen (proyecto: {})", projectId);
        }
    }
}
