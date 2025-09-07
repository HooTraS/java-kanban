package manager;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    private static final String HEADER = "id,type,name,status,description,epic,startTime,duration";

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write(HEADER);
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + file.getName(), e);
        }
    }

    private String toString(Task task) {
        String epicField = "";
        if (task.getType() == TaskType.SUBTASK) {
            epicField = String.valueOf(((Subtask) task).getEpicId());
        }
        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                epicField,
                task.getStartTime() != null ? task.getStartTime().toString() : "",
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "0"
        );
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        int epicId = type == TaskType.SUBTASK && !parts[5].isEmpty() ? Integer.parseInt(parts[5]) : -1;

        LocalDateTime startTime = null;
        if (parts.length > 6 && !parts[6].isEmpty()) {
            try {
                startTime = LocalDateTime.parse(parts[6]);
            } catch (Exception e) {
                startTime = null;
            }
        }

        Duration duration = Duration.ZERO;
        if (parts.length > 7 && !parts[7].isEmpty()) {
            try {
                duration = Duration.ofMinutes(Long.parseLong(parts[7]));
            } catch (Exception e) {
                duration = Duration.ZERO;
            }
        }

        switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status, startTime, duration);
                task.setId(id);
                return task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description, status, startTime, duration);
                epic.setId(id);
                return epic;
            }
            case SUBTASK -> {
                Subtask subtask = new Subtask(name, description, status, epicId, startTime, duration);
                subtask.setId(id);
                return subtask;
            }
        }
        return null;
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            int maxId = 0;

            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; continue;
                }
                if (line.isBlank()) continue;

                Task task = fromString(line);
                int id = task.getId();
                maxId = Math.max(maxId, id);

                switch (task.getType()) {
                    case TASK -> {
                        manager.tasks.put(id, task);
                        manager.prioritizedTasks.add(task);
                    }
                    case EPIC -> manager.epics.put(id, (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(id, subtask);
                        manager.prioritizedTasks.add(subtask);

                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) epic.addSubtaskId(subtask.getId());
                    }
                }
            }
            manager.nextId = maxId + 1;
            manager.epics.values().forEach(e -> {
                manager.updateEpicStatus(e);
                manager.updateEpicTime(e);
            });

        } catch (Exception e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + file.getName(), e);
        }
        return manager;
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        save();
    }

    @Override
    public void clearAllSubtasks() {
        super.clearAllSubtasks();
        save();
    }
}
