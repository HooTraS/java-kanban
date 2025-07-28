package manager;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setup() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void historyStoresImmutableTasks() {
        Task t = new Task("t", "d", Status.NEW);
        t.setId(1);
        historyManager.add(t);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        Task retrieved = history.get(0);

        assertEquals("t", retrieved.getName());
        assertEquals("d", retrieved.getDescription());
        assertEquals(Status.NEW, retrieved.getStatus());
    }
}