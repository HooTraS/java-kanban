package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        return null;
    }

    @Test
    void shouldSaveAndLoadTasksWithTimeFields() {
        Task task = new Task("Task 1", "Description", Status.NEW);
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getTasks().size());
        Task loadedTask = loaded.getTask(task.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());
    }

    @Test
    void shouldSaveAndLoadTasksWithNullTimeFields() {
        Task task = new Task("Task 2", "No time", Status.NEW);
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getTasks().size());
        Task loadedTask = loaded.getTask(task.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertNull(loadedTask.getStartTime());
        assertNull(loadedTask.getDuration());
    }

    @Test
    void shouldSaveAndLoadEpicsAndSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description", Status.NEW);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Sub description", Status.NEW,epic.getId());
        manager.addSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getEpics().size());
        assertEquals(1, loaded.getSubtasks().size());

        Epic loadedEpic = loaded.getEpic(epic.getId());
        Subtask loadedSubtask = loaded.getSubtask(subtask.getId());

        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
    }

    @Test
    void shouldHandleEmptyFileGracefully() throws IOException {
        Files.writeString(tempFile.toPath(), ""); // очищаем файл

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }
}
