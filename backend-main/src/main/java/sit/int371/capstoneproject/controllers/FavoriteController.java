package sit.int371.capstoneproject.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sit.int371.capstoneproject.configs.JwtUtil;
import sit.int371.capstoneproject.entities.User;
import sit.int371.capstoneproject.exceptions.ResourceNotFoundException;
import sit.int371.capstoneproject.repositories.UserRepository;
import sit.int371.capstoneproject.services.UserService;
import sit.int371.capstoneproject.autoId.SequenceGenerateFavService;
import sit.int371.capstoneproject.dtos.FavDormDTO;
import sit.int371.capstoneproject.dtos.FavoriteDTO;
import sit.int371.capstoneproject.entities.Favorite;
import sit.int371.capstoneproject.services.FavoriteService;


import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173","http://cp24kk2.sit.kmutt.ac.th","https://kmutt-compare.sit.kmutt.ac.th"})
@RequestMapping("/api/favorites")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private SequenceGenerateFavService sequenceGenerateFavService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    //Get All Favorites
    @PreAuthorize("hasRole('admin')")
    @GetMapping("")
    public List<FavDormDTO> getAllFavorites() {
        return favoriteService.getAllFavorites();
    }

    // Get Favorite By id
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{favId}")
    public FavDormDTO getFavById(@PathVariable Integer favId) {
        return favoriteService.getFavById(favId);
    }

    //Get Favorites when login following user:: ดึง favorite(s) ของ user ที่ล็อกอินอยู่
    @GetMapping("/user")
    public ResponseEntity<List<FavDormDTO>> getFavoritesByUser(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtUtil.extractUsername(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        List<FavDormDTO> favorites = favoriteService.getFavoritesByUserId(user.getUserId());

        if (favorites.isEmpty()) {
            throw new ResourceNotFoundException("No favorites found for this user.");
        }
        return ResponseEntity.ok(favorites);
    }

    @PostMapping()
    public FavoriteDTO createdFav(@Valid @RequestBody FavoriteDTO favoriteDTO){
        //generate dormitory id
        favoriteDTO.setFavId((int) sequenceGenerateFavService.generateSequence(Favorite.SEQUENCE_NAME));
        Favorite favorite = modelMapper.map(favoriteDTO, Favorite.class);
        return favoriteService.createFav(favorite);
    }

    @PutMapping("/{favid}")
    public FavoriteDTO updatedFav(@PathVariable Integer favid, @Valid @RequestBody FavoriteDTO favoriteDTO) throws AccessDeniedException {
        return favoriteService.updateFav(favid, favoriteDTO);
    }

    // Delete Favorite
    @DeleteMapping("/dorm/{dormId}")
    public ResponseEntity<String> deletedFavByDormId(@PathVariable Integer dormId){
        String message = favoriteService.deleteFavByDormId(dormId);
        return ResponseEntity.ok(message);
    }
}
