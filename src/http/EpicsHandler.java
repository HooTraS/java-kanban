package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Epic;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager manager, Gson gson) {
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
                        Epic e = manager.getEpic(id);
                        if (e == null) sendNotFound(exchange);
                        else sendText(exchange, gson.toJson(e), 200);
                    } else {
                        sendText(exchange, gson.toJson(manager.getEpics()), 200);
                    }
                    break;
                }
                case "POST": {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    if (body.isBlank()) {
                        sendText(exchange, "{\"error\":\"Empty request body\"}", 400);
                        return;
                    }

                    Epic epic = gson.fromJson(body, Epic.class);
                    if (epic.getId() == 0) {
                        int id = manager.addEpic(epic);
                        sendText(exchange, gson.toJson(manager.getEpic(id)), 201);
                    } else {
                        sendText(exchange, "{\"error\":\"Epic update not supported\"}", 400);
                    }
                    break;
                }

                case "DELETE": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteEpic(id);
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
            System.out.println("Ошибка в EpicsHandler: " + e.getMessage());
            sendServerError(exchange);
        }
    }
}

