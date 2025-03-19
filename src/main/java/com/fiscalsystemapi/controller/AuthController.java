package com.fiscalsystemapi.controller;

import com.fiscalsystemapi.dto.auth.AuthRequest;
import com.fiscalsystemapi.dto.auth.AuthResponse;
import com.fiscalsystemapi.entity.User;
import com.fiscalsystemapi.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para registro de novo usuário.
     * Recebe um JSON representando um usuário e retorna o usuário salvo (com senha oculta).
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User savedUser = authService.register(user);
        savedUser.setSenha(null);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    /**
     * Endpoint para autenticação.
     * Recebe um JSON com email e senha, e retorna um token JWT em caso de sucesso.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        String token = authService.login(authRequest.getEmail(), authRequest.getSenha());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
