package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EpicTest {
    @Test
    void epicCannotContainItself() {
        Epic epic = new Epic("epic", "desc", Status.NEW);
        Subtask subtask = new Subtask("sub", "desc", Status.NEW, epic.getId());

        assertNotEquals(epic.getId(), subtask.getId());
    }
}