package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("sign-in")
    public ResponseEntity<SignInResponseDTO> signIn(@Valid @RequestBody SignInDTO signInDTO) {
        SignInResponseDTO response = authService.signIn(signInDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("refresh")
    public ResponseEntity<TokensDTO> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        TokensDTO tokensDTO = authService.refreshToken(request, response);
        if (tokensDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity.ok(tokensDTO);
    }

}
