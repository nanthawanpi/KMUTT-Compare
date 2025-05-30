package sit.int371.capstoneproject.configs;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.int371.capstoneproject.entities.UserRoleEnum;

import java.io.IOException;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;

    // เพิ่ม constructor ที่รับ JwtUtil
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (token != null && jwtUtil.validateToken(token, jwtUtil.extractUsername(token))) {
            String username = jwtUtil.extractUsername(token);
            UserRoleEnum role = jwtUtil.extractRole(token); // ดึง Role จาก Token
            int userId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Integer.class)); // ดึง userId จาก Token

            // สร้าง GrantedAuthority จาก Role
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // ดึง token จาก Authorization header
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // เอาเฉพาะ token หลังจาก Bearer
        }
        return null;
    }
}
