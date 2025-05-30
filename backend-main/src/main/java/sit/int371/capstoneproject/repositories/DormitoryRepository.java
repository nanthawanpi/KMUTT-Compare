package sit.int371.capstoneproject.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import sit.int371.capstoneproject.entities.Dormitory;

import java.util.List;
import java.util.Optional;


public interface DormitoryRepository extends MongoRepository<Dormitory, ObjectId> {
    Optional<Dormitory> findByDormId(Integer id);
    boolean existsByDormId(Integer id);
    void deleteByDormId(Integer id);

    //generate dormitory id
    Optional<Dormitory> findTopByOrderByDormIdDesc();

    //find all dormitories when user login
    List<Dormitory> findByUserId(Integer userId);

    // count all dormitories
    long count();

    //Match Email:: Transfers Dormitories
    List<Dormitory> findByUserIdAndDormIdIn(int userId, List<Integer> dormIds);
}
