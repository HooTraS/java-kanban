package model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int id, Status status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                '}';
    }
}