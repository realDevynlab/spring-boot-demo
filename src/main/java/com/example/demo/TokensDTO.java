package com.example.demo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokensDTO {

    private String accessToken;
    private String refreshToken;

    public TokensDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
