package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = HttpTaskServer.getGson();

    public EpicsHandler(TaskManager manager) {
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
                        Epic epic = manager.getEpic(id);
                        if (epic == null) {
                            sendNotFound(h);
                        } else {
                            sendText(h, gson.toJson(epic), 200);
                        }
                    } else {
                        sendText(h, gson.toJson(manager.getEpics()), 200);
                    }
                    break;
                case "POST":
                    String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(body, Epic.class);
                    if (epic.getId() == 0) {
                        manager.addEpic(epic);
                    } else {
                        manager.updateEpic(epic);
                    }
                    sendText(h, "", 201);
                    break;
                case "DELETE":
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteEpic(id);
                    } else {
                        manager.clearAllEpics();
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

