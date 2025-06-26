package hexlet.code.controller;

import hexlet.code.dto.AuthRequest;
import hexlet.code.exception.InvalidCredentialsException;
import hexlet.code.utils.JWTUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/login")
public class AuthenticationController {

    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(JWTUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    public String login(@RequestBody AuthRequest authRequest) {
        var token = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()
        );

        try {
            authenticationManager.authenticate(token);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Неверное имя пользователя или пароль");
        }

        return jwtUtils.generateToken(authRequest.getUsername());
    }

}
