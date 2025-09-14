package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setup() {
        manager = createManager();
    }

    @Test
    void addAndGetTasksById() {
        Task t = new Task("t", "d", Status.NEW);
        int id = manager.addTask(t);
        Task retrieved = manager.getTask(id);

        assertEquals(t, retrieved);
    }

    @Test
    void tasksWithManualAndAutoIdDoNotConflict() {
        Task t1 = new Task("t1", "d", Status.NEW);
        int id1 = manager.addTask(t1);

        Task t2 = new Task("t2", "d", Status.NEW);
        t2.setId(999); // ручной id
        int id2 = manager.addTask(t2);

        assertNotEquals(id1, id2);
    }

    @Test
    void taskDataDoesNotChangeWhenStored() {
        Task t = new Task("t", "d", Status.NEW);
        int id = manager.addTask(t);
        Task stored = manager.getTask(id);

        assertEquals("t", stored.getName());
        assertEquals("d", stored.getDescription());
        assertEquals(Status.NEW, stored.getStatus());
    }

    @Test
    void historyAddsTasksCorrectly() {
        Task t = new Task("t", "d", Status.NEW);
        int id = manager.addTask(t);
        manager.getTask(id);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t, history.get(0));
    }

    @Test
    void epicStatusAllNew() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        manager.addSubtask(new Subtask("sub1", "d", Status.NEW, epicId));
        manager.addSubtask(new Subtask("sub2", "d", Status.NEW, epicId));

        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        manager.addSubtask(new Subtask("sub1", "d", Status.DONE, epicId));
        manager.addSubtask(new Subtask("sub2", "d", Status.DONE, epicId));

        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus());
    }

    @Test
    void epicStatusNewAndDone() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        manager.addSubtask(new Subtask("sub1", "d", Status.NEW, epicId));
        manager.addSubtask(new Subtask("sub2", "d", Status.DONE, epicId));

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());
    }

    @Test
    void epicStatusInProgress() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        manager.addSubtask(new Subtask("sub1", "d", Status.IN_PROGRESS, epicId));
        manager.addSubtask(new Subtask("sub2", "d", Status.IN_PROGRESS, epicId));

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());
    }

    @Test
    void subtaskLinkedToEpic() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        Subtask sub = new Subtask("sub", "d", Status.NEW, epicId);
        int subId = manager.addSubtask(sub);

        Subtask stored = manager.getSubtask(subId);
        assertEquals(epicId, stored.getEpicId());
        assertTrue(manager.getEpic(epicId).getSubtaskIds().contains(subId));
    }
}
