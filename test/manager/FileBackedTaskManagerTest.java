package manager;

import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;
    private LocalDateTime startTime;
    private Duration duration;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        // Создаем пустой файл с заголовком
        Files.writeString(tempFile.toPath(),
                "id,type,name,status,description,startTime,duration,epic\n");

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
        assertDoesNotThrow(() -> {
            FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
            assertTrue(loaded.getTasks().isEmpty());
            assertTrue(loaded.getEpics().isEmpty());
            assertTrue(loaded.getSubtasks().isEmpty());
        });
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task = new Task("Task", "Desc", Status.NEW, startTime, duration);
        manager.addTask(task);

        Epic epic = new Epic("Epic", "Epic Desc", Status.NEW, startTime, duration);
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Sub Desc", Status.NEW, epicId,
                startTime.plusMinutes(40), duration);
        manager.addSubtask(subtask);

        assertDoesNotThrow(() -> {
            FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

            List<Task> loadedTasks = loaded.getTasks();
            List<Epic> loadedEpics = loaded.getEpics();
            List<Subtask> loadedSubtasks = loaded.getSubtasks();

            assertEquals(1, loadedTasks.size());
            assertEquals(1, loadedEpics.size());
            assertEquals(1, loadedSubtasks.size());

            Task loadedTask = loadedTasks.get(0);
            Epic loadedEpic = loadedEpics.get(0);
            Subtask loadedSubtask = loadedSubtasks.get(0);

            // Проверка всех полей
            assertEquals(task.getId(), loadedTask.getId());
            assertEquals(task.getName(), loadedTask.getName());
            assertEquals(task.getDescription(), loadedTask.getDescription());
            assertEquals(task.getStatus(), loadedTask.getStatus());
            assertEquals(task.getStartTime(), loadedTask.getStartTime());
            assertEquals(task.getDuration(), loadedTask.getDuration());
            assertEquals(task.getEndTime(), loadedTask.getEndTime());

            assertEquals(epic.getId(), loadedEpic.getId());
            assertEquals(epic.getName(), loadedEpic.getName());
            assertEquals(epic.getDescription(), loadedEpic.getDescription());
            assertEquals(epic.getStatus(), loadedEpic.getStatus());

            assertEquals(subtask.getId(), loadedSubtask.getId());
            assertEquals(subtask.getName(), loadedSubtask.getName());
            assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
            assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
            assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
            assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime());
            assertEquals(subtask.getDuration(), loadedSubtask.getDuration());
            assertEquals(subtask.getEndTime(), loadedSubtask.getEndTime());
        });
    }

    @Test
    void loadFromNonexistentFileShouldThrow() {
        File nonExistent = new File(tempFile.getParentFile(), "no_such_file.csv");
        assertThrows(RuntimeException.class, () -> FileBackedTaskManager.loadFromFile(nonExistent));
    }

    @Test
    void malformedFileShouldThrowOnLoad() throws IOException {
        Files.writeString(tempFile.toPath(), "broken,line,without,correct,fields\n");
        assertThrows(RuntimeException.class, () -> FileBackedTaskManager.loadFromFile(tempFile));
    }

    @Test
    void shouldRestoreNextIdAfterReload() {
        Task task = new Task("T1", "Desc", Status.NEW, startTime, duration);
        manager.addTask(task);

        FileBackedTaskManager loaded = assertDoesNotThrow(() ->
                FileBackedTaskManager.loadFromFile(tempFile));

        Task newTask = new Task("T2", "Desc2", Status.NEW, startTime.plusHours(2), duration);
        int newId = loaded.addTask(newTask);

        assertTrue(newId > task.getId(), "ID новой задачи должен быть больше ID старой");
    }
}
