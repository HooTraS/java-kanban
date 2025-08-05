package manager;

import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path file;
    protected int nextId = 0;

    public FileBackedTaskManager(Path file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
        loadFromFile();
    }

    protected void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : tasks.values()) writer.write(toString(task) + "\n");
            for (Epic epic : epics.values()) writer.write(toString(epic) + "\n");
            for (Subtask subtask : subtasks.values()) writer.write(toString(subtask) + "\n");

            writer.write("\n");
            writer.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Error while saving tasks to file", e);
        }
    }

    private void loadFromFile() {
        try {
            List<String> lines = Files.readAllLines(file);
            if (lines.size() <= 1) return;

            int maxId = 0;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) {
                    if (i + 1 < lines.size()) {
                        List<Integer> historyIds = historyFromString(lines.get(i + 1));
                        for (Integer id : historyIds) {
                            if (tasks.containsKey(id)) getTask(id);
                            else if (subtasks.containsKey(id)) getSubtask(id);
                            else if (epics.containsKey(id)) getEpic(id);
                        }
                    }
                    break;
                }

                Task task = fromString(line);
                if (task instanceof Subtask subtask) {
                    Epic epic = epics.get(subtask.getEpicId());
                    if (epic != null) addSubtask(subtask);
                }


                if (task.getId() > maxId) maxId = task.getId();
            }

            this.nextId = maxId + 1;
        } catch (IOException e) {
            throw new ManagerSaveException("Error while loading tasks from file", e);
        }
    }


    private String toString(Task task) {
        String epicId = "";
        if (task instanceof Subtask subtask) epicId = String.valueOf(subtask.getEpicId());
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    private Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                tasks.put(id, task);
                return task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                epics.put(id, epic);
                return epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                subtasks.put(id, subtask);
                return subtask;
            }
            default -> throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }


    private static String historyToString(HistoryManager manager) {
        List<String> ids = new ArrayList<>();
        for (Task task : manager.getHistory()) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> result = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            for (String id : value.split(",")) {
                result.add(Integer.parseInt(id));
            }
        }
        return result;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file.toPath(), new InMemoryHistoryManager());
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
}
