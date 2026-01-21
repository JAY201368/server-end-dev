package com.student.system.config;

import com.student.system.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 *
 * @author Student System
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (因为使用 JWT，不需要 CSRF 保护)
                .csrf(AbstractHttpConfigurer::disable)

                // 禁用 CORS (后续可以单独配置)
                .cors(AbstractHttpConfigurer::disable)

                // 配置会话管理为无状态 (使用 JWT，不需要 Session)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 放行健康检查端点
                        .requestMatchers("/health/**").permitAll()

                        // 放行 Actuator 端点
                        .requestMatchers("/actuator/**").permitAll()

                        // 放行 Swagger 文档 (如果后续添加)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/doc.html").permitAll()

                        // 放行静态资源
                        .requestMatchers("/static/**", "/public/**").permitAll()

                        // 放行登录注册接口
                        .requestMatchers("/auth/login", "/auth/register").permitAll()

                        // 放行测试接口（仅用于调试）
                        .requestMatchers("/test/password").permitAll()

                        // 放行学生和成绩接口（开发阶段临时放行，生产环境应删除）
                        .requestMatchers("/student/**", "/score/**").permitAll()

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated())

                // 添加 JWT 认证过滤器（关键：在用户名密码认证过滤器之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
