package br.com.forjacode.taskmanager.adapters.output.security;

import br.com.forjacode.taskmanager.application.ports.output.GoogleTokenVerifierPort;
import br.com.forjacode.taskmanager.application.ports.output.GoogleUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class GoogleTokenVerifierAdapter implements GoogleTokenVerifierPort {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierAdapter(GoogleOAuthProperties properties) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(List.of(properties.clientId()))
                .build();
    }

    @Override
    public Optional<GoogleUserInfo> verify(String idToken) {
        log.info("Verifying Google ID token");
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                log.warn("Google ID token verification failed: token is invalid");
                return Optional.empty();
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String name = (String) payload.get("name");

            log.info("Google ID token verified successfully for subject {}", payload.getSubject());

            return Optional.of(new GoogleUserInfo(payload.getEmail(), name, payload.getSubject()));

        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            log.error("Google ID token verification failed", e);
            return Optional.empty();
        }
    }
}