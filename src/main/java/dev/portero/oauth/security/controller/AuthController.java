package dev.portero.oauth.security.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    @RequestMapping("")
    public ResponseEntity<?> getAuth(@RequestParam String token) {
        return ResponseEntity.ok(token);
    }
}
