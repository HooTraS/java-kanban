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
                    .thenComparingInt(Task::getId)
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private static final Comparator<Task> PRIORITY_COMPARATOR = (a, b) -> {
        LocalDateTime as = a.getStartTime();
        LocalDateTime bs = b.getStartTime();
        int cmp = as.compareTo(bs);
        if (cmp != 0) return cmp;
        return Integer.compare(a.getId(), b.getId());
    };

    private final NavigableSet<Task> prioritized = new TreeSet<>(PRIORITY_COMPARATOR);

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected int generateId() {
        return nextId++;
    }

    protected void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public static boolean isOverlapping(Task a, Task b) {
        if (a == null || b == null) return false;
        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private boolean overlapsAny(Task candidate, int selfId) {
        if (candidate == null) return false;
        if (candidate.getStartTime() == null || candidate.getEndTime() == null) return false;
        return prioritized.stream()
                .filter(t -> t.getId() != selfId)
                .anyMatch(t -> isOverlapping(candidate, t));
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritized);
    }

    @Override
    public int addTask(Task task) {
        int id = generateId();
        task.setId(id);
        if (task.getStartTime() != null && task.getEndTime() != null) {
            if (overlapsAny(task, id)) {
                throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
            }
            prioritized.add(task);
        }
        tasks.put(id, task);
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
        int id = generateId();
        subtask.setId(id);
        if (subtask.getStartTime() != null && subtask.getEndTime() != null) {
            if (overlapsAny(subtask, id)) {
                throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
            }
            prioritized.add(subtask);
        }
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic);
        recalcEpicTimes(epic);
        return id;
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
        if (!tasks.containsKey(task.getId())) return;
        Task old = tasks.get(task.getId());
        if (old.getStartTime() != null) prioritized.remove(old);
        if (task.getStartTime() != null && task.getEndTime() != null) {
            if (overlapsAny(task, task.getId())) {
                // вернуть старое состояние в приоритетную множества не нужно — метод прерывается с исключением
                throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
            }
            prioritized.add(task);
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic newEpic) {
        Epic old = epics.get(newEpic.getId());
        if (old != null) {
            old.setName(newEpic.getName());
            old.setDescription(newEpic.getDescription());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) return;
        Subtask old = subtasks.get(subtask.getId());
        if (old.getStartTime() != null) prioritized.remove(old);
        if (subtask.getStartTime() != null && subtask.getEndTime() != null) {
            if (overlapsAny(subtask, subtask.getId())) {
                throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
            }
            prioritized.add(subtask);
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            recalcEpicTimes(epic);
        }
    }

    @Override
    public void deleteTask(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            if (removed.getStartTime() != null) prioritized.remove(removed);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subId : new ArrayList<>(epic.getSubtaskIds())) {
                Subtask removed = subtasks.remove(subId);
                if (removed != null && removed.getStartTime() != null) prioritized.remove(removed);
                historyManager.remove(subId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            if (subtask.getStartTime() != null) prioritized.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
                recalcEpicTimes(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void clearAllTasks() {
        for (Task t : new ArrayList<>(tasks.values())) {
            if (t.getStartTime() != null) prioritized.remove(t);
            historyManager.remove(t.getId());
        }
        tasks.clear();
    }

    @Override
    public void clearAllEpics() {
        for (Epic e : new ArrayList<>(epics.values())) {
            for (Integer sid : new ArrayList<>(e.getSubtaskIds())) {
                Subtask s = subtasks.remove(sid);
                if (s != null && s.getStartTime() != null) prioritized.remove(s);
                historyManager.remove(sid);
            }
            historyManager.remove(e.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearAllSubtasks() {
        for (Subtask s : new ArrayList<>(subtasks.values())) {
            if (s.getStartTime() != null) prioritized.remove(s);
            historyManager.remove(s.getId());
        }
        subtasks.clear();
        for (Epic e : epics.values()) {
            e.getSubtaskIds().clear();
            updateEpicStatus(e);
            recalcEpicTimes(e);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        List<Status> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::getStatus)
                .collect(Collectors.toList());
        if (statuses.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean allNew = statuses.stream().allMatch(s -> s == Status.NEW);
        boolean allDone = statuses.stream().allMatch(s -> s == Status.DONE);
        if (allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);
    }

    private void recalcEpicTimes(Epic epic) {
        List<Subtask> subs = getEpicSubtasks(epic.getId());
        if (subs.isEmpty()) {
            epic.setDuration(null);
            epic.setStartTime(null);
            return;
        }
        Duration total = Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;
        boolean anyDuration = false;
        for (Subtask s : subs) {
            if (s.getDuration() != null) {
                total = total.plus(s.getDuration());
                anyDuration = true;
            }
            LocalDateTime st = s.getStartTime();
            LocalDateTime en = s.getEndTime();
            if (st != null) {
                if (minStart == null || st.isBefore(minStart)) minStart = st;
            }
            if (en != null) {
                if (maxEnd == null || en.isAfter(maxEnd)) maxEnd = en;
            }
        }
        epic.setDuration(anyDuration ? total : null);
        epic.setStartTime(minStart);
    }
}
