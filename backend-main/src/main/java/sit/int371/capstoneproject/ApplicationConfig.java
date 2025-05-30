package sit.int371.capstoneproject;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import sit.int371.capstoneproject.util.ListMapper;

@Configuration
@EnableMongoAuditing
public class ApplicationConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ListMapper listMapper(){
        return ListMapper.getInstance();
    }
}
