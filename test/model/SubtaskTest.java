package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SubtaskTest {

    @Test
    void subtaskCannotBeItsOwnEpic() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(1);

        Epic epic = new Epic("Epic Title", "Epic Description", Status.NEW);
        epic.setId(1);

        Subtask subtask = new Subtask(
                "Subtask Title",
                "Subtask Description",
                Status.NEW,
                epic.getId(),
                startTime,
                duration
        );
        subtask.setId(2);
        assertNotEquals(subtask.getId(), subtask.getEpicId());
    }
}
