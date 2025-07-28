package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }

    @Test
    void addAndGetDifferentTaskTypes() {
        Task task = new Task("task", "desc", Status.NEW);
        Epic epic = new Epic("epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        Subtask subtask = new Subtask("sub", "desc", Status.NEW, epicId);

        int taskId = manager.addTask(task);
        int subtaskId = manager.addSubtask(subtask);

        assertEquals(task, manager.getTask(taskId));
        assertEquals(epic, manager.getEpic(epicId));
        assertEquals(subtask, manager.getSubtask(subtaskId));
    }

    @Test
    void tasksWithManualAndAutoIdDoNotConflict() {
        Task t1 = new Task("t1", "d", Status.NEW);
        int id1 = manager.addTask(t1);

        Task t2 = new Task("t2", "d", Status.NEW);
        t2.setId(999);
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
}
