package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class TasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson = HttpTaskServer.getGson();

    public TasksHandler(TaskManager manager) {
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
                        Task task = manager.getTask(id);
                        if (task == null) {
                            sendNotFound(h);
                        } else {
                            sendText(h, gson.toJson(task), 200);
                        }
                    } else {
                        sendText(h, gson.toJson(manager.getTasks()), 200);
                    }
                    break;
                case "POST":
                    String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Task task = gson.fromJson(body, Task.class);
                    try {
                        if (task.getId() == 0) {
                            manager.addTask(task);
                        } else {
                            manager.updateTask(task);
                        }
                        sendText(h, "", 201);
                    } catch (RuntimeException e) {
                        sendHasOverlaps(h);
                    }
                    break;
                case "DELETE":
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteTask(id);
                    } else {
                        manager.clearAllTasks();
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


