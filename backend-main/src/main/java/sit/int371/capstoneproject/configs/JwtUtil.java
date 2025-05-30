package sit.int371.capstoneproject.configs;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.int371.capstoneproject.entities.UserRoleEnum;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;


@Component
public class JwtUtil {

    @Value("${jwt.secret}") // ดึง secretKey จาก application.properties
    private String secretKey;

    @Value("${jwt.expiration}") // กำหนดอายุของ Access Token (เช่น 24 ชั่วโมง)
    private long expirationTime;

    @Value("${jwt.refreshToken}")
    private long refreshExpirationTime; // เวลา Expiration ของ Refresh Token

    //สร้าง Token
    public String generateToken(String username, UserRoleEnum role, int userId, boolean isRefreshToken) {
        long tokenExpirationTime = isRefreshToken ? refreshExpirationTime : expirationTime;

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.name()) // เพิ่ม role ลงใน Claims
                .claim("userId", userId)
                .setIssuedAt(new Date()) // เวลาที่ออก Token
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpirationTime)) // เวลาหมดอายุ
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8)) // ลงนามด้วย secretKey
                .compact();
    }

    //Refresh Token
    // สร้าง Access Token อันใหม่หลังทำ Refresh token
    public String generateAccessToken(String username, UserRoleEnum role, int userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.name())
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    // ดึง username จาก Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ตรวจสอบว่า Token หมดอายุหรือไม่
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ตรวจสอบว่า Token ถูกต้องหรือไม่ ควรเช็ค username
    public boolean validateToken(String token, String username) {
        // ตรวจสอบ token กับ username
        String extractedUsername = extractUsername(token);
        return (extractUsername(token).equals(username) && !isTokenExpired(token));
    }

    // ตรวจสอบว่า Refresh Token ถูกต้องหรือไม่
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token);
            return true; // ถ้าไม่มี Exception แสดงว่า Token ถูกต้อง
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Token ไม่ถูกต้อง หรือหมดอายุ
        }
    }

    // ดึงวันหมดอายุจาก Token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ดึงข้อมูล Claims จาก Token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
    // ดึง role จาก Token
    public UserRoleEnum extractRole(String token) {
        return UserRoleEnum.valueOf(extractClaim(token, claims -> claims.get("role", String.class)));
    }
    // ดึง userId จาก Token
    public int extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }
}
