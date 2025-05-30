package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import sit.int371.capstoneproject.entities.UserRoleEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    @Indexed(unique = true)
    private int userId;
    @Indexed(unique = true)
    @NotEmpty(message = "User name cannot be empty")
    @Size(max = 50, message = "User name must not exceed 50 characters")
    private String username;
    @NotEmpty(message = "User name cannot be empty")
    @Size(max = 50, message = "User name must not exceed 50 characters")
    private String name;
    @NotNull(message = "Password cannot be null")
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%^#&*()_+{}\\[\\]:;<>,.?/~\\\\|-])[\\S]{8,}$", message = "must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character (@$!%*?&_-)")
    private String password;
    @Indexed(unique = true)
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;
    @NotEmpty(message = "Phone cannot be empty")
    @Pattern(regexp = "^\\d{10}$")
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    private String phone;
    private UserRoleEnum role = UserRoleEnum.user;
    private boolean isActive = false;
}
