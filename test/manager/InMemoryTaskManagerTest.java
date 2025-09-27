package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @BeforeEach
    void setup() {
        manager = createManager();
    }

    @Test
    void deletingTaskAlsoRemovesItFromHistory() {
        Task task = new Task("Task to delete", "desc", Status.NEW);
        int taskId = manager.addTask(task);
        manager.getTask(taskId);

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
        manager.getSubtask(subtaskId);

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
