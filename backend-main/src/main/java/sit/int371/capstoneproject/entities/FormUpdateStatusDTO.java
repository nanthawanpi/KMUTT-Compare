package sit.int371.capstoneproject.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import sit.int371.capstoneproject.dtos.UserOwnerDTO;

import java.util.Date;

@Getter
@Setter
public class FormUpdateStatusDTO {
    @NotNull(message = "Form Id cannot be null")
    private int formId;
    @LastModifiedDate
    @PastOrPresent(message = "Created date cannot be in the future")
    private Date form_date;
    private int userId;
    //    @NotEmpty(message = "Name cannot be empty")
//    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;
    //    @Indexed(unique = true)
//    @NotEmpty(message = "Email cannot be empty")
//    @Email(message = "Email should be valid")
    private String email;
    //    @NotEmpty(message = "Phone cannot be empty")
//    @Pattern(regexp = "^\\d{10}$")
//    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    private String phone;
    //    @NotNull(message = "Form in date cannot be null")
    private Date date_in;
    //    @NotNull(message = "Form out date cannot be null")
    private Date date_out;
    //    @Size(max = 200, message = "Description details must not exceed 200 characters")
    private String description = "null";

    private int dormId;
    private UserOwnerDTO owner; // เพิ่ม OwnerDTO
    private FormStatusEnum status;
}
