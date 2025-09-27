package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = HttpTaskServer.getGson();

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String query = h.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        Subtask subtask = manager.getSubtask(id);
                        if (subtask == null) {
                            sendNotFound(h);
                        } else {
                            sendText(h, gson.toJson(subtask), 200);
                        }
                    } else {
                        sendText(h, gson.toJson(manager.getSubtasks()), 200);
                    }
                    break;
                case "POST":
                    String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    try {
                        if (subtask.getId() == 0) {
                            manager.addSubtask(subtask);
                        } else {
                            manager.updateSubtask(subtask);
                        }
                        sendText(h, "", 201);
                    } catch (RuntimeException e) {
                        sendHasOverlaps(h);
                    }
                    break;
                case "DELETE":
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteSubtask(id);
                    } else {
                        manager.clearAllSubtasks();
                    }
                    sendText(h, "", 201);
                    break;
                default:
                    h.sendResponseHeaders(405, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(h);
        }
    }
}

