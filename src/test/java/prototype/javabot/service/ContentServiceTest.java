package prototype.javabot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prototype.javabot.model.ContentIdea;
import prototype.javabot.model.IdeaStatus;
import prototype.javabot.repository.ContentIdeaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentIdeaRepository repository;

    @InjectMocks
    private ContentService contentService;

    private ContentIdea testIdea;

    @BeforeEach
    void setUp() {
        testIdea = ContentIdea.builder()
                .id(1L)
                .prompt("Test prompt")
                .response("Test response")
                .status(IdeaStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_ShouldCreateAndSaveContentIdea() {
        // Given
        String prompt = "Test prompt";
        String response = "Test response";

        when(repository.save(any(ContentIdea.class))).thenReturn(testIdea);

        // When
        ContentIdea result = contentService.save(prompt, response);

        // Then
        assertNotNull(result);
        assertEquals(testIdea.getId(), result.getId());
        assertEquals(testIdea.getPrompt(), result.getPrompt());
        assertEquals(testIdea.getResponse(), result.getResponse());
        assertEquals(IdeaStatus.DRAFT, result.getStatus());

        verify(repository).save(any(ContentIdea.class));
    }

    @Test
    void findAll_ShouldReturnAllIdeas() {
        // Given
        List<ContentIdea> expectedIdeas = Arrays.asList(testIdea);
        when(repository.findAll()).thenReturn(expectedIdeas);

        // When
        List<ContentIdea> result = contentService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testIdea.getId(), result.get(0).getId());

        verify(repository).findAll();
    }

    @Test
    void findById_ShouldReturnIdea_WhenExists() {
        // Given
        Long ideaId = 1L;
        when(repository.findById(ideaId)).thenReturn(Optional.of(testIdea));

        // When
        ContentIdea result = contentService.findById(ideaId);

        // Then
        assertNotNull(result);
        assertEquals(testIdea.getId(), result.getId());

        verify(repository).findById(ideaId);
    }

    @Test
    void findById_ShouldReturnNull_WhenNotExists() {
        // Given
        Long ideaId = 999L;
        when(repository.findById(ideaId)).thenReturn(Optional.empty());

        // When
        ContentIdea result = contentService.findById(ideaId);

        // Then
        assertNull(result);

        verify(repository).findById(ideaId);
    }

    @Test
    void updateStatus_ShouldChangeIdeaStatus() {
        // Given
        Long ideaId = 1L;
        IdeaStatus newStatus = IdeaStatus.PUBLISHED;

        ContentIdea updatedIdea = ContentIdea.builder()
                .id(ideaId)
                .prompt(testIdea.getPrompt())
                .response(testIdea.getResponse())
                .status(newStatus)
                .createdAt(testIdea.getCreatedAt())
                .build();

        when(repository.findById(ideaId)).thenReturn(Optional.of(testIdea));
        when(repository.save(any(ContentIdea.class))).thenReturn(updatedIdea);

        // When
        ContentIdea result = contentService.updateStatus(ideaId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());

        verify(repository).findById(ideaId);
        verify(repository).save(any(ContentIdea.class));
    }

    @Test
    void updateStatus_ShouldThrowException_WhenIdeaNotFound() {
        // Given
        Long ideaId = 999L;
        IdeaStatus newStatus = IdeaStatus.PUBLISHED;

        when(repository.findById(ideaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            contentService.updateStatus(ideaId, newStatus);
        });

        verify(repository).findById(ideaId);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // Given
        Long ideaId = 1L;

        // When
        contentService.delete(ideaId);

        // Then
        verify(repository).deleteById(ideaId);
    }
}