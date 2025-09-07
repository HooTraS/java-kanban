package manager;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    private InMemoryHistoryManager historyManager;
    private Task task3;
    private LocalDateTime startTime;
    private Duration duration;

    protected InMemoryTaskManager createManager() {
        historyManager = new InMemoryHistoryManager();
        return new InMemoryTaskManager(historyManager);
    }

    @BeforeEach
    void setUp() {
        // Инициализация базового manager
        manager = createManager();

        // Настройка общих полей
        startTime = LocalDateTime.now();
        duration = Duration.ofMinutes(30);

        // Пример задачи
        task3 = new Task("Task 3", "Desc 3", Status.DONE, startTime, duration);
        task3.setId(3);
    }

    @Test
    void remove_shouldRemoveTaskFromHistory() {
        historyManager.add(task3);
        historyManager.remove(task3.getId());
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void getHistory_shouldReturnTasksInCorrectOrder() {
        Task t1 = new Task("Task 1", "Desc 1", Status.NEW, startTime, duration);
        t1.setId(1);
        Task t2 = new Task("Task 2", "Desc 2", Status.IN_PROGRESS, startTime.plusMinutes(10), duration);
        t2.setId(2);

        historyManager.add(t1);
        historyManager.add(t2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t1, history.get(0));
        assertEquals(t2, history.get(1));
    }
}
