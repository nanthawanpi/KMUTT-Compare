package sit.int371.capstoneproject.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sit.int371.capstoneproject.autoId.SequenceGenerateUserService;
import sit.int371.capstoneproject.dtos.*;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.BadRequestException;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.UserRepository;
import sit.int371.capstoneproject.services.*;
import sit.int371.capstoneproject.util.ListMapper;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173","http://cp24kk2.sit.kmutt.ac.th","https://kmutt-compare.sit.kmutt.ac.th"})
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private FormService formService;
    @Autowired
    private DormitoryService dormitoryService;
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SequenceGenerateUserService sequenceGenerateUserService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ListMapper listMapper;

        @PreAuthorize("hasRole('admin')") // Admin เท่านั้นที่เรียก API นี้ได้
        @GetMapping("/dashboard")
        public DashboardDTO getDashboardData() {
            return adminService.getDashboardData();
        }

        //Get All User by Admin
        @GetMapping("/users")
        public List<OutputUserDTO> getAllUserDTO(){
            List<User> userList = adminService.getAllUserByAdmin();
            return listMapper.mapList(userList, OutputUserDTO.class, modelMapper);
        }

        @GetMapping("user/{userId}")
        public OutputUserDTO getUserDTO(@PathVariable Integer userId){
            return modelMapper.map(adminService.getUserIdByAdmin(userId), OutputUserDTO.class);
        }

        @PostMapping("/user")
        public ResponseEntity<CreateUserDTO> createdUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
            if (userRepository.existsByUsername(createUserDTO.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            if (userRepository.existsByEmail(createUserDTO.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            createUserDTO.setUserId((int) sequenceGenerateUserService.generateSequence(User.SEQUENCE_NAME));
            User user = modelMapper.map(createUserDTO, User.class);
            CreateUserDTO createdUser = adminService.createUserByAdmin(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        }


        //แก้ไขหรืออัพเดทข้อมูลของ user โดย admin
        @PutMapping("/user/{userid}")
        public UserDTO updatedUserByAdmin(@PathVariable Integer userid, @Valid @RequestBody UserDTO userDTO){
            return adminService.updateUserByAdmin(userid, userDTO);
        }

        @DeleteMapping("user/{userid}")
        public ResponseEntity<String> deletedUser(@PathVariable Integer userid){
            String message = adminService.deleteUserByAdmin(userid);
            return ResponseEntity.ok(message);
        }

        // Get All Form
        @GetMapping("/forms")
        public ResponseEntity<List<FormDTO>> getAllForms() {
            List<FormDTO> forms = formService.getAllForms();
            if (forms.isEmpty()) {
                throw new ResourceNotFoundException("Forms not found!");
            }
            return ResponseEntity.ok(forms);
        }
        // Get Form By id
        @PreAuthorize("hasRole('admin')")
        @GetMapping("/form/{formId}")
        public ResponseEntity<FormDTO> getFormById(@PathVariable Integer formId) {
            FormDTO formDTO = formService.getFormById(formId);
            return ResponseEntity.ok(formDTO);
        }

        //Get All Dormitories
        @GetMapping("/dormitories")
        public ResponseEntity<List<DormitoryUserDTO>> getAllDormitories() {
            List<DormitoryUserDTO> dormitories = dormitoryService.getAllDormitories(); // ดึง username จาก User collection
            // เช็คว่ามีข้อมูลหรือไม่ ถ้าไม่มีข้อมูลให้ส่ง 404
            if (dormitories.isEmpty()) {
                throw new ResourceNotFoundException("Dormitory not found!");
            }
            return ResponseEntity.ok(dormitories);
        }

        //Get Dormitory by id
        @GetMapping("/dormitory/{dormId}")
        public ResponseEntity<DormitoryUserDTO> getDormitoryById(@PathVariable Integer dormId) {
            DormitoryUserDTO dormitory = dormitoryService.getDormById(dormId); // ดึง username จาก User collection
            return ResponseEntity.ok(dormitory);
        }

        //Get All Favorites
        @GetMapping("/favorites")
        public List<FavDormDTO> getAllFavorites() {
            return favoriteService.getAllFavorites();
        }

        // Get Favorite By id
        @GetMapping("favorite/{favId}")
        public FavDormDTO getFavById(@PathVariable Integer favId) {
            return favoriteService.getFavById(favId);
        }
}
