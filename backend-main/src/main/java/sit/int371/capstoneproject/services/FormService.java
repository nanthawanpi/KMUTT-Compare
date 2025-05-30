package sit.int371.capstoneproject.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sit.int371.capstoneproject.dtos.FormCreateDTO;
import sit.int371.capstoneproject.dtos.FormDTO;
import sit.int371.capstoneproject.dtos.FormUpdateDTO;
import sit.int371.capstoneproject.dtos.UserOwnerDTO;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.Form;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.mails.EmailService;
import sit.int371.capstoneproject.repositories.DormitoryRepository;
import sit.int371.capstoneproject.repositories.FormRepository;
import sit.int371.capstoneproject.repositories.UserRepository;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FormService {
    @Autowired
    private FormRepository formRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Autowired
    private EmailService emailService;

    //Methode -get all forms
    public List<FormDTO> getAllForms() {
        // ดึงข้อมูลทั้งหมดจาก Form collection
        List<Form> forms = formRepository.findAll();
        if(forms.isEmpty()){
            throw new ResourceNotFoundException("Forms not found!");
        }

        List<FormDTO> formDTOList = new ArrayList<>();
        for (Form form : forms) {
            FormDTO formDTO = new FormDTO();
            BeanUtils.copyProperties(form, formDTO); // คัดลอกข้อมูลทั่วไปจาก Form

            // ดึง username, phone, email จาก User collection
            userRepository.findByUserId(form.getUserId())
                    .ifPresent(user -> {
                        formDTO.setName(user.getName());
                        formDTO.setPhone(user.getPhone());
                        formDTO.setEmail(user.getEmail());
                    });

            // ดึง dormName และ dormAddress จาก Dormitory collection
            dormitoryRepository.findByDormId(form.getDormId())
                    .ifPresent(dorm -> {
                        formDTO.setDormName(dorm.getDormName());
                        formDTO.setAddress(dorm.getAddress());
                    });

            //สร้าง ownerDTO และดึงข้อมูล user
            userRepository.findByUserId(form.getUserId()).ifPresent(user -> {
                UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
                userOwnerDTO.setUserId(user.getUserId());
                userOwnerDTO.setName(user.getName());
                userOwnerDTO.setEmail(user.getEmail());
                userOwnerDTO.setPhone(user.getPhone());

                formDTO.setOwner(userOwnerDTO); // เพิ่ม owner ลงใน FormDTO
            });

            formDTOList.add(formDTO); // เพิ่มข้อมูลในรายการ
        }
        return formDTOList; // คืนค่ารายการของ FormDTO
    }

    //Methode -get all forms
    public List<FormDTO> getAllFormsByUser() {
        // ดึงข้อมูลทั้งหมดจาก Form collection
        List<Form> forms = formRepository.findAll();
        if (forms.isEmpty()) {
            throw new ResourceNotFoundException("Forms not found!");
        }

        List<FormDTO> formDTOList = new ArrayList<>();
        for (Form form : forms) {
            FormDTO formDTO = new FormDTO();
            BeanUtils.copyProperties(form, formDTO); // คัดลอกข้อมูลจาก Form

            //สร้าง ownerDTO และดึงข้อมูล user
            userRepository.findByUserId(form.getUserId()).ifPresent(user -> {
                UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
                userOwnerDTO.setUserId(user.getUserId());
                userOwnerDTO.setName(user.getName());
                userOwnerDTO.setEmail(user.getEmail());
                userOwnerDTO.setPhone(user.getPhone());

                formDTO.setOwner(userOwnerDTO); // เพิ่ม owner ลงใน FormDTO
            });

            //ดึง dormName และ dormAddress จาก Dormitory collection
            dormitoryRepository.findByDormId(form.getDormId()).ifPresent(dorm -> {
                formDTO.setDormName(dorm.getDormName());
                formDTO.setAddress(dorm.getAddress());
            });

            formDTOList.add(formDTO); // เพิ่มข้อมูลในรายการ
        }
        return formDTOList; // คืนค่ารายการของ FormDTO
    }


    //Methode -get form by id
    public FormDTO getFormById(Integer id) {
        Form form = formRepository.findByFormId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form id " + id + " not found !!!"));

        // แปลง Form เป็น FormDTO
        FormDTO formDTO = modelMapper.map(form, FormDTO.class);

        // ดึงข้อมูล Dormitory และข้อมูล userId จาก Dormitory
        dormitoryRepository.findByDormId(form.getDormId()).ifPresent(dorm -> {
            formDTO.setDormName(dorm.getDormName());
            formDTO.setAddress(dorm.getAddress());

            // เชื่อมโยงข้อมูล Owner จาก userId ที่อยู่ใน Dormitory
            userRepository.findByUserId(dorm.getUserId()).ifPresent(user -> {
                UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
                userOwnerDTO.setUserId(user.getUserId());
                userOwnerDTO.setName(user.getName());
                userOwnerDTO.setEmail(user.getEmail());
                userOwnerDTO.setPhone(user.getPhone());

                formDTO.setOwner(userOwnerDTO); // เพิ่ม owner ลงใน FormDTO
            });
        });

        return formDTO;
    }


    //Method -create form
    public FormCreateDTO createForm(Form form, Authentication authentication){
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
	
	//Method -update/edit form
    public FormUpdateDTO updateForm(Integer id, FormUpdateDTO formUpdateDTO, Authentication authentication){
        // ดึงข้อมูลผู้ใช้งานจาก Authentication
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found!!!"));

        // ค้นหา Form ที่ต้องการอัปเดต
        Form existingForm = formRepository.findByFormId(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " does not exist!!!"));

        // ค้นหา Dormitory จาก dormId ที่อยู่ใน form
        Dormitory dormitory = dormitoryRepository.findByDormId(existingForm.getDormId())
                .orElseThrow(() -> new ResourceNotFoundException("Dormitory with ID " + existingForm.getDormId() + " not found!!!"));

        // ค้นหา Owner (เจ้าของหอพัก) จาก userId ที่อยู่ใน dormitory
        User owner = userRepository.findByUserId(dormitory.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner with ID " + dormitory.getUserId() + " not found!!!"));

        // อัปเดตข้อมูลของ Form
        existingForm.setUserId(user.getUserId());              // อัปเดต userId ตามผู้ใช้ที่ login
        existingForm.setName(formUpdateDTO.getName());
        existingForm.setEmail(formUpdateDTO.getEmail());
        existingForm.setPhone(formUpdateDTO.getPhone());
        existingForm.setDate_in(formUpdateDTO.getDate_in());
        existingForm.setDate_out(formUpdateDTO.getDate_out());
        existingForm.setDescription(formUpdateDTO.getDescription());

        // บันทึกข้อมูล Form ที่อัปเดต
        Form updatedForm = formRepository.save(existingForm);

        // ส่งอีเมลแจ้งเตือนเมื่อมีการอัปเดต Form
        sendEmailForFormUpdate(user, updatedForm);

        // แปลง Form ที่อัปเดตแล้วเป็น FormUpdateDTO
        FormUpdateDTO updatedFormDTO = modelMapper.map(updatedForm, FormUpdateDTO.class);

        // เพิ่มข้อมูล Owner จาก Dormitory
        UserOwnerDTO userOwnerDTO = new UserOwnerDTO();
        userOwnerDTO.setUserId(owner.getUserId());
        userOwnerDTO.setName(owner.getName());
        userOwnerDTO.setEmail(owner.getEmail());
        userOwnerDTO.setPhone(owner.getPhone());

        updatedFormDTO.setOwner(userOwnerDTO); // เพิ่ม owner ลงใน DTO

        // คืนค่า FormUpdateDTO พร้อมข้อมูล Owner
        return updatedFormDTO;
    }

    //Method -delete form
    public String deleteForm(Integer id) throws AccessDeniedException {
        // ดึงข้อมูลผู้ใช้ที่ล็อกอินอยู่
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found!");
        }

        User user = userOptional.get();
        String role = String.valueOf(user.getRole());
        Integer userId = user.getUserId();

        // ตรวจสอบว่า form มีอยู่จริงหรือไม่
        Form form = formRepository.findByFormId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form with Id " + id + " does not exist!!!"));
        // ตรวจสอบสิทธิ์ในการลบ
        if (!"admin".equals(role) && !userId.equals(form.getUserId())) {
            throw new AccessDeniedException("You do not have permission to delete this form!");
        }

        // ลบ form
        formRepository.deleteByFormId(id);
        return "Form with Id " + id + " has been deleted successfully!";
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
}
