package sit.int371.capstoneproject.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int371.capstoneproject.autoId.SequenceGenerateUserService;
import sit.int371.capstoneproject.configs.JwtUtil;
import sit.int371.capstoneproject.dtos.LoginRequest;
import sit.int371.capstoneproject.dtos.RegisterRequest;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.entities.UserRoleEnum;
import sit.int371.capstoneproject.exceptions.BadRequestException;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.UserRepository;
import sit.int371.capstoneproject.services.AuthenticationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173","http://cp24kk2.sit.kmutt.ac.th","https://kmutt-compare.sit.kmutt.ac.th"})
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SequenceGenerateUserService sequenceGenerateUserService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // ตรวจสอบว่า username หรือ email ซ้ำหรือไม่
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        //generate user id
        registerRequest.setUserId((int) sequenceGenerateUserService.generateSequence(User.SEQUENCE_NAME));
        return authenticationService.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, String> tokens = authenticationService.login(loginRequest.getUsername(), loginRequest.getPassword());
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // เปลี่ยนสถานะของผู้ใช้ให้เป็น active
        user.setActive(true);
        userRepository.save(user);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh Token is required");
        }

        // ตรวจสอบว่า Refresh Token ถูกต้องไหม
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
        }

        // ดึง username จาก Refresh Token
        String username = jwtUtil.extractUsername(refreshToken);
        UserRoleEnum role = jwtUtil.extractRole(refreshToken);
        int userId = jwtUtil.extractUserId(refreshToken);

        // สร้าง Access Token ใหม่
        String newAccessToken = jwtUtil.generateAccessToken(username, role, userId);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Active user(s) แสดงทั้งจำนวน และชื่อผู้ใช้
    @GetMapping("/active-users")
    public ResponseEntity<Map<String, Object>> getActiveUsers() {
        List<User> activeUsers = userRepository.findByIsActive(true);
        List<String> activeUsernames = activeUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        // สร้าง map สำหรับส่ง response กลับมา แล้วแสดงข้อมูลทั้งจำนวนของ users และชื่อออกไปโชว์
        Map<String, Object> response = new HashMap<>();
        response.put("activeUsernames", activeUsernames);
        response.put("activeUserCount", activeUsers.size());

        return ResponseEntity.ok(response);
    }

    // For Develop:: แสดงชื่อ user(s) ที่กำลัง active
    @GetMapping("/active-users-name")
    public ResponseEntity<List<String>> getActiveUsersName() {
        List<User> activeUsers = userRepository.findByIsActive(true);
        List<String> activeUsernames = activeUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        return ResponseEntity.ok(activeUsernames);
    }

    // For Develop:: แสดงชื่อจำนวน user(s) ที่กำลัง active
    @GetMapping("/active-users-count")
    public ResponseEntity<Integer> getActiveUserCount() {
        List<User> activeUsers = userRepository.findByIsActive(true);
        return ResponseEntity.ok(activeUsers.size());
    }

    //logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        // ดึง JWT token จาก header
        String token = authorizationHeader.replace("Bearer ", "");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        // ดึง username จาก token
        String username = jwtUtil.extractUsername(token);

        // ตรวจสอบ token ว่า valid หรือไม่ และตรวจสอบว่า username ตรงกับที่เราเอามาจาก token
        if (!jwtUtil.validateToken(token, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user != null) {
            // เปลี่ยนสถานะให้เป็น false
            user.setActive(false);
            userRepository.save(user); // บันทึกข้อมูล

            return ResponseEntity.ok("Logged out successfully");
        } else {
            return ResponseEntity.badRequest().body("User is not logged in.");
        }
    }
}
