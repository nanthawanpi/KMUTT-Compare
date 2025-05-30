package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatchUserEmailDTO {
    //Matching Email of new user
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "Dormitory Id cannot be null")
    private List<Integer> dormIds;
}
