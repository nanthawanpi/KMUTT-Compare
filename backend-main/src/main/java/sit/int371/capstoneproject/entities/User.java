package sit.int371.capstoneproject.entities;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Document(collection = "user")
public class User {
    @Id
    private ObjectId id;
    @Transient // ไม่เก็บ field นี้ใน MongoDB
    public static final String SEQUENCE_NAME = "user_sequence";
    @Indexed(unique = true)
    private int userId;
    @Indexed(unique = true)
    @NotEmpty(message = "User name cannot be empty")
    @Size(max = 50, message = "User name must not exceed 50 characters")
    private String username;
    @NotEmpty(message = "Name cannot be empty")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;
    @NotNull(message = "Password cannot be null")
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%^#&*()_+{}\\[\\]:;<>,.?/~\\\\|-])[\\S]{8,20}$",
            message = "must be at least 8 characters long to 20 and contain at least one uppercase letter, one number, and one special character (@$!%*?&_-)")
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
    @CreatedDate
    @PastOrPresent(message = "Created date cannot be in the future") //วันที่สร้างและอัปเดตไม่เป็นวันในอนาคต
    private Date createdOn;
    @LastModifiedDate
    @PastOrPresent(message = "Created date cannot be in the future")
    private Date updatedOn;
    private boolean isActive = false;
}
