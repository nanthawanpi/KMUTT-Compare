package sit.int371.capstoneproject.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteDTO {
    @NotNull(message = "Favorite Id cannot be null")
    private int favId;
    @NotNull(message = "Dormitory Id is required")
    @Min(value = 1, message = "Dormitory Id must be greater than 0")
    private int dormId;
    private int userId;
}
