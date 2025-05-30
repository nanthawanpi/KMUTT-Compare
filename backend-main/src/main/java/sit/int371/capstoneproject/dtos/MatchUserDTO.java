package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchUserDTO {
    //Matching Username & Password
    private String username;
    @NotNull(message = "Password cannot be null")
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%^#&*()_+{}\\[\\]:;<>,.?/~\\\\|-])[\\S]{8,20}$",
            message = "must be at least 8 characters long to 20 and contain at least one uppercase letter, one number, and one special character (@$!%*?&_-)")
    private String password;
    private String confirm;
}
