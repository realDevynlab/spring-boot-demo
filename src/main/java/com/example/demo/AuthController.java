package com.example.demo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("sign-in")
    public ResponseEntity<SignInResponseDTO> signIn(@Valid @RequestBody SignInDTO signInDTO) {
        SignInResponseDTO response = authService.signIn(signInDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
