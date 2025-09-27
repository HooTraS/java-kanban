package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson = HttpTaskServer.getGson();

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, gson.toJson(manager.getHistory()), 200);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(h);
        }
    }
}

