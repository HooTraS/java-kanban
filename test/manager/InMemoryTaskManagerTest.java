package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    private LocalDateTime startTime;
    private Duration duration;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        startTime = LocalDateTime.now();
        duration = Duration.ofMinutes(30);
    }

    @Test
    void shouldAddEpicAndSubtask() {
        Epic epic = new Epic("Epic", "Epic desc", Status.NEW,
                LocalDateTime.of(2025, 9, 2, 14, 0), Duration.ofHours(3));
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Sub desc", Status.NEW, epicId, startTime, duration);
        int subId = manager.addSubtask(subtask);

        Subtask savedSub = manager.getSubtask(subId);
        assertEquals(epicId, savedSub.getEpicId(), "Epic ID подзадачи должен совпадать");
        assertEquals(subId, manager.getEpic(epicId).getSubtaskIds().get(0),
                "Подзадача должна быть добавлена в список эпика");
    }

    @Test
    void shouldAllowOverlappingTasksIfManagerDoesNotCheck() {
        Task task1 = new Task("Task1", "Desc", Status.NEW, startTime, duration);
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW, startTime.plusMinutes(10), duration);
        manager.addTask(task2);

        org.junit.jupiter.api.Assertions.assertEquals(2, manager.getTasks().size(),
                "Обе задачи должны быть в менеджере, т.к. пересечение не запрещено");
    }

    @Test
    void subtaskMustHaveEpic() {
        Epic epic = new Epic("Epic", "desc", Status.NEW,
                LocalDateTime.of(2025, 9, 2, 14, 0), Duration.ofHours(3));
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "desc", Status.NEW, epicId, startTime, duration);
        int subtaskId = manager.addSubtask(subtask);

        assertEquals(epicId, manager.getSubtask(subtaskId).getEpicId(),
                "У подзадачи должен быть корректный epicId");
    }

    @Test
    void epicStatusShouldBeCalculatedFromSubtasks() {
        Epic epic = new Epic("Epic", "desc", Status.NEW,
                LocalDateTime.of(2025, 9, 2, 14, 0), Duration.ofHours(3));
        int epicId = manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "desc", Status.NEW, epicId, startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "desc", Status.NEW, epicId, startTime.plusMinutes(40), duration);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus(),
                "Если все подзадачи NEW — эпик должен быть NEW");

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);

        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus(),
                "Если все подзадачи DONE — эпик должен быть DONE");

        sub1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub1);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Если статусы разные — эпик должен быть IN_PROGRESS");
    }

    @Test
    void deletingEpicAlsoRemovesSubtasksAndHistory() {
        Epic epic = new Epic("Epic", "desc", Status.NEW,
                LocalDateTime.of(2025, 9, 2, 14, 0), Duration.ofHours(3));
        int epicId = manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "desc", Status.NEW, epicId, startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "desc", Status.NEW, epicId, startTime.plusMinutes(40), duration);
        int subId1 = manager.addSubtask(sub1);
        int subId2 = manager.addSubtask(sub2);

        manager.getEpic(epicId);
        manager.getSubtask(subId1);
        manager.getSubtask(subId2);

        assertEquals(3, manager.getHistory().size(), "Элементы должны быть в истории");

        manager.deleteEpic(epicId);

        assertTrue(manager.getHistory().isEmpty(),
                "После удаления эпика и его подзадач история должна быть очищена");
    }
}
