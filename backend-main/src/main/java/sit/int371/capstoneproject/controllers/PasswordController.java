package sit.int371.capstoneproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sit.int371.capstoneproject.dtos.ChangePasswordDTO;
import sit.int371.capstoneproject.services.AuthenticationService;


@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173","http://cp24kk2.sit.kmutt.ac.th","https://kmutt-compare.sit.kmutt.ac.th"})
@RequestMapping("/api")
public class PasswordController {
    @Autowired
    private AuthenticationService authService;

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        // ดึงข้อมูล user ที่ login อยู่
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        // ดึง username จาก Authentication
        String username = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        // เรียก service เพื่อเปลี่ยนรหัสผ่าน
        boolean isChanged = authService.changePassword(username, changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
        if (isChanged) {
            return ResponseEntity.ok("Password changed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect old password");
        }
    }
}
