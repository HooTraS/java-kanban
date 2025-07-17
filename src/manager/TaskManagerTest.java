package manager;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
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

    @Test
    void historyAddsTasksCorrectly() {
        Task t = new Task("t", "d", Status.NEW);
        int id = manager.addTask(t);
        manager.getTask(id);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t, history.get(0));
    }
}
