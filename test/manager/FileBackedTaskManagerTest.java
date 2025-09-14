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

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;
    private FileBackedTaskManager fileBackedManager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("tasks", ".csv").toFile();
        fileBackedManager = new FileBackedTaskManager(tempFile.toPath(), new InMemoryHistoryManager());
        manager = fileBackedManager;
    }

    @Override
    protected FileBackedTaskManager createManager() {
        return null;
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
            System.err.println("Не удалось удалить временный файл: " + tempFile.getAbsolutePath());
        }
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW);
        Task task2 = new Task("Task 2", "Desc 2", Status.IN_PROGRESS);
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic = new Epic("Epic", "Epic Desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "SubDesc 1", Status.DONE, epicId);
        Subtask sub2 = new Subtask("Sub 2", "SubDesc 2", Status.NEW, epicId);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getTasks(), loaded.getTasks());
        assertEquals(manager.getEpics(), loaded.getEpics());
        assertEquals(manager.getSubtasks(), loaded.getSubtasks());
    }

    @Test
    void loadFromNonExistentFileShouldThrow() {
        File fakeFile = new File("nonexistent.csv");

        assertThrows(RuntimeException.class, () -> {
            FileBackedTaskManager.loadFromFile(fakeFile);
        }, "Ожидалось исключение при загрузке несуществующего файла");
    }

    @Test
    void saveAndLoadValidFileShouldNotThrow() {
        Task task = new Task("Test task", "desc", Status.NEW);
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getTasks(), loaded.getTasks(), "Списки задач должны совпадать");
    }
}