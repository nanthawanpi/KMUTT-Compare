package sit.int371.capstoneproject.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import sit.int371.capstoneproject.entities.Favorite;
import sit.int371.capstoneproject.entities.Form;

import java.util.List;
import java.util.Optional;

public interface FormRepository extends MongoRepository<Form, ObjectId> {
    Optional<Form> findByFormId(Integer id);
    List<Form> findByUserId(Integer userId); // + find all forms when user login
    boolean existsByFormId(Integer id);

    void deleteByFormId(Integer id);

    //generate form id
    Optional<Form> findTopByOrderByFormIdDesc();

    //Rating
    List<Form> findByDormId(int dormId);
}
