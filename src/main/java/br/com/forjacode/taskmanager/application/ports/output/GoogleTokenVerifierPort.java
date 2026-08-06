package br.com.forjacode.taskmanager.application.ports.output;

import java.util.Optional;

public interface GoogleTokenVerifierPort {

    Optional<GoogleUserInfo> verify(String idToken);
}
