package sit.int371.capstoneproject.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.int371.capstoneproject.dtos.CreateUserDTO;
import sit.int371.capstoneproject.dtos.DashboardDTO;
import sit.int371.capstoneproject.dtos.UserDTO;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.BadRequestException;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.DormitoryRepository;
import sit.int371.capstoneproject.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired
    private DormitoryRepository dormitoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;

    public DashboardDTO getDashboardData() {
        DashboardDTO dashboardDTO = new DashboardDTO();

        // คำนวณจำนวน dormitories
        dashboardDTO.setCount_dormitories((int) dormitoryRepository.count());

        // คำนวณจำนวน users
        dashboardDTO.setCount_users((int) userRepository.count());

        // คำนวณ active users
        List<User> activeUsers = userRepository.findByIsActiveTrue();  // ค้นหา active users
        dashboardDTO.setActive_users(activeUsers.size());

        // ดึง userId และ username ของ active users
        List<Integer> activeUserIds = activeUsers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        dashboardDTO.setActive_user_id(activeUserIds);

        List<String> activeUserUsernames = activeUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        dashboardDTO.setActive_user_username(activeUserUsernames);

        return dashboardDTO;
    }

    //Method -update user By Admin
    public UserDTO updateUserByAdmin(Integer id, UserDTO userDTO){
        User exitsUser = userRepository.findByUserId(id).orElseThrow(
                () -> new ResourceNotFoundException(id + " does not exited!!!"));
        // เช็คว่า username ซ้ำหรือไม่
        if (userRepository.existsByUsername(userDTO.getUsername()) &&
                !exitsUser.getUsername().equals(userDTO.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // เช็คว่า email ซ้ำหรือไม่
        if (userRepository.existsByEmail(userDTO.getEmail()) &&
                !exitsUser.getEmail().equals(userDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        exitsUser.setUsername(userDTO.getUsername());
        exitsUser.setName(userDTO.getName());
        exitsUser.setEmail(userDTO.getEmail());
        exitsUser.setPhone(userDTO.getPhone());
        exitsUser.setRole(userDTO.getRole());
        User updatedUserByAdmin = userRepository.save(exitsUser);
        return modelMapper.map(updatedUserByAdmin, UserDTO.class);
    }

    //Move this form <User>
    //Method -find All users by Admin
    public List<User> getAllUserByAdmin(){
        List<User> user = userRepository.findAll();
        if (user.isEmpty()){
            throw new ResourceNotFoundException("User is not found!");
        }
        return user;
    }

    //Methode -find user by id for Admin
    public User getUserIdByAdmin(Integer id){
        return userRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + id + " not found !!!"));
    }

    //Method -create user
    public CreateUserDTO createUserByAdmin(User user){
        // เช็คว่า username ซ้ำหรือไม่
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        // เช็คว่า email ซ้ำหรือไม่
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        User addUser = new User();
        addUser.setUserId(user.getUserId());
        addUser.setUsername(user.getUsername());
        addUser.setName(user.getName());
        addUser.setPassword(passwordEncoder.encode(user.getPassword()));
        addUser.setEmail(user.getEmail());
        addUser.setPhone(user.getPhone());
        addUser.setRole(user.getRole());
        return modelMapper.map(userRepository.save(addUser), CreateUserDTO.class);
    }

    //Method -Delete user By admin
    public String deleteUserByAdmin(Integer userId) {
        // ตรวจสอบว่ามี Dormitory ที่ User เป็นเจ้าของหรือไม่
        List<Dormitory> ownedDormitories = dormitoryRepository.findByUserId(userId);

        if (!ownedDormitories.isEmpty()) {
            throw new BadRequestException("User with ID " + userId + " still owns dormitories. Transfer ownership before deletion.");
        }

        // ตรวจสอบว่ามี User อยู่จริงก่อนลบ
        if (userRepository.existsByUserId(userId)) {
            userRepository.deleteByUserId(userId);
            return "User with ID " + userId + " has been deleted successfully!";
        } else {
            throw new ResourceNotFoundException("User with ID " + userId + " does not exist!");
        }
    }
}
