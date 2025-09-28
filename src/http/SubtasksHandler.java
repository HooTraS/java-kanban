package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
                        Subtask st = manager.getSubtask(id);
                        if (st == null) sendNotFound(exchange);
                        else sendText(exchange, gson.toJson(st), 200);
                    } else {
                        sendText(exchange, gson.toJson(manager.getSubtasks()), 200);
                    }
                    break;
                }
                case "POST": {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    if (body == null || body.isBlank()) {
                        sendText(exchange, "{\"error\":\"Empty body\"}", 400);
                        return;
                    }
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    try {
                        if (subtask.getId() == 0) manager.addSubtask(subtask);
                        else manager.updateSubtask(subtask);
                        sendText(exchange, "", 201);
                    } catch (RuntimeException e) {
                        sendHasOverlaps(exchange);
                    }
                    break;
                }
                case "DELETE": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteSubtask(id);
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
            System.out.println("Ошибка в SubtasksHandler: " + e.getMessage());
            sendServerError(exchange);
        }
    }
}
