package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SubtaskTest {
    @Test
    void subtaskCannotBeItsOwnEpic() {
        Task task = new Task("t", "d", Status.NEW);
        Subtask subtask = new Subtask("s", "d", Status.NEW, task.getId());

        assertNotEquals(subtask.getEpicId(), subtask.getId());
    }
}