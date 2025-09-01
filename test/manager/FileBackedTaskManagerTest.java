package manager;

import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        // сразу пишем заголовок CSV
        Files.writeString(tempFile.toPath(), "id,type,name,status,description,startTime,duration,epic\n");
        manager = new FileBackedTaskManager(tempFile.toPath(), new InMemoryHistoryManager());
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                System.err.println("Не удалось удалить временный файл: " + tempFile.getAbsolutePath());
            }
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

        Epic epic = new Epic("Epic", "Epic Desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask sub = new Subtask("Sub", "Sub Desc", Status.NEW, epicId,
                startTime.plusMinutes(40), duration);
        manager.addSubtask(sub);

        assertDoesNotThrow(() -> {
            FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

            List<Task> loadedTasks = loaded.getTasks();
            List<Epic> loadedEpics = loaded.getEpics();
            List<Subtask> loadedSubtasks = loaded.getSubtasks();

            assertEquals(1, loadedTasks.size());
            assertEquals(1, loadedEpics.size());
            assertEquals(1, loadedSubtasks.size());

            assertEquals(task, loadedTasks.get(0));
            assertEquals(epic, loadedEpics.get(0));
            assertEquals(sub, loadedSubtasks.get(0));
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
