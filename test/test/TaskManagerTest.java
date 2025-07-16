// Пример тестов на основе ТЗ

package test;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }

    @Test
    void tasksWithSameIdAreEqual() {
        Task task1 = new Task("name", "desc", Status.NEW);
        int id = manager.addTask(task1);
        Task task2 = new Task("name", "desc", Status.NEW);
        task2.setId(id);

        assertEquals(task1, task2);
    }

    @Test
    void subtasksWithSameIdAreEqual() {
        Epic epic = new Epic("epic name", "epic description", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask s1 = new Subtask("name", "desc", Status.NEW, epicId);
        int id = manager.addSubtask(s1);
        Subtask s2 = new Subtask("name", "desc", Status.NEW, epicId);
        s2.setId(id);

        assertEquals(s1, s2);
    }

    @Test
    void epicCannotContainItself() {
        Epic epic = new Epic("epic name", "epic description", Status.NEW);
        int id = manager.addEpic(epic);

        Subtask sub = new Subtask("sub", "desc", Status.NEW, id);
        int subId = manager.addSubtask(sub);

        assertNotEquals(id, subId);
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Task t = new Task("t", "d", Status.NEW);
        int tid = manager.addTask(t);

        Subtask s = new Subtask("s", "d", Status.NEW, tid);
        int id = manager.addSubtask(s);

        assertEquals(-1, id); // т.к. нет такого эпика
    }

    @Test
    void managersAlwaysReturnInitialized() {
        assertNotNull(manager);
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
        t2.setId(999); // этот id будет проигнорирован
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
