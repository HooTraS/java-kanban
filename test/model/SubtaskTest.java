package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SubtaskTest {

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Epic Title", "Epic Description", Status.NEW);
        epic.setId(1);

        Subtask subtask = new Subtask("Subtask Title", "Subtask Description", Status.NEW, epic.getId());
        subtask.setId(2);

        assertNotEquals(subtask.getId(), subtask.getEpicId());
    }
}
