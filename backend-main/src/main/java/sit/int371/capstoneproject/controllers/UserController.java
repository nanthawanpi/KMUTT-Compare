package sit.int371.capstoneproject.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sit.int371.capstoneproject.autoId.SequenceGenerateDormService;
import sit.int371.capstoneproject.configs.JwtUtil;
import sit.int371.capstoneproject.dtos.*;
import sit.int371.capstoneproject.autoId.SequenceGenerateUserService;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.Form;
import sit.int371.capstoneproject.entities.FormUpdateStatusDTO;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.BadRequestException;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.UserRepository;
import sit.int371.capstoneproject.services.DormitoryService;
import sit.int371.capstoneproject.services.FormService;
import sit.int371.capstoneproject.services.RatingService;
import sit.int371.capstoneproject.services.UserService;

import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173","http://cp24kk2.sit.kmutt.ac.th","https://kmutt-compare.sit.kmutt.ac.th"})
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private FormService formService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private DormitoryService dormitoryService;
    @Autowired
    private SequenceGenerateDormService sequenceGenerateDormService;
    @Autowired
    private SequenceGenerateUserService sequenceGenerateUserService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;

    // Get user when login following user:: ดึงข้อมูลของตัวเอง user ที่ล็อกอินอยู่
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public UserMeDTO getCurrentUser() {
        return userService.getCurrentUser();
    }

    // Create User
    @PostMapping("/me")
    public CreateUserDTO createdUser(@Valid @RequestBody CreateUserDTO createUserDTO){
        // เช็คว่า username ซ้ำหรือไม่
        if (userRepository.existsByUsername(createUserDTO.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        // เช็คว่า email ซ้ำหรือไม่
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        // generate user id
        createUserDTO.setUserId((int) sequenceGenerateUserService.generateSequence(User.SEQUENCE_NAME));
        User user = modelMapper.map(createUserDTO, User.class);
        return userService.createUser(user);
    }

    // Update User
    @PutMapping("/me")
    public UserDTO updatedUser(@Valid @RequestBody UserDTO userDTO){
        return userService.updateUser(userDTO);
    }

    // Delete User
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteUser(@RequestBody MatchUserDTO matchUserDTO) {
        String matchResult = userService.matchPassword(matchUserDTO.getUsername(), matchUserDTO.getPassword());
        if (!matchResult.equals("Password Matched!")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(matchResult);
        }
        if (!"delete account".equals(matchUserDTO.getConfirm())) {
            return ResponseEntity.badRequest().body("Please press 'delete account' for delete your account!");
        }
        String deleteResult = userService.deleteUser(matchUserDTO.getUsername());
        return ResponseEntity.ok(deleteResult);
    }

// ------------------------ Forms User Management -----------------------------
//Get Forms when login following user:: ดึง form(s) ของ user ที่ล็อกอินอยู่
    @GetMapping("/forms")
    public ResponseEntity<List<FormDTO>> getFormsByUserId(@RequestHeader("Authorization") String token) {
        // ตัด "Bearer " ออกจาก token
        String jwt = token.substring(7);
        // ดึง username จาก JWT
        String username = jwtUtil.extractUsername(jwt);
        // ค้นหาผู้ใช้จาก username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        // ค้นหาฟอร์มของ userId นี้
        List<FormDTO> forms = userService.getFormsByUserId(user.getUserId());
        if (forms.isEmpty()) {
            throw new ResourceNotFoundException("No forms found for this user.");
        }
        return ResponseEntity.ok(forms);
    }

    // Get user when login following user:: ดึงข้อมูลของตัวเอง user ที่ล็อกอินอยู่
    @GetMapping("/receive-form")
    public ResponseEntity<List<FormDTO>> getUserReceivedForms(Authentication authentication) {
        // เรียกใช้ UserService เพื่อนำข้อมูลฟอร์มของผู้ใช้งาน
        List<FormDTO> formDTOs = userService.getUserReceivedForms(authentication);
        return ResponseEntity.ok(formDTOs);
    }

    //Get Forms when login following user:: ดึง form(s) ของ user ที่ล็อกอินอยู่
    @GetMapping("/sent-form")
    public ResponseEntity<List<FormDTO>> getFormsByUser(@RequestHeader("Authorization") String token) {
        // ตัด "Bearer " ออกจาก token
        String jwt = token.substring(7);
        // ดึง username จาก JWT
        String username = jwtUtil.extractUsername(jwt);

        // ค้นหาผู้ใช้จาก username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        // ค้นหาฟอร์มของ userId นี้
        List<FormDTO> forms = userService.getFormsByUserId(user.getUserId());

        if (forms.isEmpty()) {
            throw new ResourceNotFoundException("No forms found for this user.");
        }
        return ResponseEntity.ok(forms);
    }

    // Get Form by formId
    @GetMapping("/sent-form/{formId}")
    public ResponseEntity<FormDTO> getFormById(@PathVariable Integer formId) {
        FormDTO formDTO = userService.getFormByFormId(formId);
        return ResponseEntity.ok(formDTO);
    }

    // Method -Create Form by User
    @PostMapping("/sent-form")
    public ResponseEntity<FormCreateDTO> createdFormByUser(@Valid @RequestBody FormCreateDTO formCreateDTO, Authentication authentication) {
        // Generate formId และ map DTO ไปเป็น Entity
        Form form = modelMapper.map(formCreateDTO, Form.class);
        form.setFormId((int) sequenceGenerateUserService.generateSequence(Form.SEQUENCE_NAME));
        FormCreateDTO createdForm = userService.createFormByUser(form, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdForm);
    }

    //Update Form
    @PutMapping("/sent-form/{formId}")
    public FormUpdateDTO updatedForm(@PathVariable Integer formId, @Valid @RequestBody FormUpdateDTO formUpdateDTO, Authentication authentication){
        return formService.updateForm(formId, formUpdateDTO, authentication);
    }

    //Method -Update Status Form by User
    @PutMapping("/form/{formId}/check-in")
    public FormUpdateDTO updateFormByUser(@PathVariable Integer formId, @Valid @RequestBody FormUpdateStatusDTO formUpdateStatusDTO, Authentication authentication){
        return userService.updateFormByUser(formId, formUpdateStatusDTO, authentication);
    }

    //Delete Form
    @DeleteMapping("/form/{formId}")
    public ResponseEntity<String> deletedForm(@PathVariable Integer formId) throws AccessDeniedException {
        String message = formService.deleteForm(formId);
        return ResponseEntity.ok(message);
    }

// ------------------------ Dormitories User Management -----------------------------
    // Get Dormitories when login following user:: ดึง dormitory ของ user ที่ล็อกอินอยู่
    @GetMapping("/dormitories")
    public ResponseEntity<List<DormitoryUserDTO>> getDormitoriesByUser(@RequestHeader("Authorization") String token) {
        // ตัด "Bearer " ออกจาก token
        String jwt = token.substring(7);
        // ดึง username จาก JWT
        String username = jwtUtil.extractUsername(jwt);
        // หา user จาก database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        // ดึง dormitory ที่ userId นี้สร้างไว้
        List<DormitoryUserDTO> dormitories = userService.getDormitoriesByUserId(user.getUserId());
        if (dormitories.isEmpty()) {
            throw new ResourceNotFoundException("No dormitories found for this user.");
        }
        return ResponseEntity.ok(dormitories);
    }

    // Rating
    @PutMapping("/votes/dormitory/{dormId}")
    public ResponseEntity<String> vote(@PathVariable int dormId, @RequestBody VoteRequest voteRequest) {
        if (voteRequest.getTotalScore() < 1 || voteRequest.getTotalScore() > 5) {
            return ResponseEntity.badRequest().body("Score must be between 1 to 5");
        }
        // เรียกใช้ service เพื่อทำการโหวต
        double newAverage = ratingService.voteForDormitory(dormId, voteRequest.getTotalScore());
        // ส่งค่าเฉลี่ยใหม่กลับไปให้ผู้ใช้
        return ResponseEntity.ok("Voted successfully! New average rating: " + newAverage);
    }

    // Match Email and change UserId in dormitory
    @PutMapping("/dormitory/change-user")
    public ResponseEntity<String> updateUserId(@RequestBody MatchUserEmailDTO matchUserEmailDTO, @RequestHeader("Authorization") String authorizationHeader) {
        // ดึง token จาก Authorization header (เช่น "Bearer <token>")
        String token = authorizationHeader.substring(7);
        int currentUserId = jwtUtil.extractUserId(token);

        // เรียกใช้ Service พร้อมกับ email, currentUserId และ dormIds ที่ดึงได้จาก request
        userService.updateUserIdInDorms(matchUserEmailDTO.getEmail(), currentUserId, matchUserEmailDTO.getDormIds());
        return ResponseEntity.ok("Transfers Dormitories successfully.");
    }


    //Create Dormitory
    @PostMapping("/dormitory")
    public DormitoryDTO createdDormitory(@Valid @RequestBody DormitoryDTO dormitoryDTO){
        //generate dormitory id
        dormitoryDTO.setDormId((int) sequenceGenerateDormService.generateSequence(Dormitory.SEQUENCE_NAME));
        Dormitory dormitory = modelMapper.map(dormitoryDTO, Dormitory.class);
        return dormitoryService.createDorm(dormitory);
    }

    //Update Dormitory
    @PutMapping("/dormitory/{dormId}")
    public DormitoryDTO updatedDorm(@PathVariable Integer dormId, @Valid @RequestBody DormitoryDTO dormitoryDTO) throws AccessDeniedException {
        return dormitoryService.updateDorm(dormId, dormitoryDTO);
    }

    //Delete Dormitory
    @DeleteMapping("/dormitory/{dormId}")
    public ResponseEntity<String> deletedDorm(@PathVariable Integer dormId) throws AccessDeniedException {
        String message = dormitoryService.deleteDorm(dormId);
        return ResponseEntity.ok(message);
    }

// ------------------------ Favorite User Management -----------------------------
    //Get Favorites when login following user:: ดึง favorite(s) ของ user ที่ล็อกอินอยู่
    @GetMapping("/favorites")
    public ResponseEntity<List<FavDormDTO>> getFavoritesByUser(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtUtil.extractUsername(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        List<FavDormDTO> favorites = userService.getFavoritesByUserId(user.getUserId());

        if (favorites.isEmpty()) {
            throw new ResourceNotFoundException("No favorites found for this user.");
        }
        return ResponseEntity.ok(favorites);
    }
}
