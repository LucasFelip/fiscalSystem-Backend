package com.fiscalsystemapi.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String senha;
}
