package http;

import com.google.gson.Gson;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTasksTest {
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
    void shouldAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.of(2025, 9, 27, 12, 0));
        task.setDuration(Duration.ofMinutes(30));

        String json = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test", tasks.get(0).getName());
    }

    @Test
    void shouldReturnAllTasks() throws IOException, InterruptedException {
        Task task = new Task("T1", "D1", Status.NEW);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.of(2025, 9, 27, 12, 0));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("T1", tasks[0].getName());
    }

    @Test
    void shouldReturn406IfOverlap() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "D1", Status.NEW);
        task1.setDuration(Duration.ofMinutes(30));
        task1.setStartTime(LocalDateTime.of(2025, 9, 27, 12, 0));
        manager.addTask(task1);

        Task overlap = new Task("T2", "D2", Status.NEW);
        overlap.setDuration(Duration.ofMinutes(30));
        overlap.setStartTime(LocalDateTime.of(2025, 9, 27, 12, 15));

        String json = gson.toJson(overlap);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "При пересечении задач должен вернуться код 406");
    }
}

