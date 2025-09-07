package model;

import manager.InMemoryTaskManager;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private final LocalDateTime startTime = LocalDateTime.now();
    private final Duration duration = Duration.ofMinutes(30);
    private InMemoryTaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager(null);
        epic = new Epic("Epic Title", "Epic Description", Status.NEW, startTime, duration);
        manager.addEpic(epic);
    }

    @Test
    void epicCannotContainItself() {
        Subtask subtask = new Subtask("Subtask Title", "Subtask Description",
                Status.NEW, epic.getId(), startTime, duration);
        subtask.setId(2);
        assertNotEquals(epic.getId(), subtask.getId());
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW, startTime, duration);
        Task task2 = new Task("Task2", "Desc2", Status.DONE, startTime, duration);
        task1.setId(100);
        task2.setId(100);
        assertEquals(task1, task2);
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic2 = new Epic("Epic2", "Desc2", Status.NEW, startTime, duration);
        epic2.setId(epic.getId());
        assertEquals(epic, epic2);
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.IN_PROGRESS, epic.getId(), startTime, duration);
        sub1.setId(400);
        sub2.setId(400);
        assertEquals(sub1, sub2);
    }

    @Test
    void epicStatusAndTimeCalculations() {
        Epic epic = new Epic("Epic", "Epic Description", Status.NEW, null, Duration.ZERO);
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId(),
                LocalDateTime.of(2025, 9, 7, 10, 0), Duration.ofMinutes(30));
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.DONE, epic.getId(),
                LocalDateTime.of(2025, 9, 7, 10, 30), Duration.ofMinutes(20));

        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        manager.updateEpicStatus(epic);
        manager.updateEpicTime(epic);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        assertEquals(LocalDateTime.of(2025, 9, 7, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 9, 7, 10, 50), epic.getEndTime());
        assertEquals(Duration.ofMinutes(50), epic.getDuration());
    }

    @Test
    void epicStatusAllNew() {
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.NEW, epic.getId(), startTime.plusMinutes(40), duration);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        manager.updateEpicStatus(epic);
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.DONE, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.DONE, epic.getId(), startTime.plusMinutes(40), duration);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        manager.updateEpicStatus(epic);
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void epicStatusAnyInProgress() {
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.IN_PROGRESS, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.NEW, epic.getId(), startTime.plusMinutes(40), duration);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        manager.updateEpicStatus(epic);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }
}
