package prototype.javabot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import prototype.javabot.model.ContentIdea;
import prototype.javabot.model.IdeaStatus;
import prototype.javabot.service.ContentService;

import java.util.List;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;

    @GetMapping
    public List<ContentIdea> getAll() {
        return contentService.findAll();
    }

    @PostMapping
    public ContentIdea create(@RequestParam String prompt, @RequestParam String response) {
        return contentService.save(prompt, response);
    }

    @PutMapping("/{id}/status")
    public ContentIdea updateStatus(@PathVariable Long id, @RequestParam IdeaStatus status) {
        return contentService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contentService.delete(id);
    }
}
