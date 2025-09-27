package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;
    private static final Gson gson = new Gson();

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/epics", new EpicsHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
            @Override
            public JsonElement serialize(Duration duration, Type type, JsonSerializationContext context) {
                return new JsonPrimitive(duration.toMinutes());
            }
        });
        builder.registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
            @Override
            public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                return Duration.ofMinutes(json.getAsLong());
            }
        });

        builder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime ldt, Type type, JsonSerializationContext context) {
                return new JsonPrimitive(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        });
        builder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        });

        return builder.create();
    }


    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer(Managers.getDefault());
        taskServer.start();
    }
}


