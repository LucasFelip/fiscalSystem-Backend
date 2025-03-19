package com.fiscalsystemapi.service;

import com.fiscalsystemapi.entity.User;
import com.fiscalsystemapi.exception.ApiException;
import com.fiscalsystemapi.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String jwtSecret;
    private final long jwtExpirationMs;

    public AuthService(UserRepository userRepository,
                       @Value("${jwt.secret}") String jwtSecret,
                       @Value("${jwt.expirationMs:3600000}") long jwtExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Registra um novo usuário após validar a existência de e-mail e CPF.
     *
     * @param user Objeto User contendo os dados do usuário.
     * @return O usuário salvo.
     * @throws ApiException Caso o e-mail ou CPF já estejam cadastrados.
     */
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ApiException("E-mail já cadastrado!");
        }
        if (userRepository.findByCpf(user.getCpf()).isPresent()) {
            throw new ApiException("CPF já cadastrado!");
        }
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        return userRepository.save(user);
    }

    /**
     * Autentica o usuário e gera um token JWT caso as credenciais sejam válidas.
     *
     * @param email       E-mail do usuário.
     * @param rawPassword Senha em texto plano.
     * @return Token JWT gerado.
     * @throws ApiException Caso o usuário não seja encontrado ou a senha esteja incorreta.
     */
    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Usuário não encontrado!"));
        if (!passwordEncoder.matches(rawPassword, user.getSenha())) {
            throw new ApiException("Senha incorreta!");
        }
        return generateJwtToken(user);
    }

    /**
     * Gera o token JWT utilizando o ID do usuário, e informações adicionais.
     *
     * @param user Usuário autenticado.
     * @return Token JWT.
     */
    private String generateJwtToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("nomeCompleto", user.getNomeCompleto())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}
