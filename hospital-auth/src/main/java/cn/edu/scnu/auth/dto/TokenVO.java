package cn.edu.scnu.auth.dto;

import lombok.Data;

@Data
public class TokenVO {
    private String token;
    private String role;
    private Long userId;
    private long expiresAt;
    private String name;
}
