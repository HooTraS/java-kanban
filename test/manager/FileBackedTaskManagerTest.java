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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
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

        List<Task> tasks = loaded.getTasks();
        List<Epic> epics = loaded.getEpics();
        List<Subtask> subtasks = loaded.getSubtasks();

        assertEquals(2, tasks.size());
        assertEquals(1, epics.size());
        assertEquals(2, subtasks.size());

        assertEquals("Task 1", tasks.get(0).getName());
        assertEquals("Epic", epics.get(0).getName());
        assertEquals(epicId, subtasks.get(0).getEpicId());
    }
}
