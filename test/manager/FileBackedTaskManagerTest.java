package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
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
    private LocalDateTime startTime;
    private Duration duration;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        Files.writeString(tempFile.toPath(),
                "id,type,name,status,description,epicId,startTime,duration\n");

        manager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);

        startTime = LocalDateTime.of(2025, 9, 7, 12, 0);
        duration = Duration.ofMinutes(90);
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists() && !tempFile.delete()) {
            System.err.println("Не удалось удалить временный файл: " + tempFile.getAbsolutePath());
        }
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getTasks().isEmpty(), "Tasks должны быть пустыми");
        assertTrue(loaded.getEpics().isEmpty(), "Epics должны быть пустыми");
        assertTrue(loaded.getSubtasks().isEmpty(), "Subtasks должны быть пустыми");
    }


    @Test
    void shouldSaveAndLoadTasksAndSubtasks() {
        Task task = new Task("Task", "Desc", Status.NEW, startTime, duration);
        manager.addTask(task);

        Epic epic = new Epic("Epic", "Epic Desc", Status.NEW, startTime, duration);
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Sub Desc", Status.NEW, epicId,
                startTime.plusMinutes(40), duration);
        manager.addSubtask(subtask);

        FileBackedTaskManager loaded = assertDoesNotThrow(() ->
                FileBackedTaskManager.loadFromFile(tempFile));

        assertEquals(1, loaded.getTasks().size(), "Должна быть 1 задача");
        assertEquals(1, loaded.getEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loaded.getSubtasks().size(), "Должна быть 1 подзадача");

        Task loadedTask = loaded.getTasks().get(0);
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());

        Epic loadedEpic = loaded.getEpics().get(0);
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());

        Subtask loadedSubtask = loaded.getSubtasks().get(0);
        assertEquals(subtask.getId(), loadedSubtask.getId());
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
        assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime());
        assertEquals(subtask.getDuration(), loadedSubtask.getDuration());
    }

    @Test
    void shouldRestoreNextIdAfterReload() {
        Task task = new Task("T1", "Desc", Status.NEW, startTime, duration);
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = new Task("T2", "Desc2", Status.NEW, startTime.plusHours(2), duration);
        int newId = loaded.addTask(newTask);

        assertTrue(newId > task.getId(), "ID новой задачи должен быть больше ID старой");
    }

    @Test
    void loadFromNonexistentFileShouldThrow() {
        File nonExistent = new File(tempFile.getParentFile(), "no_such_file.csv");
        assertThrows(RuntimeException.class, () -> FileBackedTaskManager.loadFromFile(nonExistent));
    }

    @Test
    void malformedFileShouldBeHandledGracefully() throws IOException {
        Files.writeString(tempFile.toPath(), "broken,line,without,correct,fields\n");

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(manager.getTasks().isEmpty(), "Tasks должны быть пустыми при некорректном файле");
        assertTrue(manager.getEpics().isEmpty(), "Epics должны быть пустыми при некорректном файле");
        assertTrue(manager.getSubtasks().isEmpty(), "Subtasks должны быть пустыми при некорректном файле");
    }

}
