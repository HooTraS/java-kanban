package http;

import com.google.gson.Gson;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerPrioritizedTest {

    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldReturnTasksInPriorityOrder() throws IOException, InterruptedException {
        Task t1 = new Task("T1", "D1", Status.NEW);
        t1.setDuration(Duration.ofMinutes(15));
        t1.setStartTime(LocalDateTime.of(2025, 9, 27, 13, 0));
        Task t2 = new Task("T2", "D2", Status.NEW);
        t2.setDuration(Duration.ofMinutes(15));
        t2.setStartTime(LocalDateTime.of(2025, 9, 27, 12, 0));

        manager.addTask(t1);
        manager.addTask(t2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        Task[] tasks = gson.fromJson(resp.body(), Task[].class);
        assertEquals(2, tasks.length);
        assertEquals(t2.getId(), tasks[0].getId());
    }
}

