package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
public class UserOwnerDTO {
    @NotNull(message = "User ID cannot be null")
    private int userId;
    @NotEmpty(message = "Name cannot be empty")
    @Size(max = 50, message = "Name must not exceed 50 characters")
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
