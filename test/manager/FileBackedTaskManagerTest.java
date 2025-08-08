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

        List<Task> originalTasks = manager.getTasks();
        List<Epic> originalEpics = manager.getEpics();
        List<Subtask> originalSubtasks = manager.getSubtasks();

        List<Task> loadedTasks = loaded.getTasks();
        List<Epic> loadedEpics = loaded.getEpics();
        List<Subtask> loadedSubtasks = loaded.getSubtasks();

        assertEquals(originalTasks.size(), loadedTasks.size());
        assertEquals(originalEpics.size(), loadedEpics.size());
        assertEquals(originalSubtasks.size(), loadedSubtasks.size());

        for (int i = 0; i < originalTasks.size(); i++) {
            Task expected = originalTasks.get(i);
            Task actual = loadedTasks.get(i);
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getDescription(), actual.getDescription());
            assertEquals(expected.getStatus(), actual.getStatus());
            assertEquals(expected.getType(), actual.getType());
        }

        for (int i = 0; i < originalEpics.size(); i++) {
            Epic expected = originalEpics.get(i);
            Epic actual = loadedEpics.get(i);
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getDescription(), actual.getDescription());
            assertEquals(expected.getStatus(), actual.getStatus());
            assertEquals(expected.getSubtaskIds(), actual.getSubtaskIds());

        }

        for (int i = 0; i < originalSubtasks.size(); i++) {
            Subtask expected = originalSubtasks.get(i);
            Subtask actual = loadedSubtasks.get(i);
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getDescription(), actual.getDescription());
            assertEquals(expected.getStatus(), actual.getStatus());
            assertEquals(expected.getType(), actual.getType());
            assertEquals(expected.getEpicId(), actual.getEpicId());
        }
    }


}
