package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected LocalDateTime startTime;
    protected Duration duration;

    @BeforeEach
    void setup() {
        startTime = LocalDateTime.now();
        duration = Duration.ofMinutes(30);
    }

    @Test
    void shouldAddAndGetTask() {
        Task task = new Task("Task 1", "Desc", Status.NEW, startTime, duration);
        int id = manager.addTask(task);

        Task saved = manager.getTask(id);
        assertNotNull(saved, "Task должна быть в менеджере");
        assertEquals(task.getName(), saved.getName(), "Имена задач должны совпадать");
        assertEquals(task.getDescription(), saved.getDescription(), "Описания задач должны совпадать");
        assertEquals(task.getStatus(), saved.getStatus(), "Статусы задач должны совпадать");
    }

    @Test
    void shouldAddEpicAndSubtask() {
        Epic epic = new Epic("Epic", "Epic desc", Status.NEW,
                LocalDateTime.of(2025, 9, 2, 14, 0), Duration.ofHours(3));
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Sub desc", Status.NEW, epicId, startTime, duration);
        int subId = manager.addSubtask(subtask);

        Subtask savedSub = manager.getSubtask(subId);
        assertNotNull(savedSub, "Subtask должна быть в менеджере");
        assertEquals(epicId, savedSub.getEpicId(), "Subtask должна принадлежать правильному эпику");

        Epic savedEpic = manager.getEpic(epicId);
        assertNotNull(savedEpic, "Epic должна быть в менеджере");
        assertTrue(savedEpic.getSubtaskIds().contains(subId),
                "Epic должен содержать id добавленной subtask");
    }

    @Test
    void epicStatusShouldDependOnSubtasks() {
        LocalDateTime testStartTime = LocalDateTime.of(2025, 9, 2, 14, 0);
        Duration testDuration = Duration.ofHours(1);

        Epic epic = new Epic("Epic", "Epic desc", Status.NEW, testStartTime, Duration.ofHours(3));
        int epicId = manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW, epicId, testStartTime, testDuration);
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW, epicId, testStartTime.plusMinutes(40), testDuration);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus(),
                "Если все подзадачи NEW — эпик должен быть NEW");

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Если есть DONE и NEW — эпик должен быть IN_PROGRESS");

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus(),
                "Если все подзадачи DONE — эпик должен быть DONE");
    }

    @Test
    void shouldDetectOverlappingTasks() {
        // Первая задача
        Task task1 = new Task(
                "Задача 1",
                "Описание 1",
                Status.NEW,
                LocalDateTime.of(2025, 9, 7, 10, 0),
                Duration.ofHours(2) // 10:00–12:00
        );

        // Вторая задача пересекается с первой
        Task task2 = new Task(
                "Задача 2",
                "Описание 2",
                Status.NEW,
                LocalDateTime.of(2025, 9, 7, 11, 0),
                Duration.ofHours(2) // 11:00–13:00
        );

        // Третья задача не пересекается
        Task task3 = new Task(
                "Задача 3",
                "Описание 3",
                Status.NEW,
                LocalDateTime.of(2025, 9, 7, 12, 30),
                Duration.ofHours(1) // 12:30–13:30
        );

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        // Проверяем пересечения вручную
        boolean overlap1and2 = task1.getEndTime().isAfter(task2.getStartTime()) &&
                task1.getStartTime().isBefore(task2.getEndTime());
        boolean overlap1and3 = task1.getEndTime().isAfter(task3.getStartTime()) &&
                task1.getStartTime().isBefore(task3.getEndTime());
        boolean overlap2and3 = task2.getEndTime().isAfter(task3.getStartTime()) &&
                task2.getStartTime().isBefore(task3.getEndTime());

        assertTrue(overlap1and2, "Задачи 1 и 2 пересекаются по времени");
        assertFalse(overlap1and3, "Задачи 1 и 3 не должны пересекаться");
        assertTrue(overlap2and3, "Задачи 2 и 3 пересекаются по времени");
    }

}
