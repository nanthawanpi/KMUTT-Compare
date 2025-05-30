package sit.int371.capstoneproject.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
