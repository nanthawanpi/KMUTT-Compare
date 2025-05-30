package sit.int371.capstoneproject.services;

import org.bson.types.Decimal128;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sit.int371.capstoneproject.dtos.DormitoryDTO;
import sit.int371.capstoneproject.dtos.DormitoryUserDTO;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.DormitoryRepository;
import sit.int371.capstoneproject.repositories.UserRepository;


import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class DormitoryService {
    @Autowired
    private DormitoryRepository dormitoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;

    // Method -find all dormitory และเรียกเข้า username, phone, email มาโชว์ด้วย
    public List<DormitoryUserDTO> getAllDormitories() {
        List<Dormitory> dormitories = dormitoryRepository.findAll();
        List<DormitoryUserDTO> dtoList = new ArrayList<>();

        for (Dormitory dormitory : dormitories) {
            DormitoryUserDTO dormUserDTO = new DormitoryUserDTO();
            BeanUtils.copyProperties(dormitory, dormUserDTO); // คัดลอกข้อมูลทั่วไปจาก Dormitory

            // ดึง username, phone, email จาก User collection
            userRepository.findByUserId(dormitory.getUserId())
                    .ifPresent(user -> {
                        dormUserDTO.setUsername(user.getUsername());
                        dormUserDTO.setPhone(user.getPhone());
                        dormUserDTO.setEmail(user.getEmail());
                    });

            dtoList.add(dormUserDTO);
        }

        return dtoList;
    }

    // Method -find dormitory by id
    public DormitoryUserDTO getDormById(Integer id) {
        Dormitory dormitory = dormitoryRepository.findByDormId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dormitory id " + id + " not found !!!"));

        DormitoryUserDTO dormUserDTO = new DormitoryUserDTO();
        BeanUtils.copyProperties(dormitory, dormUserDTO); // คัดลอกข้อมูลทั่วไปจาก Dormitory

        // ดึง username, phone, email จาก User collection
        userRepository.findByUserId(dormitory.getUserId())
                .ifPresent(user -> {
                    dormUserDTO.setUsername(user.getUsername());
                    dormUserDTO.setPhone(user.getPhone());
                    dormUserDTO.setEmail(user.getEmail());
                });

        return dormUserDTO;
    }

    // Method -find dormitory by User id when:: login
    public List<DormitoryUserDTO> getDormitoriesByUserId(Integer userId) {
        return dormitoryRepository.findByUserId(userId).stream().map(dorm -> {
            // แปลง Dormitory เป็น DormitoryUserDTO
            DormitoryUserDTO dormitoryUserDTO = modelMapper.map(dorm, DormitoryUserDTO.class);

            // *** ดึง username, phone, email จาก User collection ***
            User user = userRepository.findByUserId(dorm.getUserId()).orElse(null);
            if (user != null) {
                dormitoryUserDTO.setUserId(user.getUserId());
                dormitoryUserDTO.setUsername(user.getUsername());
                dormitoryUserDTO.setEmail(user.getEmail());
                dormitoryUserDTO.setPhone(user.getPhone());
            }
            return dormitoryUserDTO;
        }).collect(Collectors.toList());
    }

    // Method -create dormitory + count all facilities
    public DormitoryDTO createDorm(Dormitory dormitory) {
        // ดึง userId จาก SecurityContext หรือ Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // หรือสามารถดึง username อื่นๆ ที่เกี่ยวข้องกับผู้ใช้งาน
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found!");
        }
        User user = userOptional.get();
        Integer userId = user.getUserId(); // หรือค่าที่เหมาะสมจาก User


        // ตรวจสอบว่า userId มีอยู่ในฐานข้อมูลหรือไม่
        if (!userRepository.existsByUserId(userId)) {
            throw new ResourceNotFoundException("User id " + userId + " not exited!!!");
        }

        // คำนวณจำนวน facilities
        int totalFacilities = 0;
        if (dormitory.getRoom_facility() != null) {
            totalFacilities += dormitory.getRoom_facility().size();
        }
        if (dormitory.getBuilding_facility() != null) {
            totalFacilities += dormitory.getBuilding_facility().size();
        }
        dormitory.setCount_facilities(totalFacilities);

        // กำหนด userId จากการล็อกอิน
        dormitory.setUserId(userId);

        // กำหนด Rating เริ่มต้นถ้าไม่ได้ส่งมา
        if (dormitory.getRating() == null) {
            dormitory.setRating(new Dormitory.Rating(new Decimal128(new BigDecimal("0.0")), 0));
        }

        // บันทึกลง DB
        Dormitory addDorm = new Dormitory();
        addDorm.setDormId(dormitory.getDormId());
        addDorm.setDormName(dormitory.getDormName());
        addDorm.setStatus(dormitory.getStatus());
        addDorm.setAddress(dormitory.getAddress());
        addDorm.setRoomCount(dormitory.getRoomCount());
        addDorm.setType(dormitory.getType());
        addDorm.setSize(dormitory.getSize());
        addDorm.setMin_price(dormitory.getMin_price());
        addDorm.setMax_price(dormitory.getMax_price());
        addDorm.setDistance(dormitory.getDistance());
        addDorm.setCreated_at(dormitory.getCreated_at());
        addDorm.setUpdated_at(dormitory.getUpdated_at());
        addDorm.setImage(dormitory.getImage());
        addDorm.setBuilding_facility(dormitory.getBuilding_facility());
        addDorm.setRoom_facility(dormitory.getRoom_facility());
        addDorm.setCount_facilities(dormitory.getCount_facilities());
        addDorm.setRating(dormitory.getRating());

        addDorm.setUserId(userId);
        return modelMapper.map(dormitoryRepository.save(addDorm), DormitoryDTO.class);
    }

    //Method -update dormitory
    public DormitoryDTO updateDorm(Integer id, DormitoryDTO dormitoryDTO) throws AccessDeniedException {
        Dormitory existingDorm = dormitoryRepository.findByDormId(id).orElseThrow(
                () -> new ResourceNotFoundException("Dormitory ID " + id + " does not exist!"));

        // ดึงข้อมูล user จาก SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found!");
        }

        User user = userOptional.get();
        String role = String.valueOf(user.getRole()); // role ของ user (ADMIN หรือ USER)
        Integer userId = user.getUserId();

        // ตรวจสอบสิทธิ์การแก้ไข
        if (!role.equals("admin") && !Objects.equals(existingDorm.getUserId(), userId)) {
            throw new AccessDeniedException("You do not have permission to update this dormitory!");
        }

        // อัปเดตค่าของ Dormitory
        existingDorm.setDormName(dormitoryDTO.getDormName());
        existingDorm.setStatus(dormitoryDTO.getStatus());
        existingDorm.setAddress(dormitoryDTO.getAddress());
        existingDorm.setRoomCount(dormitoryDTO.getRoomCount());
        existingDorm.setType(dormitoryDTO.getType());
        existingDorm.setSize(dormitoryDTO.getSize());
        existingDorm.setMin_price(dormitoryDTO.getMin_price());
        existingDorm.setMax_price(dormitoryDTO.getMax_price());
        existingDorm.setDistance(dormitoryDTO.getDistance());
        existingDorm.setCreated_at(dormitoryDTO.getCreated_at());
        existingDorm.setUpdated_at(dormitoryDTO.getUpdated_at());
        existingDorm.setImage(dormitoryDTO.getImage());
        existingDorm.setBuilding_facility(dormitoryDTO.getBuilding_facility());
        existingDorm.setRoom_facility(dormitoryDTO.getRoom_facility());

        // คำนวณและอัปเดตค่า countFacility
        int totalFacilities = calculateFacilitiesCount(dormitoryDTO.getBuilding_facility(), dormitoryDTO.getRoom_facility());
        existingDorm.setCount_facilities(totalFacilities);


        // บันทึกลงฐานข้อมูล
        Dormitory updatedDormitory = dormitoryRepository.save(existingDorm);
        return modelMapper.map(updatedDormitory, DormitoryDTO.class);
    }

    // Method การคำนวณจำนวน facilities
    private int calculateFacilitiesCount(List<String> buildingFacilities, List<String> roomFacilities) {
        // นับจำนวน facilities ใน building_facility และ room_facility รวมกัน
        int countBuildingFacilities = buildingFacilities != null ? buildingFacilities.size() : 0;
        int countRoomFacilities = roomFacilities != null ? roomFacilities.size() : 0;
        // รวมจำนวนทั้งหมด
        return countBuildingFacilities + countRoomFacilities;
    }

    //Method -delete dormitory
    public String deleteDorm(Integer id) throws AccessDeniedException {
        // ตรวจสอบว่า dorm มีอยู่จริงหรือไม่
        Dormitory dormitory = dormitoryRepository.findByDormId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dormitory with Id " + id + " does not exist!!!"));

        // ดึงข้อมูลผู้ใช้จาก SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found!");
        }

        User user = userOptional.get();
        String role = String.valueOf(user.getRole());
        Integer userId = user.getUserId();

        // ตรวจสอบสิทธิ์การลบ
        if (role.equals("admin") || Objects.equals(dormitory.getUserId(), userId)) {
            dormitoryRepository.deleteByDormId(id);
            return "Dormitory with Id " + id + " has been deleted successfully!";
        } else {
            throw new AccessDeniedException("You do not have permission to delete this dormitory!");
        }
    }
}
