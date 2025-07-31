package prototype.javabot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String prompt;
    @Column(columnDefinition = "TEXT")
    private String response;

    @Enumerated(EnumType.STRING)
    private IdeaStatus status;

    private LocalDateTime createdAt;
}
