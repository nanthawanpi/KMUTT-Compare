package sit.int371.capstoneproject.services;

import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.Form;
import sit.int371.capstoneproject.entities.FormStatusEnum;
import sit.int371.capstoneproject.repositories.DormitoryRepository;
import sit.int371.capstoneproject.repositories.FormRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RatingService {
    @Autowired
    private DormitoryRepository dormitoryRepository;
    @Autowired
    private FormRepository formRepository;

    public double voteForDormitory(int dormId, int score) {
        Dormitory dormitory = dormitoryRepository.findByDormId(dormId)
                .orElseThrow(() -> new RuntimeException("Dormitory not found"));

        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }

        // ดึงค่าเดิมจากฐานข้อมูล
        BigDecimal previousTotalScore = dormitory.getRating().getTotalScore().bigDecimalValue();
        int previousCountVotes = dormitory.getRating().getCount_votes();

        // อัปเดตค่าใหม่
        BigDecimal updatedTotalScore = previousTotalScore.add(BigDecimal.valueOf(score));
        int updatedCountVotes = previousCountVotes + 1;

        // คำนวณค่าเฉลี่ยใหม่
        BigDecimal averageRating = updatedTotalScore.divide(BigDecimal.valueOf(updatedCountVotes), 2, RoundingMode.HALF_UP);

        // อัปเดตค่ากลับไปที่ Dormitory
        dormitory.getRating().setTotalScore(new Decimal128(averageRating));
        dormitory.getRating().setCount_votes(updatedCountVotes);

        // อัปเดต status ใน Form
        List<Form> relatedForms = formRepository.findByDormId(dormId);
        for (Form form : relatedForms) {
            form.setStatus(FormStatusEnum.success); // setting status "success"
            formRepository.save(form);
        }

        // บันทึกค่าที่อัปเดตลงฐานข้อมูล
        dormitoryRepository.save(dormitory);
        // คืนค่าค่าเฉลี่ยใหม่
        return averageRating.doubleValue();
    }
}