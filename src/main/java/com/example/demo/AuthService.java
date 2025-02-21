package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public SignInResponseDTO signIn(SignInDTO signInDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInDTO.getUsernameOrEmail(), signInDTO.getPassword())
        );
        CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.getAccessToken(authentication);
        String refreshToken = jwtService.getRefreshToken(authentication);
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(
                authenticatedUser.getId(),
                authenticatedUser.getUsername(),
                authenticatedUser.getEmail()
        );
        TokensDTO tokensDTO = new TokensDTO(accessToken, refreshToken);
        return new SignInResponseDTO(userDetailsDTO, tokensDTO);
    }

}
