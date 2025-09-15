package manager;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager;
    protected int nextId = 1;

    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected int generateId() {
        return nextId++;
    }

    private boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getDuration() != null)
                .anyMatch(task -> {
                    LocalDateTime taskStart = task.getStartTime();
                    LocalDateTime taskEnd = task.getEndTime();
                    return !(newEnd.isBefore(taskStart) || newStart.isAfter(taskEnd));
                });
    }

    @Override
    public int addTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей");
        }

        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return -1;

        if (hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей");
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.getSubtaskIds().add(id);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        List<Subtask> epicSubs = getEpicSubtasks(epic.getId());

        if (epicSubs.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
        } else {
            LocalDateTime start = epicSubs.stream()
                    .map(Subtask::getStartTime)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

            LocalDateTime end = epicSubs.stream()
                    .map(Subtask::getEndTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            Duration duration = epicSubs.stream()
                    .map(Subtask::getDuration)
                    .filter(Objects::nonNull)
                    .reduce(Duration.ZERO, Duration::plus);

            epic.setStartTime(start);
            epic.setDuration(duration);
            epic.setEndTime(end);
        }
        updateEpicStatus(epic);

        return id;
    }

    @Override
    public Task getTask(int id) {
        Task t = tasks.get(id);
        if (t != null) historyManager.add(t);
        return t;
    }

    @Override
    public Epic getEpic(int id) {
        Epic e = epics.get(id);
        if (e != null) historyManager.add(e);
        return e;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask s = subtasks.get(id);
        if (s != null) historyManager.add(s);
        return s;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void updateTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей");
        }
        if (!tasks.containsKey(task.getId())) return;
        Task old = tasks.get(task.getId());
        prioritizedTasks.remove(old);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateEpic(Epic newEpic) {
        Epic old = epics.get(newEpic.getId());
        if (old == null) return;
        old.setName(newEpic.getName());
        old.setDescription(newEpic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) return;
        Subtask old = subtasks.get(subtask.getId());
        prioritizedTasks.remove(old);
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) updateEpicStatus(epic);
    }

    @Override
    public void deleteTask(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            prioritizedTasks.remove(removed);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subId -> {
                Subtask removed = subtasks.remove(subId);
                if (removed != null) {
                    prioritizedTasks.remove(removed);
                    historyManager.remove(subId);
                }
            });
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask removed = subtasks.remove(id);
        if (removed != null) {
            prioritizedTasks.remove(removed);
            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void clearAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void clearAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        prioritizedTasks.removeAll(subtasks.values());
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        prioritizedTasks.removeAll(subtasks.values());
        epics.values().forEach(e -> {
            e.getSubtaskIds().clear();
            updateEpicStatus(e);
        });
        subtasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (Integer id : subIds) {
            Status status = subtasks.get(id).getStatus();
            if (status != Status.NEW) allNew = false;
            if (status != Status.DONE) allDone = false;
        }
        if (allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);
    }
}