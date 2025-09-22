package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile.toPath(), new InMemoryHistoryManager());
    }

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            File file = File.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(file.toPath(), new InMemoryHistoryManager());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл для тестов", e);
        }
    }

    @Test
    void shouldSaveAndLoadTasksWithTimeFields() {
        Task task = new Task("Task 1", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.parse("2025-09-20T10:00"));
        task.setDuration(Duration.ofDays(60));
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getTasks().size(), loaded.getTasks().size());
        assertTrue(loaded.getTasks().containsAll(manager.getTasks()));
    }

    @Test
    void shouldSaveAndLoadTasksWithNullTimeFields() {
        Task task = new Task("Task 2", "No time", Status.NEW);
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getTasks().size(), loaded.getTasks().size());
        assertTrue(loaded.getTasks().containsAll(manager.getTasks()));
    }

    @Test
    void shouldSaveAndLoadEpicsAndSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description", Status.NEW);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Sub description", Status.NEW, epic.getId());
        manager.addSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getEpics().size(), loaded.getEpics().size());
        assertEquals(manager.getSubtasks().size(), loaded.getSubtasks().size());

        assertTrue(loaded.getEpics().containsAll(manager.getEpics()));
        assertTrue(loaded.getSubtasks().containsAll(manager.getSubtasks()));
    }

    @Test
    void shouldRestorePrioritizedTasks() {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW);
        task1.setStartTime(LocalDateTime.parse("2025-09-20T09:00"));
        task1.setDuration(Duration.ofDays(30));

        Task task2 = new Task("Task 2", "Desc 2", Status.NEW);
        task2.setStartTime(LocalDateTime.parse("2025-09-20T08:00"));
        task2.setDuration(Duration.ofDays(45));

        manager.addTask(task1);
        manager.addTask(task2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getPrioritizedTasks(), loaded.getPrioritizedTasks());
    }

    @Test
    void shouldHandleEmptyFileGracefully() throws IOException {
        Files.writeString(tempFile.toPath(), "");

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }
}
