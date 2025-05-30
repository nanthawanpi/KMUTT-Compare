package sit.int371.capstoneproject.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sit.int371.capstoneproject.autoId.SequenceGenerateDormService;
import sit.int371.capstoneproject.configs.JwtUtil;
import sit.int371.capstoneproject.dtos.DormitoryDTO;
import sit.int371.capstoneproject.dtos.DormitoryUserDTO;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.UserRepository;
import sit.int371.capstoneproject.services.DormitoryService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173","http://cp24kk2.sit.kmutt.ac.th","https://kmutt-compare.sit.kmutt.ac.th"})
@RequestMapping("/api/dormitories")
public class DormitoryController {
    @Autowired
    private DormitoryService dormitoryService;
    @Autowired
    private SequenceGenerateDormService sequenceGenerateDormService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;


    //Get All Dormitories
    @GetMapping
    public ResponseEntity<List<DormitoryUserDTO>> getAllDormitories() {
        List<DormitoryUserDTO> dormitories = dormitoryService.getAllDormitories(); // ดึง username จาก User collection
        // เช็คว่ามีข้อมูลหรือไม่ ถ้าไม่มีข้อมูลให้ส่ง 404
        if (dormitories.isEmpty()) {
            throw new ResourceNotFoundException("Dormitory not found!");
        }
        return ResponseEntity.ok(dormitories);
    }

    //Get Dormitory by id
    @GetMapping("/{dormId}")
    public ResponseEntity<DormitoryUserDTO> getDormitoryById(@PathVariable Integer dormId) {
        DormitoryUserDTO dormitory = dormitoryService.getDormById(dormId); // ดึง username จาก User collection
        return ResponseEntity.ok(dormitory);
    }

    // Get Dormitories when login following user:: ดึง dormitory ของ user ที่ล็อกอินอยู่
    @GetMapping("/user")
    public ResponseEntity<List<DormitoryUserDTO>> getDormitoriesByUser(@RequestHeader("Authorization") String token) {
        // ตัด "Bearer " ออกจาก token
        String jwt = token.substring(7);
        // ดึง username จาก JWT
        String username = jwtUtil.extractUsername(jwt);
        // หา user จาก database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        // ดึง dormitory ที่ userId นี้สร้างไว้
        List<DormitoryUserDTO> dormitories = dormitoryService.getDormitoriesByUserId(user.getUserId());
        if (dormitories.isEmpty()) {
            throw new ResourceNotFoundException("No dormitories found for this user.");
        }
        return ResponseEntity.ok(dormitories);
    }

    //Create Dormitory
    @PostMapping("")
    public DormitoryDTO createdDormitory(@Valid @RequestBody DormitoryDTO dormitoryDTO){
        //generate dormitory id
        dormitoryDTO.setDormId((int) sequenceGenerateDormService.generateSequence(Dormitory.SEQUENCE_NAME));
        Dormitory dormitory = modelMapper.map(dormitoryDTO, Dormitory.class);
        return dormitoryService.createDorm(dormitory);
    }

    //Update Dormitory
    @PutMapping("/{dormId}")
    public DormitoryDTO updatedDorm(@PathVariable Integer dormId, @Valid @RequestBody DormitoryDTO dormitoryDTO) throws AccessDeniedException {
        return dormitoryService.updateDorm(dormId, dormitoryDTO);
    }

    //Delete Dormitory
    @DeleteMapping("/{dormId}")
    public ResponseEntity<String> deletedDorm(@PathVariable Integer dormId) throws AccessDeniedException {
        String message = dormitoryService.deleteDorm(dormId);
        return ResponseEntity.ok(message);
    }
}
