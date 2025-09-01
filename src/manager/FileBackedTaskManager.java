package manager;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path file;
    private static final String HEADER = "id,type,name,status,description,startTime,duration,epic";

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
        if (task instanceof Subtask subtask) {
            epicField = String.valueOf(subtask.getEpicId());
        }
        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                task.getStartTime() != null ? task.getStartTime().toString() : "",
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "",
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
                    case TASK -> manager.tasks.put(id, task);
                    case EPIC -> manager.epics.put(id, (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(id, subtask);
                        manager.epics.get(subtask.getEpicId()).addSubtaskId(id);
                    }
                }
                // добавляем в отсортированный список
                if (task.getStartTime() != null && !(task instanceof Epic)) {
                    manager.prioritizedTasks.add(task);
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
        LocalDateTime startTime = parts[5].isBlank() ? null : LocalDateTime.parse(parts[5]);
        Duration duration = parts[6].isBlank() ? null : Duration.ofMinutes(Long.parseLong(parts[6]));

        return switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status, startTime, duration);
                task.setId(id);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                yield epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[7]);
                Subtask subtask = new Subtask(name, description, status, epicId, startTime, duration);
                subtask.setId(id);
                yield subtask;
            }
        };
    }

    @Override
    public int addTask(Task task) {
        checkTimeIntersection(task);
        int id = super.addTask(task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        checkTimeIntersection(task);
        super.updateTask(task);
        prioritizedTasks.remove(task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
        super.deleteTask(id);
        save();
    }

    @Override
    public int addSubtask(Subtask subtask) {
        checkTimeIntersection(subtask);
        int id = super.addSubtask(subtask);
        if (subtask.getStartTime() != null) prioritizedTasks.add(subtask);
        save();
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        checkTimeIntersection(subtask);
        super.updateSubtask(subtask);
        prioritizedTasks.remove(subtask);
        if (subtask.getStartTime() != null) prioritizedTasks.add(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
        }
        super.deleteSubtask(id);
        save();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void checkTimeIntersection(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) return;

        for (Task existing : prioritizedTasks) {
            if (existing.getStartTime() == null || existing.getEndTime() == null) continue;

            boolean overlap = !(newTask.getEndTime().isBefore(existing.getStartTime())
                    || newTask.getStartTime().isAfter(existing.getEndTime()));

            if (overlap && newTask.getId() != existing.getId()) {
                throw new IllegalArgumentException("Задача пересекается по времени с задачей id=" + existing.getId());
            }
        }
    }
}
