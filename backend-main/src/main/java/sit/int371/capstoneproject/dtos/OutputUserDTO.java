package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import sit.int371.capstoneproject.entities.UserRoleEnum;

import java.util.Date;

@Getter
@Setter
public class OutputUserDTO {
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
    private UserRoleEnum role = UserRoleEnum.user;
    @CreatedDate
    @PastOrPresent(message = "Created date cannot be in the future")
    private Date createdOn;
    @LastModifiedDate
    @PastOrPresent(message = "Created date cannot be in the future")
    private Date updatedOn;
    private boolean isActive;
}
