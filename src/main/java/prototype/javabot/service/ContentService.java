package prototype.javabot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import prototype.javabot.model.ContentIdea;
import prototype.javabot.model.IdeaStatus;
import prototype.javabot.repository.ContentIdeaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentIdeaRepository repository;

    public ContentIdea save(String prompt, String response) {
        try {
            ContentIdea idea = ContentIdea.builder()
                    .prompt(prompt)
                    .response(response)
                    .status(IdeaStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .build();

            ContentIdea savedIdea = repository.save(idea);
            log.info("Контент сохранен с ID: {}", savedIdea.getId());
            return savedIdea;

        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при сохранении контента: {}", e.getMessage());

            // Попробуем сохранить с обрезанным текстом как fallback
            try {
                String truncatedPrompt = prompt.length() > 4000 ? prompt.substring(0, 4000) + "..." : prompt;
                String truncatedResponse = response.length() > 9000 ? response.substring(0, 9000) + "..." : response;

                ContentIdea idea = ContentIdea.builder()
                        .prompt(truncatedPrompt)
                        .response(truncatedResponse)
                        .status(IdeaStatus.DRAFT)
                        .createdAt(LocalDateTime.now())
                        .build();

                ContentIdea savedIdea = repository.save(idea);
                log.warn("Контент сохранен с обрезанным текстом, ID: {}", savedIdea.getId());
                return savedIdea;

            } catch (Exception fallbackException) {
                log.error("Не удалось сохранить даже обрезанный контент: {}", fallbackException.getMessage());
                throw new RuntimeException("Не удалось сохранить контент", fallbackException);
            }
        }
    }

    public List<ContentIdea> findAll() {
        return repository.findAll();
    }

    public ContentIdea findById(Long ideaId) {
        return repository.findById(ideaId).orElse(null);
    }


    public void delete(Long id) {
        repository.deleteById(id);
    }

    public ContentIdea updateStatus(Long id, IdeaStatus newStatus) {
        ContentIdea idea = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Идея не найдена"));
        idea.setStatus(newStatus);
        return repository.save(idea);
    }
}