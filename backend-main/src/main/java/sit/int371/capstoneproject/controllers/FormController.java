package sit.int371.capstoneproject.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sit.int371.capstoneproject.autoId.SequenceGenerateFormService;
import sit.int371.capstoneproject.configs.JwtUtil;
import sit.int371.capstoneproject.dtos.FormCreateDTO;
import sit.int371.capstoneproject.dtos.FormDTO;
import sit.int371.capstoneproject.dtos.FormUpdateDTO;
import sit.int371.capstoneproject.entities.Form;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.UserRepository;
import sit.int371.capstoneproject.services.FormService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173","http://cp24kk2.sit.kmutt.ac.th","https://kmutt-compare.sit.kmutt.ac.th"})
@RequestMapping("/api/forms")
public class FormController {
    @Autowired
    private FormService formService;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SequenceGenerateFormService sequenceGenerateFormService;

    // Get All Form
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public ResponseEntity<List<FormDTO>> getAllForms() {
        List<FormDTO> forms = formService.getAllForms();
        if (forms.isEmpty()) {
            throw new ResourceNotFoundException("Forms not found!");
        }
        return ResponseEntity.ok(forms);
    }

    // Get All Form
    @GetMapping("/receive-form")
    public ResponseEntity<List<FormDTO>> getAllFormsByUser() {
        List<FormDTO> forms = formService.getAllFormsByUser();
        return ResponseEntity.ok(forms);
    }

    // Get Form By id
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{formId}")
    public ResponseEntity<FormDTO> getFormById(@PathVariable Integer formId) {
        FormDTO formDTO = formService.getFormById(formId);
        return ResponseEntity.ok(formDTO);
    }

    //Create Form
    @PostMapping("")
    public FormCreateDTO createdForm(@Valid @RequestBody FormCreateDTO formCreateDTO, Authentication authentication) {
        // Generate formId และ map DTO ไปเป็น Entity
        Form form = modelMapper.map(formCreateDTO, Form.class);
        form.setFormId((int) sequenceGenerateFormService.generateSequence(Form.SEQUENCE_NAME));
        FormCreateDTO createdForm = formService.createForm(form, authentication);
        return createdForm;
    }

	//Update Form
    @PutMapping("/{formId}")
    public FormUpdateDTO updatedForm(@PathVariable Integer formId, @Valid @RequestBody FormUpdateDTO formUpdateDTO, Authentication authentication){
        return formService.updateForm(formId, formUpdateDTO, authentication);
    }

    //Delete Form
    @DeleteMapping("/{formId}")
    public ResponseEntity<String> deletedForm(@PathVariable Integer formId) throws AccessDeniedException {
        String message = formService.deleteForm(formId);
        return ResponseEntity.ok(message);
    }
}
