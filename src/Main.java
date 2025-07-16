import manager.TaskManager;
import model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        int id1 = manager.addTask(new Task("Переезд", "Организовать переезд", Status.NEW));
        int id2 = manager.addTask(new Task("Ремонт", "Косметический ремонт в квартире", Status.IN_PROGRESS));

        int epicId1 = manager.addEpic(new Epic("Праздник", "Организация семейного праздника", Status.NEW));
        int epicId2 = manager.addEpic(new Epic("Покупка квартиры", "Полный цикл покупки", Status.NEW));

        int sub1Id = manager.addSubtask(new Subtask("Купить продукты", "Составить список и купить", Status.NEW, epicId1));
        int sub2Id = manager.addSubtask(new Subtask("Украсить зал", "Купить декор и украсить", Status.NEW, epicId1));
        int sub3Id = manager.addSubtask(new Subtask("Найти агента", "Выбрать риелтора", Status.NEW, epicId2));

        System.out.println("=== Все задачи ===");
        System.out.println(manager.getAllTasks());

        System.out.println("=== Все эпики ===");
        System.out.println(manager.getAllEpics());

        System.out.println("=== Все подзадачи ===");
        System.out.println(manager.getAllSubtasks());

        Subtask sub1 = manager.getSubtaskById(sub1Id);
        Subtask sub2 = manager.getSubtaskById(sub2Id);
        Subtask sub3 = manager.getSubtaskById(sub3Id);

        if (sub1 != null) {
            sub1.setStatus(Status.DONE);
            manager.updateSubtask(sub1);
        }

        if (sub2 != null) {
            sub2.setStatus(Status.DONE);
            manager.updateSubtask(sub2);
        }

        if (sub3 != null) {
            sub3.setStatus(Status.IN_PROGRESS);
            manager.updateSubtask(sub3);
        }

        System.out.println("=== Эпики после изменения статусов подзадач ===");
        System.out.println(manager.getAllEpics());

        manager.deleteTaskById(id1);
        manager.deleteEpicById(epicId1);

        System.out.println("=== После удаления задачи и эпика ===");
        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
    }
}
