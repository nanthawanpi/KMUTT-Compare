package sit.int371.capstoneproject.services;

import org.bson.types.Decimal128;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sit.int371.capstoneproject.dtos.DormitoryDTO;
import sit.int371.capstoneproject.dtos.FavDormDTO;
import sit.int371.capstoneproject.dtos.FavoriteDTO;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.Favorite;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.DormitoryRepository;
import sit.int371.capstoneproject.repositories.FavoriteRepository;
import sit.int371.capstoneproject.repositories.UserRepository;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DormitoryRepository dormitoryRepository;
    @Autowired
    private ModelMapper modelMapper;


    //Method -find all favorites
    @PreAuthorize("hasRole('admin')")
    public List<FavDormDTO> getAllFavorites() {
        List<Favorite> favorites = favoriteRepository.findAll();

        if (favorites.isEmpty()) {
            throw new ResourceNotFoundException("Favorites not found!");
        }

        // แปลง List<Favorite> เป็น List<FavDormDTO>
        return favorites.stream().map(this::convertToFavDormDTO).collect(Collectors.toList());
    }

    //Method -find favorite by id
    @PreAuthorize("hasRole('admin')")
    public FavDormDTO getFavById(Integer id){
        Favorite favorite = favoriteRepository.findByFavId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite id " + id + " not found !!!"));

        return convertToFavDormDTO(favorite);
    }

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

    //Method -create favorite
    public FavoriteDTO createFav(Favorite favorite){
        // ดึง userId จาก SecurityContext หรือ Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // หรือสามารถดึง username อื่นๆ ที่เกี่ยวข้องกับผู้ใช้งาน
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found!");
        }
        User user = userOptional.get();
        Integer userId = user.getUserId(); // หรือค่าที่เหมาะสมจาก User

        // ตรวจสอบว่า dormId และ userId เป็น 0 หรือไม่
        List<String> errors = new ArrayList<>();
        if (!dormitoryRepository.existsByDormId(favorite.getDormId())) {
            errors.add("Dorm id " + favorite.getDormId() + " not found");
        }

        Favorite addFav = new Favorite();
        addFav.setFavId(favorite.getFavId());
        addFav.setDormId(favorite.getDormId());
        addFav.setUserId(userId);
        return modelMapper.map(favoriteRepository.save(addFav), FavoriteDTO.class);
    }

    //Method -update favorite
    public FavoriteDTO updateFav(Integer id, FavoriteDTO favoriteDTO) throws AccessDeniedException {
        // ดึง userId จาก SecurityContext หรือ Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // หรือสามารถดึง username อื่นๆ ที่เกี่ยวข้องกับผู้ใช้งาน
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found!");
        }
        User user = userOptional.get();
        Integer userId = user.getUserId(); // หรือค่าที่เหมาะสมจาก User
        String role = String.valueOf(user.getRole()); // ดึง role จาก User

        // ค้นหา favorite ที่มีอยู่แล้ว
        Favorite exitsFav = favoriteRepository.findByFavId(id).orElseThrow(
                () -> new ResourceNotFoundException(id + " does not exist!"));
        // ตรวจสอบว่า user มีสิทธิ์ในการอัพเดต favorite
        if (!role.equals("admin") && exitsFav.getUserId() != userId) {
            throw new AccessDeniedException("You do not have permission to update this favorite!");
        }
        // ตรวจสอบว่า dormId และ userId เป็น 0 หรือไม่
        List<String> errors = new ArrayList<>();
        if (!dormitoryRepository.existsByDormId(favoriteDTO.getDormId())) {
            errors.add("Dorm id " + favoriteDTO.getDormId() + " not found");
        }
        if (!userRepository.existsByUserId(userId)) {
            errors.add("User id " + userId + " not found");
        }
        // หากมีข้อผิดพลาดใดๆ ให้โยน exception พร้อมข้อความรวม
        if (!errors.isEmpty()) {
            throw new ResourceNotFoundException(String.join(", ", errors));
        }

        // ให้คง userId ของผู้ที่สร้างไว้เดิม
        exitsFav.setDormId(favoriteDTO.getDormId());
        return modelMapper.map(favoriteRepository.save(exitsFav), FavoriteDTO.class);
    }

    //Method -delete favorite by dorm Id
    public String deleteFavByDormId(Integer dormId){
        if (favoriteRepository.existsFavoriteByDormId(dormId)){
            favoriteRepository.deleteFavoriteByDormId(dormId);
            return "Favorite with Dormitory ID " + dormId + " has been deleted successfully!";
        }else {
            throw new ResourceNotFoundException("Dormitory Id in Favorite " + dormId + " dose not exited!!!");
        }
    }

    // Convert Method - Convert Favorite to FavDormDTO
    private FavDormDTO convertToFavDormDTO(Favorite favorite) {
        // แปลง Favorite เป็น FavDormDTO
        FavDormDTO favDormDTO = modelMapper.map(favorite, FavDormDTO.class);

        Optional<Dormitory> dormitoryOptional = dormitoryRepository.findByDormId(favorite.getDormId());
        if(dormitoryOptional.isPresent()){
            Dormitory dormitory = dormitoryOptional.get();
            favDormDTO.setDormName(dormitory.getDormName());
            favDormDTO.setSize(dormitory.getSize());
            favDormDTO.setMin_price(dormitory.getMin_price());
            favDormDTO.setMax_price(dormitory.getMax_price());
            favDormDTO.setDistance(dormitory.getDistance());
//            favDormDTO.setScore(dormitory.getScore());
        }else {
            //กรณี dormId นั้นๆถูกลบ ทำให้สามารถแสดงข้อมูลของ favorite ได้
            favDormDTO.setDormName("This Dormitory out of service, now!");
            favDormDTO.setSize(new Decimal128(0));
            favDormDTO.setMin_price(new Decimal128(0));
            favDormDTO.setMax_price(new Decimal128(0));
            favDormDTO.setDistance(new Decimal128(0));
        }
        return favDormDTO;
    }
}