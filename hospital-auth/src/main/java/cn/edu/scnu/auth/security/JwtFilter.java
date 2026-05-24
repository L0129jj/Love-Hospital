package cn.edu.scnu.auth.security;

import io.jsonwebtoken.Claims;
import cn.edu.scnu.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final String jwtSecret;
    private final StringRedisTemplate redisTemplate;

    public JwtFilter(@Value("${jwt.secret}") String jwtSecret,
                     StringRedisTemplate redisTemplate) {
        this.jwtSecret = jwtSecret;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtil.parseClaims(token, jwtSecret);

            // 检查黑名单
            String blacklistKey = "token:blacklist:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 已注销");
                return;
            }

            // 检查是否过期（jjwt 已自动校验，这里双重保险）
            if (claims.getExpiration().before(new Date())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 已过期");
                return;
            }

            String role = claims.get("role", String.class);
            Long userId = claims.get("userId", Long.class);
            String subject = claims.getSubject();

            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);
            auth.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 无效或已过期");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
