package sit.int371.capstoneproject.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.FormStatusEnum;

import java.util.Date;

@Getter
@Setter
public class FormDTO {
    @NotNull(message = "Form Id cannot be null")
    private int formId;
    @LastModifiedDate
    @PastOrPresent(message = "Created date cannot be in the future")
    private Date form_date;
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
    @NotNull(message = "Form in date cannot be null")
    private Date date_in;
    @NotNull(message = "Form out date cannot be null")
    private Date date_out;
    @Size(max = 200, message = "Description details must not exceed 200 characters")
    private String description = "null";

    @NotNull(message = "Dormitory ID cannot be null")
    private int dormId;
    private String dormName;
    @Valid //เช็คว่า fields ใน Address ครบหรือยัง
    private Dormitory.Address address;

    private UserOwnerDTO owner; // เพิ่ม OwnerDTO
    private FormStatusEnum status = FormStatusEnum.reserved;
}
