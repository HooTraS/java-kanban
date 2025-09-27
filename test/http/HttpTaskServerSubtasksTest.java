package http;

import com.google.gson.Gson;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerSubtasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson = HttpTaskServer.getGson();
    private Epic epic;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        server = new HttpTaskServer(manager);
        server.start();
        epic = new Epic("Epic1", "Desc", Status.NEW);
        manager.addEpic(epic);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Sub1", "D", Status.NEW, epic.getId());
        subtask.setStartTime(LocalDateTime.of(2025, 9, 27, 12, 0));
        subtask.setDuration(Duration.ofMinutes(15));

        String json = gson.toJson(subtask);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getSubtasks().size());
    }

    @Test
    void shouldReturnSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic1", "Epic for subtasks", Status.NEW);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub1", "Subtask for Epic1", Status.NEW, epic.getId());
        subtask.setDuration(Duration.ofMinutes(30));
        subtask.setStartTime(LocalDateTime.of(2025, 9, 27, 14, 0));
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);

        assertNotNull(subtasks, "Сабтаски не возвращаются");
        assertEquals(1, subtasks.length, "Должна быть одна сабтаска");
        assertEquals("Sub1", subtasks[0].getName(), "Некорректное имя сабтаски");
    }

}

