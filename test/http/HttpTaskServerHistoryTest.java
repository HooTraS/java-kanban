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

public class HttpTaskServerHistoryTest {
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
    void shouldReturnHistory() throws IOException, InterruptedException {
        Task t = new Task("T1", "D", Status.NEW);
        t.setDuration(Duration.ofMinutes(30));
        t.setStartTime(LocalDateTime.of(2025, 9, 27, 12, 0));
        manager.addTask(t);
        manager.getTask(t.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        Task[] history = gson.fromJson(resp.body(), Task[].class);
        assertEquals(1, history.length);
        assertEquals(t.getId(), history[0].getId());
    }
}

