package sit.int371.capstoneproject.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.int371.capstoneproject.configs.JwtUtil;
import sit.int371.capstoneproject.dtos.RegisterRequest;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.entities.UserRoleEnum;
import sit.int371.capstoneproject.exceptions.BadRequestException;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //Register Method
    public ResponseEntity<?> register(RegisterRequest request) {
        // ตรวจสอบว่า username หรือ email ซ้ำหรือไม่
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        //สร้าง User และบันทึกลง DB
        User addUser = new User();
        addUser.setUserId(request.getUserId());
        addUser.setUsername(request.getUsername());
        addUser.setName(request.getName());
        addUser.setEmail(request.getEmail());
        addUser.setPassword(passwordEncoder.encode(request.getPassword()));
        addUser.setPhone(request.getPhone());
        userRepository.save(addUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("Register successful!");
    }


    // Login Method
    public Map<String, String> login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // การตรวจสอบรหัสผ่านที่เก็บในฐานข้อมูล
            if (passwordEncoder.matches(password, user.getPassword())) {  // ใช้ passwordEncoder.matches() สำหรับตรวจสอบรหัสผ่านที่เข้ารหัส
                // สร้าง access token และ refresh token + role
                String accessToken = jwtUtil.generateToken(username, user.getRole(), user.getUserId(), false); // 1 วัน
                String refreshToken = jwtUtil.generateToken(username, user.getRole(), user.getUserId(), true); // 30 วัน

                // Return เป็น JSON format
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);
                return tokens;
            } else {
                throw new RuntimeException("Invalid password!");  // หากรหัสผ่านไม่ตรง
            }
        } else {
            throw new ResourceNotFoundException("User not found!");  // หากผู้ใช้ไม่พบ
        }
    }

    // Change Password Method
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // ตรวจสอบว่ารหัสผ่านเก่าตรงกับในระบบหรือไม่
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false; // รหัสผ่านเก่าไม่ถูกต้อง
        }
        // ตั้งค่ารหัสผ่านใหม่
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
