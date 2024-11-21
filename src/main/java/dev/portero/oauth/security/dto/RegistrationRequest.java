package dev.portero.oauth.security.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class RegistrationRequest {

    private String username;
    private String email;
    private String password;
}
