package prototype.javabot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prototype.javabot.model.ContentIdea;

import java.util.List;
import java.util.Optional;

public interface ContentIdeaRepository extends JpaRepository<ContentIdea, Long> {


}
