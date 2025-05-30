package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
public class UserMeDTO {
    //ไม่โชว์ password
    @Indexed(unique = true)
    private int userId;
    @Indexed(unique = true)
    @NotEmpty(message = "User name cannot be empty")
    @Size(max = 50, message = "User name must not exceed 50 characters")
    private String username;
    @NotEmpty(message = "User name cannot be empty")
    @Size(max = 50, message = "User name must not exceed 50 characters")
    private String name;
    @Indexed(unique = true)
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;
    @NotEmpty(message = "Phone cannot be empty")
    @Pattern(regexp = "^\\d{10}$")
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    private String phone;
}
