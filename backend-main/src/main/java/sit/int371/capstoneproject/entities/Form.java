package sit.int371.capstoneproject.entities;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Document(collection = "form")
public class Form {
    @Id // ทำให้ MongoDB รู้ว่านี่คือ field สำหรับ ID
    private ObjectId id;   //เป็น Object ID ที่เอาไว้ระบุตัวข้อมูล
    @Transient // ไม่เก็บ field นี้ใน MongoDB
    public static final String SEQUENCE_NAME = "form_sequence";
    @NotNull(message = "Form Id cannot be null")
    private int formId;
    @LastModifiedDate
    @PastOrPresent(message = "Created date cannot be in the future") //วันที่สร้างและอัปเดตไม่เป็นวันในอนาคต
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
    @NotNull(message = "Form in date cannot be null")  //ให้ user กำหนดวัน-เวลาเข้าพักเอง
    private Date date_in;
    @NotNull(message = "Form out date cannot be null") //ให้ user กำหนดวัน-เวลาออกจากหอพักเอง
    private Date date_out;
    @Size(max = 200, message = "Description details must not exceed 200 characters")
    private String description = "null";

    @NotNull(message = "Dormitory ID cannot be null")
    private int dormId;
    private FormStatusEnum status = FormStatusEnum.reserved;
}
