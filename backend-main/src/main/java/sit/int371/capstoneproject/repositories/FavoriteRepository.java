package sit.int371.capstoneproject.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import sit.int371.capstoneproject.entities.Dormitory;
import sit.int371.capstoneproject.entities.Favorite;
import sit.int371.capstoneproject.entities.User;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends MongoRepository<Favorite, ObjectId> {
    Optional<Favorite> findByFavId(Integer id);

    boolean existsFavoriteByDormId(Integer dormId);
    void deleteFavoriteByDormId(Integer dormId);

    Optional<Favorite> findTopByOrderByFavIdDesc();

    //find all favorites when user login
    List<Favorite> findByUserId(Integer userId);
}
