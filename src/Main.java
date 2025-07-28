import manager.Managers;
import manager.TaskManager;
import model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        int task1Id = manager.addTask(new Task("Переезд", "Организовать переезд", Status.NEW));
        int task2Id = manager.addTask(new Task("Ремонт", "Сделать косметику", Status.NEW));

        int epic1Id = manager.addEpic(new Epic("Праздник", "Организация семейного", Status.NEW));
        int epic2Id = manager.addEpic(new Epic("Покупка", "Купить квартиру", Status.NEW));

        manager.addSubtask(new Subtask("Купить еду", "Список и в магазин", Status.NEW, epic1Id));
        manager.addSubtask(new Subtask("Украсить зал", "Купить декор", Status.NEW, epic1Id));
        manager.addSubtask(new Subtask("Риелтор", "Найти агента", Status.NEW, epic2Id));

        manager.getEpic(epic1Id);
        manager.getTask(task1Id);
        manager.getSubtask(epic2Id);

        for (Task t : manager.getHistory()) {
            System.out.println(t);
        }
    }
}
