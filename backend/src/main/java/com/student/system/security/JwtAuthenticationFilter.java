package com.student.system.security;

import com.student.system.util.JwtUtil;
import com.student.system.util.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从请求头中获取Token，验证后设置到SecurityContext
 *
 * @author Student System
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${app.jwt.header}")
    private String header;

    @Value("${app.jwt.token-prefix}")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头获取Token
        String token = getTokenFromRequest(request);

        // 2. 如果Token存在且SecurityContext中没有认证信息
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 3. 从Token中获取用户名
                String username = jwtUtil.getUsernameFromToken(token);

                if (username != null) {
                    // 4. 验证Token是否在Redis中存在（关键：从Redis验证）
                    if (redisUtil.validateToken(username, token)) {
                        // 5. 加载用户详情（包含角色和权限）
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // 6. 验证Token有效性
                        if (jwtUtil.validateToken(token, username)) {
                            // 7. 创建认证对象
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // 8. 设置到SecurityContext
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            // 9. 刷新Redis中Token的过期时间（可选）
                            redisUtil.refreshTokenExpire(username);

                            log.debug("用户 {} 认证成功", username);
                        }
                    } else {
                        log.warn("Token在Redis中不存在或已失效: {}", username);
                    }
                }
            } catch (Exception e) {
                log.error("JWT认证失败: {}", e.getMessage());
            }
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(header);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return null;
    }

}
