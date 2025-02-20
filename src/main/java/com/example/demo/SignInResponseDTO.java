package com.example.demo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInResponseDTO {

    private UserDetailsDTO user;
    private TokensDTO tokens;

    public SignInResponseDTO(UserDetailsDTO user, TokensDTO tokens) {
        this.user = user;
        this.tokens = tokens;
    }

}
