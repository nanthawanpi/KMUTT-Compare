package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import sit.int371.capstoneproject.entities.UserRoleEnum;

@Getter
@Setter
public class RegisterRequest {
    @NotNull(message = "User Id cannot be null")
    private int userId;
    @NotEmpty(message = "User name cannot be empty")
    @Size(max = 50, message = "User name must not exceed 50 characters")
    private String username;
    @NotEmpty(message = "Name cannot be empty")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "Password cannot be null")
    @Size(min = 8,max = 20)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%^#&*()_+{}\\[\\]:;<>,.?/~\\\\|-])[\\S]{8,20}$", message = "must be 8-20 characters long, at least 1 of uppercase, lowercase, number and special characters")
    private String password;
    @NotEmpty(message = "Phone cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;
}
