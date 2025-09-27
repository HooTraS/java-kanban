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

    // Навигационное множество: быстрый доступ к соседям по времени
    protected final NavigableSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator
                    .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected int generateId() {
        return nextId++;
    }

    @Override
    public int addTask(Task task) {
        task.setId(generateId());
        ensureNoOverlap(task);
        tasks.put(task.getId(), task);
        addToPrioritizedIfHasStart(task);
        return task.getId();
    }

    @Override
    public int addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        updateEpicTime(epic);
        return epic.getId();
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id=" + epicId + " не найден");
        }
        subtask.setId(generateId());
        ensureNoOverlap(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        addToPrioritizedIfHasStart(subtask);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask.getId();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
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
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return;

        Task old = tasks.get(task.getId());
        removeFromPrioritizedIfHasStart(old);

        try {
            ensureNoOverlap(task);
            tasks.put(task.getId(), task);
            addToPrioritizedIfHasStart(task);
        } catch (RuntimeException ex) {
            addToPrioritizedIfHasStart(old);
            throw ex;
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) return;
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        updateEpicTime(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) return;

        Subtask old = subtasks.get(subtask.getId());
        removeFromPrioritizedIfHasStart(old);

        try {
            ensureNoOverlap(subtask);
            subtasks.put(subtask.getId(), subtask);
            addToPrioritizedIfHasStart(subtask);

            Epic epic = epics.get(subtask.getEpicId());
            updateEpicTime(epic);
        } catch (RuntimeException ex) {
            addToPrioritizedIfHasStart(old);
            throw ex;
        }
    }

    @Override
    public void deleteTask(int id) {
        Task removed = tasks.remove(id);
        removeFromPrioritizedIfHasStart(removed);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic removedEpic = epics.remove(id);
        if (removedEpic != null) {
            removedEpic.getSubtaskIds().stream().forEach(subId -> {
                Subtask s = subtasks.remove(subId);
                removeFromPrioritizedIfHasStart(s);
                historyManager.remove(subId);
            });
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask removed = subtasks.remove(id);
        if (removed != null) {
            removeFromPrioritizedIfHasStart(removed);
            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void clearAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        prioritizedTasks.removeAll(
                tasks.values().stream().filter(t -> t.getStartTime() != null).collect(Collectors.toSet())
        );
        tasks.clear();
    }

    @Override
    public void clearAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);

        prioritizedTasks.removeAll(
                subtasks.values().stream().filter(t -> t.getStartTime() != null).collect(Collectors.toSet())
        );

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);

        prioritizedTasks.removeAll(
                subtasks.values().stream().filter(t -> t.getStartTime() != null).collect(Collectors.toSet())
        );

        epics.values().forEach(e -> {
            e.getSubtaskIds().clear();
            updateEpicStatus(e);
            updateEpicTime(e);
        });
        subtasks.clear();
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
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void updateEpicStatus(Epic epic) {
        List<Subtask> epicSubtasks = getEpicSubtasks(epic.getId());

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = epicSubtasks.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(s -> s.getStatus() == Status.DONE);

        if (allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);
    }

    protected void updateEpicTime(Epic epic) {
        List<Subtask> epicSubtasks = getEpicSubtasks(epic.getId());
        if (epicSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime start = epicSubtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = epicSubtasks.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        long totalMinutes = epicSubtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();

        epic.setStartTime(start);
        epic.setEndTime(end);
        epic.setDuration(Duration.ofMinutes(totalMinutes));
    }

    private void addToPrioritizedIfHasStart(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedIfHasStart(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    private boolean isOverlapping(Task a, Task b) {
        if (a == null || b == null) return false;
        if (a.getStartTime() == null || a.getDuration() == null) return false;
        if (b.getStartTime() == null || b.getDuration() == null) return false;

        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private boolean hasOverlap(Task candidate) {
        if (candidate.getStartTime() == null || candidate.getDuration() == null) return false;

        Task lower = prioritizedTasks.lower(candidate);
        Task higher = prioritizedTasks.higher(candidate);

        return (lower != null && lower.getId() != candidate.getId() && isOverlapping(lower, candidate))
                || (higher != null && higher.getId() != candidate.getId() && isOverlapping(candidate, higher));
    }

    private void ensureNoOverlap(Task candidate) {
        if (hasOverlap(candidate)) {
            throw new IllegalStateException(
                    "Задача пересекается по времени с другой задачей: id=" + candidate.getId());
        }
    }
}

