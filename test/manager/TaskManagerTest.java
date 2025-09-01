package manager;

import model.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected final LocalDateTime startTime = LocalDateTime.now();
    protected final Duration duration = Duration.ofMinutes(30);

    @Test
    void shouldAddAndGetTask() {
        Task task = new Task("Task 1", "Desc", Status.NEW, startTime, duration);
        int id = manager.addTask(task);

        Task saved = manager.getTaskById(id);
        assertNotNull(saved);
        assertEquals(task.getName(), saved.getName());
        assertEquals(task.getDescription(), saved.getDescription());
        assertEquals(task.getStatus(), saved.getStatus());
    }

    @Test
    void shouldAddEpicAndSubtask() {
        Epic epic = new Epic("Epic", "Epic desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Sub desc", Status.NEW, epicId, startTime, duration);
        int subId = manager.addSubtask(subtask);

        Subtask savedSub = manager.getSubtaskById(subId);
        assertEquals(epicId, savedSub.getEpicId());
        assertEquals(epicId, manager.getEpicById(epicId).getSubtaskIds().get(0));
    }

    @Test
    void epicStatusShouldDependOnSubtasks() {
        Epic epic = new Epic("Epic", "Epic desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW, epicId, startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW, epicId, startTime.plusMinutes(40), duration);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(Status.NEW, manager.getEpicById(epicId).getStatus());

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epicId).getStatus());

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        assertEquals(Status.DONE, manager.getEpicById(epicId).getStatus());
    }

    @Test
    void shouldNotAllowOverlappingTasks() {
        Task task1 = new Task("Task1", "Desc", Status.NEW, startTime, duration);
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW, startTime.plusMinutes(10), duration);
        assertThrows(IllegalArgumentException.class, () -> manager.addTask(task2));
    }
}
