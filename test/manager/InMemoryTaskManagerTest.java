package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void deletingTaskAlsoRemovesItFromHistory() {
        Task task = new Task("Task to delete", "desc", Status.NEW);
        int taskId = manager.addTask(task);
        manager.getTask(taskId); // Добавляем в историю

        assertEquals(1, manager.getHistory().size());
        manager.deleteTask(taskId);
        assertTrue(manager.getHistory().isEmpty(), "Задача не удалена из истории");
    }

    @Test
    void deletingSubtaskAlsoRemovesItFromHistory() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        Subtask subtask = new Subtask("Sub", "desc", Status.NEW, epicId);
        int subtaskId = manager.addSubtask(subtask);
        manager.getSubtask(subtaskId); // В историю

        assertEquals(1, manager.getHistory().size());
        manager.deleteSubtask(subtaskId);
        assertTrue(manager.getHistory().isEmpty(), "Подзадача не удалена из истории");
    }

    @Test
    void deletingEpicAlsoRemovesItAndSubtasksFromHistory() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "desc", Status.NEW, epicId);
        Subtask sub2 = new Subtask("Sub2", "desc", Status.NEW, epicId);
        int subId1 = manager.addSubtask(sub1);
        int subId2 = manager.addSubtask(sub2);

        manager.getEpic(epicId);
        manager.getSubtask(subId1);
        manager.getSubtask(subId2);

        assertEquals(3, manager.getHistory().size(), "Элементы не попали в историю");

        manager.deleteEpic(epicId);

        assertTrue(manager.getHistory().isEmpty(), "Эпик и его подзадачи не удалены из истории");
    }
}
