package manager;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path file;
    private static final String HEADER = "id,type,name,status,description,duration,startTime,endTime,epic";

    public FileBackedTaskManager(Path file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(HEADER + "\n");
            for (Task task : tasks.values()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл", e);
        }
    }

    private String toString(Task task) {
        String epicField = "";
        if (task.getType() == TaskType.SUBTASK) {
            epicField = String.valueOf(((Subtask) task).getEpicId());
        }
        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "null",
                task.getStartTime() != null ? task.getStartTime().toString() : "null",
                task.getEndTime() != null ? task.getEndTime().toString() : "null",
                epicField
        );
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath(), new InMemoryHistoryManager());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                Task task = fromString(line);
                int id = task.getId();
                if (id > maxId) {
                    maxId = id;
                }

                switch (task.getType()) {
                    case TASK -> {
                        manager.tasks.put(id, task);
                        if (task.getStartTime() != null) {
                            manager.prioritizedTasks.add(task);
                        }
                    }
                    case EPIC -> manager.epics.put(id, (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(id, subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.getSubtaskIds().add(id);
                        }
                        if (subtask.getStartTime() != null) {
                            manager.prioritizedTasks.add(subtask);
                        }
                    }
                }
            }
            manager.nextId = maxId + 1;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла", e);
        }
        return manager;
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        Duration duration = (!parts[5].equals("null") && !parts[5].isEmpty())
                ? Duration.ofMinutes(Long.parseLong(parts[5]))
                : null;

        LocalDateTime startTime = (!parts[6].equals("null") && !parts[6].isEmpty())
                ? LocalDateTime.parse(parts[6])
                : null;

        LocalDateTime endTime = (!parts[7].equals("null") && !parts[7].isEmpty())
                ? LocalDateTime.parse(parts[7])
                : null;

        return switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                task.setDuration(duration);
                task.setStartTime(startTime);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                epic.setDuration(duration);
                epic.setStartTime(startTime);
                epic.setEndTime(endTime);
                yield epic;
            }
            case SUBTASK -> {
                int epicId = (!parts[8].equals("null") && !parts[8].isEmpty())
                        ? Integer.parseInt(parts[8])
                        : -1;
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                yield subtask;
            }
        };
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        save();
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void clearAllSubtasks() {
        super.clearAllSubtasks();
        save();
    }
}
