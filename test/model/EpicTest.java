package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void epicCannotContainItself() {
        Epic epic = new Epic("Epic Title", "Epic Description", Status.NEW);
        epic.setId(1);

        Subtask subtask = new Subtask("Subtask Title", "Subtask Description", Status.NEW, epic.getId());
        subtask.setId(2);

        assertNotEquals(epic.getId(), subtask.getId());
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task1", "Description1", Status.NEW);
        Task task2 = new Task("Task2", "Description2", Status.DONE);

        task1.setId(100);
        task2.setId(100);

        assertEquals(task1, task2);
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic1", "Description1", Status.NEW);
        Epic epic2 = new Epic("Epic2", "Description2", Status.NEW);

        epic1.setId(200);
        epic2.setId(200);

        assertEquals(epic1, epic2);
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        epic.setId(300);

        Subtask subtask1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc2", Status.IN_PROGRESS, epic.getId());

        subtask1.setId(400);
        subtask2.setId(400);

        assertEquals(subtask1, subtask2);
    }

    @Test
    void epicStatusAllNew() {
        Status[] subtasks = {Status.NEW, Status.NEW};
        Status expected = calculateEpicStatus(subtasks);
        assertEquals(Status.NEW, expected);
    }

    @Test
    void epicStatusAllDone() {
        Status[] subtasks = {Status.DONE, Status.DONE};
        Status expected = calculateEpicStatus(subtasks);
        assertEquals(Status.DONE, expected);
    }

    @Test
    void epicStatusNewAndDone() {
        Status[] subtasks = {Status.NEW, Status.DONE};
        Status expected = calculateEpicStatus(subtasks);
        assertEquals(Status.IN_PROGRESS, expected);
    }

    @Test
    void epicStatusAllInProgress() {
        Status[] subtasks = {Status.IN_PROGRESS, Status.IN_PROGRESS};
        Status expected = calculateEpicStatus(subtasks);
        assertEquals(Status.IN_PROGRESS, expected);
    }

    private Status calculateEpicStatus(Status[] subtasks) {
        boolean allNew = true;
        boolean allDone = true;

        for (Status status : subtasks) {
            if (status != Status.NEW) allNew = false;
            if (status != Status.DONE) allDone = false;
        }

        if (allNew) return Status.NEW;
        if (allDone) return Status.DONE;
        return Status.IN_PROGRESS;
    }
}
