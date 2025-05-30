package sit.int371.capstoneproject.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import sit.int371.capstoneproject.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByUserId(Integer id);
    Optional<User> findByUsername(String username);
    boolean existsByUserId(Integer id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteByUserId(Integer id);

    //generate staff id
    Optional<User> findTopByOrderByUserIdDesc();

    // Active users
    List<User> findByIsActive(boolean isActive);


    //Dashboard::
    // count all users
    long count();

    // ค้นหาผู้ใช้ที่ active
    List<User> findByIsActiveTrue();

    //Match email for tranfer User in Dormitory
    Optional<User> findByEmail(String email);
}
