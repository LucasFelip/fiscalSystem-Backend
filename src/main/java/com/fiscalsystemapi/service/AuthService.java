package com.fiscalsystemapi.service;

import com.fiscalsystemapi.entity.User;
import com.fiscalsystemapi.exception.ApiException;
import com.fiscalsystemapi.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
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
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("nomeCompleto", user.getNomeCompleto())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Retorna o usuário atualmente logado, com base no SecurityContext.
     *
     * @return Usuário logado.
     * @throws ApiException Se não houver usuário autenticado ou se o ID for inválido.
     */
    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("Usuário não autenticado!");
        }
        String userIdStr = authentication.getPrincipal().toString();
        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            throw new ApiException("Usuário autenticado com ID inválido!");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Usuário não encontrado!"));
    }
}
