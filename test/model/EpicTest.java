package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    private final LocalDateTime startTime = LocalDateTime.now();
    private final Duration duration = Duration.ofMinutes(30);

    @Test
    void epicCannotContainItself() {
        Epic epic = new Epic("Epic Title", "Epic Description", Status.NEW);
        epic.setId(1);

        Subtask subtask = new Subtask("Subtask Title", "Subtask Description",
                Status.NEW, epic.getId(), startTime, duration);
        subtask.setId(2);

        assertNotEquals(epic.getId(), subtask.getId());
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task1", "Description1", Status.NEW, startTime, duration);
        Task task2 = new Task("Task2", "Description2", Status.DONE, startTime, duration);

        task1.setId(100);
        task2.setId(100);

        assertEquals(task1, task2, "Tasks with the same id should be equal");
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic1", "Description1", Status.NEW);
        Epic epic2 = new Epic("Epic2", "Description2", Status.NEW);

        epic1.setId(200);
        epic2.setId(200);

        assertEquals(epic1, epic2);
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        epic.setId(300);

        Subtask subtask1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId(), startTime, duration);
        Subtask subtask2 = new Subtask("Sub2", "Desc2", Status.IN_PROGRESS, epic.getId(), startTime, duration);

        subtask1.setId(400);
        subtask2.setId(400);

        assertEquals(subtask1, subtask2);
    }

    // 🔥 Тесты по ТЗ — проверка расчёта статуса Epic
    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.NEW, epic.getId(), startTime.plusMinutes(30), duration);

        epic.addSubtask(sub1);
        epic.addSubtask(sub2);

        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.DONE, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.DONE, epic.getId(), startTime.plusMinutes(30), duration);

        epic.addSubtask(sub1);
        epic.addSubtask(sub2);

        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenMixedNewAndDone() {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.DONE, epic.getId(), startTime.plusMinutes(30), duration);

        epic.addSubtask(sub1);
        epic.addSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskInProgress() {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.IN_PROGRESS, epic.getId(), startTime, duration);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.NEW, epic.getId(), startTime.plusMinutes(30), duration);

        epic.addSubtask(sub1);
        epic.addSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }
}
