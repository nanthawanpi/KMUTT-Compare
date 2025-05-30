package sit.int371.capstoneproject.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardDTO {
    private int count_dormitories;
    private int count_users;
    private int active_users;
    private List<Integer> active_user_id;
    private List<String> active_user_username;
}
