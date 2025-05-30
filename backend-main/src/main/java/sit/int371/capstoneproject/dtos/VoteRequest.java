package sit.int371.capstoneproject.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {
    private int totalScore; // รับค่าคะแนนจาก user
}
