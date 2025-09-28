package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        Task t = manager.getTask(id);
                        if (t == null) sendNotFound(exchange);
                        else sendText(exchange, gson.toJson(t), 200);
                    } else {
                        sendText(exchange, gson.toJson(manager.getTasks()), 200);
                    }
                    break;
                }
                case "POST": {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    if (body == null || body.isBlank()) {
                        sendText(exchange, "{\"error\":\"Empty body\"}", 400);
                        return;
                    }
                    Task task = gson.fromJson(body, Task.class);
                    try {
                        if (task.getId() == 0) manager.addTask(task);
                        else manager.updateTask(task);
                        sendText(exchange, "", 201);
                    } catch (RuntimeException e) {
                        sendHasOverlaps(exchange);
                    }
                    break;
                }
                case "DELETE": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteTask(id);
                        sendText(exchange, "", 200);
                    } else {
                        sendText(exchange, "{\"error\":\"id required\"}", 400);
                    }
                    break;
                }
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            System.out.println("Ошибка в TasksHandler: " + e.getMessage());
            sendServerError(exchange);
        }
    }
}

