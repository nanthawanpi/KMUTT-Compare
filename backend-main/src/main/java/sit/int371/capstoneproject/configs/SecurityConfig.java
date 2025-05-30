package sit.int371.capstoneproject.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sit.int371.capstoneproject.exceptions.CustomAccessDeniedHandler;
import sit.int371.capstoneproject.exceptions.CustomAuthEntryPoint;

@Configuration
@EnableMethodSecurity // ทำให้ @PreAuthorize ทำงานใน controller ที่เราไปกำหนด check role เอาไว้
public class SecurityConfig {
    @Autowired
    private JwtUtil jwtUtil;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) //ใช้ Lambda DSL
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**","/api/images/**").permitAll() //อนุญาตให้เข้าถึง URL /api/auth/**
                        .requestMatchers("/api/auth/logout").authenticated()          // logout เมื่อ user อยู่ในระบบ
                        // กำหนดสิทธิ์ API ตาม Role
                        .requestMatchers("/api/admin/**").hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/api/dormitories", "/api/dormitories/{id}","/api/user/dormitories", "/api/user/dormitories/{id}").permitAll()
                        .requestMatchers("/api/user/**").hasAnyRole("admin", "user") // ADMIN และ USER ใช้งานได้
                        .anyRequest().authenticated() //ทุกคำขออื่น ๆ ต้องการการยืนยันตัวตน
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class); // filter ก่อน UsernamePasswordAuthenticationFilter
        return http.build();
    }
}
