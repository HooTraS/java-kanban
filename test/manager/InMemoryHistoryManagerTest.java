package manager;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task( "Task 1", "Description 1", Status.NEW);
        task2 = new Task( "Task 2", "Description 2", Status.IN_PROGRESS);
        task3 = new Task( "Task 3", "Description 3", Status.DONE);
    }

    @Test
    void add_shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void add_shouldIgnoreNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size());
    }

    @Test
    void add_shouldReplaceTaskWithSameId() {
        Task task1Duplicate = new Task( "Task 1 Duplicate", "Description 1 Duplicate", Status.DONE);
        historyManager.add(task1);
        historyManager.add(task1Duplicate);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1Duplicate, history.get(0));
    }

    @Test
    void remove_shouldRemoveTaskFromHistory() {
        historyManager.add(task1);
        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size());
    }

    @Test
    void remove_shouldNotFailForNonexistentId() {
        historyManager.remove(100);
        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size());
    }

    @Test
    void getHistory_shouldReturnEmptyListWhenHistoryIsEmpty() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void getHistory_shouldReturnTasksInCorrectOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }
}