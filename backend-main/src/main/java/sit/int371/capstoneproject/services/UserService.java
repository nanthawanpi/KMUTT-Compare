package sit.int371.capstoneproject.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.int371.capstoneproject.dtos.*;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.Form;
import sit.int371.capstoneproject.entities.FormUpdateStatusDTO;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.BadRequestException;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.exceptions.UnauthorizedException;
import sit.int371.capstoneproject.mails.EmailService;
import sit.int371.capstoneproject.repositories.DormitoryRepository;
import sit.int371.capstoneproject.repositories.FavoriteRepository;
import sit.int371.capstoneproject.repositories.FormRepository;
import sit.int371.capstoneproject.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FormRepository formRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private DormitoryRepository dormitoryRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;

    //Methode -Info user when user login
    public UserMeDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for username: " + username));
        return modelMapper.map(user, UserMeDTO.class);
    }

    //Method -create user
    public CreateUserDTO createUser(User user){
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

    //Method -update user
    public UserDTO updateUser(UserDTO userDTO){
        // ดึง userId จาก SecurityContext หรือ Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // หรือสามารถดึง username อื่นๆ ที่เกี่ยวข้องกับผู้ใช้งาน
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found!");
        }
        User exitsUser = userOptional.get();
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

        // อัพเดทข้อมูลผู้ใช้
        exitsUser.setUsername(userDTO.getUsername());
        exitsUser.setName(userDTO.getName());
        exitsUser.setEmail(userDTO.getEmail());
        exitsUser.setPhone(userDTO.getPhone());
        User updatedUser = userRepository.save(exitsUser);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    // Matching Password Method
    public String matchPassword(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            // ถ้าไม่พบ username
            throw new ResourceNotFoundException("User not found!");
        }

        User user = userOptional.get();
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            // ถ้ารหัสผ่านตรง
            return "Password Matched!";
        } else {
            // ถ้ารหัสผ่านไม่ตรง
            return "Incorrect password!";
        }
    }

    //Method -delete user (username)
    public String deleteUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
        }
        User user = userOptional.get();
        Integer userId = user.getUserId();

        // ตรวจสอบว่า userId นี้เป็นเจ้าของหอพักหรือไม่
        List<Dormitory> ownedDorms = dormitoryRepository.findByUserId(userId);
        if (!ownedDorms.isEmpty()) {
            throw new BadRequestException("Cannot delete user: This user owns one or more dormitories, Should to Transfers dormitory! or Delete your dormitory!");
        }

        userRepository.deleteByUserId(userId);
        return "User with ID " + userId + " has been deleted successfully!";
    }


    // Matching Email Method
    public void updateUserIdInDorms(String email, int userId, List<Integer> dormIds) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        int newUserId = user.getUserId();
        // ค้นหา Dormitory ที่ currentUserId เป็นเจ้าของ และ dormId อยู่ในลิสต์ที่ผู้ใช้ส่งมา
        List<Dormitory> dormitories = dormitoryRepository.findByUserIdAndDormIdIn(userId, dormIds);
        if (dormitories.isEmpty()) {
            throw new ResourceNotFoundException("No dormitories found for the current user with the specified dormIds.");
        }
        // อัปเดต userId ใหม่ใน Dormitory ที่เลือก
        for (Dormitory dorm : dormitories) {
            dorm.setUserId(newUserId);
        }
        // บันทึกข้อมูลที่อัปเดตลงในฐานข้อมูล
        dormitoryRepository.saveAll(dormitories);
    }

//  ---------------------------- Forms ----------------------------------
    //  Method -Get form(s) user
    public List<FormDTO> getFormsByUserId(int userId) {
        // ค้นหาฟอร์มที่เชื่อมโยงกับ userId
        List<Form> formList = formRepository.findByUserId(userId);

        // แปลงฟอร์มจาก Form entity เป็น FormDTO พร้อมดึงข้อมูลที่เชื่อมโยง
        return formList.stream()
                .map(form -> {
                    // แปลง Form เป็น FormDTO
                    FormDTO formDTO = modelMapper.map(form, FormDTO.class);

                    // ดึงข้อมูล Dormitory ที่เกี่ยวข้อง
                    dormitoryRepository.findByDormId(form.getDormId()).ifPresent(dorm -> {
                        formDTO.setDormName(dorm.getDormName());
                        formDTO.setAddress(dorm.getAddress());

                        // ดึงข้อมูล Owner (เจ้าของหอพัก) จาก dormitory
                        userRepository.findByUserId(dorm.getUserId()).ifPresent(owner -> {
                            UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
                            userOwnerDTO.setUserId(owner.getUserId());
                            userOwnerDTO.setName(owner.getName());
                            userOwnerDTO.setEmail(owner.getEmail());
                            userOwnerDTO.setPhone(owner.getPhone());

                            formDTO.setOwner(userOwnerDTO); // เพิ่ม owner ลงใน FormDTO
                        });
                    });

                    return formDTO; // คืนค่าฟอร์มที่แปลงแล้ว
                })
                .collect(Collectors.toList()); // สะสมใน List ของ FormDTO
    }

    //Methode -Get form by id
    public FormDTO getFormByFormId(Integer id) {
        Form form = formRepository.findByFormId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form id " + id + " not found !!!"));

        // แปลง Form เป็น FormDTO
        FormDTO formDTO = modelMapper.map(form, FormDTO.class);

        // ดึงข้อมูล Dormitory ที่เกี่ยวข้อง
        dormitoryRepository.findByDormId(form.getDormId()).ifPresent(dorm -> {
            formDTO.setDormName(dorm.getDormName());
            formDTO.setAddress(dorm.getAddress());

            // ดึงข้อมูล Owner (เจ้าของหอพัก) จาก dormitory
            userRepository.findByUserId(dorm.getUserId()).ifPresent(owner -> {
                UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
                userOwnerDTO.setUserId(owner.getUserId());
                userOwnerDTO.setName(owner.getName());
                userOwnerDTO.setEmail(owner.getEmail());
                userOwnerDTO.setPhone(owner.getPhone());

                formDTO.setOwner(userOwnerDTO); // เพิ่ม owner ลงใน FormDTO
            });
        });

        return formDTO;
    }

    //Methode -Info user when user(owner) login
    public List<FormDTO> getUserReceivedForms(Authentication authentication) {
        // ดึงข้อมูลผู้ใช้งานจาก Authentication (ข้อมูลของผู้ที่ login)
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found"));

        // ดึงรายการ dormitory ที่ user เป็นเจ้าของ
        List<Dormitory> ownedDormitories = dormitoryRepository.findByUserId(user.getUserId());
        if (ownedDormitories.isEmpty()) {
            throw new ResourceNotFoundException("You are not the owner of any dormitories.");
        }

        // ++เพิ่มเข้ามา ดึง dormId ทั้งหมดที่ user เป็นเจ้าของ
        List<Integer> ownedDormIds = ownedDormitories.stream()
                .map(Dormitory::getDormId)
                .collect(Collectors.toList());

        // ++เพิ่มเข้ามา ค้นหาเฉพาะฟอร์มที่มี dormId ตรงกับหอพักที่เจ้าของเป็นเจ้าของ
        List<Form> forms = formRepository.findAll().stream()
                .filter(form -> ownedDormIds.contains(form.getDormId()))
                .collect(Collectors.toList());        if (forms.isEmpty()) {
            throw new ResourceNotFoundException("No forms received for your dormitories.");
        }

        List<FormDTO> formDTOList = new ArrayList<>();
        for (Form form : forms) {
            FormDTO formDTO = new FormDTO();
            BeanUtils.copyProperties(form, formDTO); // คัดลอกข้อมูลจาก Form

            // ดึงข้อมูล Dormitory และ Owner
            dormitoryRepository.findByDormId(form.getDormId()).ifPresent(dorm -> {
                formDTO.setDormName(dorm.getDormName());
                formDTO.setAddress(dorm.getAddress());

                // ใส่ข้อมูลเจ้าของหอพัก (owner)
                UserOwnerDTO ownerDTO = new UserOwnerDTO();
                ownerDTO.setUserId(user.getUserId());
                ownerDTO.setName(user.getName());
                ownerDTO.setEmail(user.getEmail());
                ownerDTO.setPhone(user.getPhone());
                formDTO.setOwner(ownerDTO);
            });

            formDTOList.add(formDTO);
        }
        return formDTOList;
    }

//  Method -Create Form(s) by user
    public FormCreateDTO createFormByUser(Form form, Authentication authentication){
        // ดึง userId จาก Authentication ซึ่งเป็นข้อมูลของผู้ใช้งานที่ login
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found!!!"));

        // ค้นหา Dormitory จาก dormId ที่ผู้ใช้ส่งมา
        Dormitory dormitory = dormitoryRepository.findByDormId(form.getDormId())
                .orElseThrow(() -> new ResourceNotFoundException("Dormitory ID " + form.getDormId() + " not found!!!"));

        // ดึง userId ของเจ้าของหอพักจาก Dormitory
        int ownerId = dormitory.getUserId(); // สมมติว่า Dormitory มี field `userId`

        // 3. ค้นหาข้อมูลเจ้าของหอพัก
        User owner = userRepository.findByUserId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner userId " + ownerId + " not found!!!"));

        // 4. บันทึกข้อมูล Form
        Form addForm = new Form();
        addForm.setFormId(form.getFormId());
        addForm.setUserId(user.getUserId());  // ใช้ userId จาก user ที่ login
        addForm.setName(form.getName());
        addForm.setEmail(form.getEmail());
        addForm.setPhone(form.getPhone());
        addForm.setDate_in(form.getDate_in());
        addForm.setDate_out(form.getDate_out());
        addForm.setDescription(form.getDescription());
        addForm.setDormId(form.getDormId());

        Form saveForm = formRepository.save(addForm);

        // ส่งอีเมลแจ้งเตือน
        sendEmailForFormCreation(user, saveForm);

        // 5. แปลงข้อมูล Form เป็น FormCreateDTO
        FormCreateDTO formCreateDTO = modelMapper.map(saveForm, FormCreateDTO.class);

        // 6. ใส่ข้อมูลเจ้าของหอพักลงใน OwnerDTO
        UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
        userOwnerDTO.setUserId(owner.getUserId());   // ข้อมูลเจ้าของหอพัก
        userOwnerDTO.setName(owner.getName());
        userOwnerDTO.setEmail(owner.getEmail());
        userOwnerDTO.setPhone(owner.getPhone());

        formCreateDTO.setOwner(userOwnerDTO); // ตั้งค่า owner ใน FormCreateDTO

        return formCreateDTO;
    }

    //Method -update/edit form status
    public FormUpdateDTO updateFormByUser(Integer id, FormUpdateStatusDTO formUpdateStatusDTO, Authentication authentication){
        // ดึงข้อมูลผู้ใช้งานจาก Authentication (ผู้ที่ login)
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found!!!"));

        // ค้นหา Form ที่ต้องการอัปเดต
        Form existingForm = formRepository.findByFormId(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " does not exist!!!"));

        // ค้นหา Dormitory ที่เกี่ยวข้อง
        Dormitory dormitory = dormitoryRepository.findByDormId(existingForm.getDormId())
                .orElseThrow(() -> new ResourceNotFoundException("Dormitory with ID " + existingForm.getDormId() + " not found!!!"));

        // ค้นหา Owner ของ Dormitory
        User owner = userRepository.findByUserId(dormitory.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner with ID " + dormitory.getUserId() + " not found!!!"));

        // ตรวจสอบว่า user ที่ล็อกอินต้องเป็น คนที่ส่งฟอร์ม หรือเจ้าของหอพักเท่านั้น!!!
        if (user.getUserId() != existingForm.getUserId() &&
                user.getUserId() != owner.getUserId()) {
            throw new UnauthorizedException("You do not have permission to update this form");
        }

        // อัปเดตเฉพาะ Status ของ form
        existingForm.setStatus(formUpdateStatusDTO.getStatus());

        // บันทึกข้อมูล Form ที่อัปเดต
        Form updatedForm = formRepository.save(existingForm);

        // ส่งอีเมลแจ้งเตือนเมื่อมีการอัปเดต Form
        sendEmailForFormUpdate(user, updatedForm);

        // แปลง Form ที่อัปเดตแล้วเป็น FormUpdateDTO
        FormUpdateDTO updatedFormDTO = modelMapper.map(updatedForm, FormUpdateDTO.class);

        // เพิ่มข้อมูล Owner ที่ถูกต้อง //เจ้าของหอพัก
        UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
        userOwnerDTO.setUserId(owner.getUserId());
        userOwnerDTO.setName(owner.getName());
        userOwnerDTO.setEmail(owner.getEmail());
        userOwnerDTO.setPhone(owner.getPhone());

        updatedFormDTO.setOwner(userOwnerDTO); // เพิ่ม owner ลงใน DTO

        // คืนค่า FormUpdateDTO พร้อมข้อมูล Owner
        return updatedFormDTO;
    }

    // Gmail
    private void sendEmailForFormCreation(User user, Form form) {
        String subject = "New Form Submission for Dormitory Reservation";
        String body = generateEmailBody(user, form);
        emailService.sendEmail(form.getEmail(), subject, body);
    }
    private void sendEmailForFormUpdate(User user, Form form) {
        String subject = "Updated Form Submission for Dormitory Reservation";
        String body = generateEmailBody(user, form);
        emailService.sendEmail(form.getEmail(), subject, body);
    }
    private String generateEmailBody(User user, Form form) {
        String body = "Dear " + user.getUsername() + ",\n\n" +
                "A new form has been submitted for dormitory reservation. " +
                "Here are the details:\n\n" +
                "Form ID: " + form.getFormId() + "\n" +
                "Name: " + form.getName() + "\n" +
                "Phone: " + form.getPhone() + "\n" +
                "Email: " + form.getEmail() + "\n" +
                "Date In: " + form.getDate_in() + "\n" +
                "Date Out: " + form.getDate_out() + "\n" +
                "Status: " + form.getStatus() + "\n\n";

        if (form.getDescription() != null && !form.getDescription().isEmpty()) {
            body += "Description: " + form.getDescription() + "\n\n";
        }
        body += "Kind regards,\nYour Dormitory Reservation System";
        return body;
    }


//  ---------------------------- Dormitory ----------------------------------
    // Method -find dormitory by User id when login
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

//  ---------------------------- Favorite ----------------------------------
    //Method -find favorites by User id when login
    public List<FavDormDTO> getFavoritesByUserId(Integer userId) {
        return favoriteRepository.findByUserId(userId).stream().map(fav -> {
            FavDormDTO dto = modelMapper.map(fav, FavDormDTO.class);
            // ค้นหา Dormitory ตาม dormId
            Optional<Dormitory> dormitory = dormitoryRepository.findByDormId(fav.getDormId());

            // ตรวจสอบว่า Optional มีค่าหรือไม่
            if (dormitory.isPresent()) {
                Dormitory dorm = dormitory.get();
                dto.setDormName(dorm.getDormName());
                dto.setSize(dorm.getSize());
                dto.setMin_price(dorm.getMin_price());
                dto.setMax_price(dorm.getMax_price());
                dto.setDistance(dorm.getDistance());
            }
            return dto;
        }).toList();
    }
}
