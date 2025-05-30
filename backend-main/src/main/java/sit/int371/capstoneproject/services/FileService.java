package sit.int371.capstoneproject.services;


import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import sit.int371.capstoneproject.dtos.FileUploadReturnDTO;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.BadRequestException;
import sit.int371.capstoneproject.exceptions.InternalServerException;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.FileRepository;
import sit.int371.capstoneproject.repositories.UserRepository;
import sit.int371.capstoneproject.util.UUIDv7;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    private final String uploadDir = "cap-file-upload";
//    private final String baseUrl = "http://localhost:8080/api/images";
    private final String baseUrl = "https://kmutt-compare.sit.kmutt.ac.th/api/images";
    private final Tika tika = new Tika();
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserRepository userRepository;

    private boolean isValidImageType(String mimeType) {
        return mimeType != null && (
                mimeType.equals("image/jpeg") ||
                        mimeType.equals("image/png") ||
                        mimeType.equals("image/gif") ||
                        mimeType.equals("image/bmp") ||
                        mimeType.equals("image/webp")
        );
    }

    public ResponseEntity<Resource> getImage(String fileId){
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileId).normalize();
            Resource resource = new UrlResource(filePath.toUri());

        if(!resource.exists()){
            throw new ResourceNotFoundException("Image not found!!!");
        }
        String contentType = tika.detect(filePath);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"").contentType(MediaType.parseMediaType(contentType)).body(resource);

        }catch (IOException e){
            throw new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    //Method upload images
    public List<FileUploadReturnDTO> uploadImages(List<MultipartFile> multipartFileList) throws BadRequestException {
        ArrayList<FileUploadReturnDTO> fileUploadReturnDTOList = new ArrayList<>();

        // ดึงข้อมูล userId จาก SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName(); // ใช้ username หรือ email จาก Authentication
        User user = userRepository.findByUsername(username) // หรือใช้ findByEmail ขึ้นอยู่กับการออกแบบ
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Integer userId = user.getUserId();   // ดึง userId

        try {
            File directory = new File(uploadDir);
            if(!directory.exists()){
                directory.mkdirs();
            }
            for (MultipartFile multipartFile: multipartFileList){
                // ตรวจสอบว่าไฟล์ individual ไม่เป็น null และไม่ว่าง
                if (multipartFile == null || multipartFile.isEmpty()) {
                    throw new BadRequestException("One or more files are null or empty");
                }
                // ตรวจสอบประเภทของไฟล์ (MIME Type)
                String mimeType = multipartFile.getContentType();
                if (!isValidImageType(mimeType)) {
                    throw new BadRequestException("Invalid file type. Only JPEG, GIF, PNG, BMP and WEBP are allowed.");
                }

                String generateFileName = String.valueOf(UUIDv7.randomUUID());
                Path filePath = Path.of(uploadDir, generateFileName);
                Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                String uploadDate = String.valueOf(LocalDateTime.now());

                // จัดการ database ทั้งหมด
                if (!userRepository.existsByUserId(userId)) {
                    throw new ResourceNotFoundException("User id " + userId + " not found!!!");
                }

                sit.int371.capstoneproject.entities.File fileEntity = new sit.int371.capstoneproject.entities.File();
                fileEntity.setFileId(generateFileName);
                fileEntity.setFileName(multipartFile.getOriginalFilename());
                fileEntity.setUploadDate(uploadDate);
                fileEntity.setUserId(userId);
                fileRepository.save(fileEntity);

                fileUploadReturnDTOList.add(new FileUploadReturnDTO(generateFileName, multipartFile.getOriginalFilename(), uploadDate, baseUrl + "/" + generateFileName));
            }
            return fileUploadReturnDTOList;
        } catch (IOException e) {
            throw new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public String deleteImage(String id) {
    if (fileRepository.existsByFileId(id)) {
        try {
            Path filePath = Path.of(uploadDir, id); // Path ไปโฟลเดอร์ cap-file-upload
            if (Files.exists(filePath)) { // ตรวจสอบว่าไฟล์มีอยู่
                Files.delete(filePath);
            } else {
                throw new ResourceNotFoundException("File Id: " + id + " does not exist in the folder!");
            }
            fileRepository.deleteByFileId(id); // ลบข้อมูลจาก database
            return "File ID: " + id + " has been deleted successfully!";
        } catch (IOException e) {
            throw new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file: " + e.getMessage());
        }
        } else {
            throw new ResourceNotFoundException("File ID " + id + " does not exist!");
        }
    }
}
