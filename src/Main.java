public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Переезд", "Организовать переезд", manager.generateId(), Status.NEW);
        Task task2 = new Task("Ремонт", "Косметический ремонт в квартире", manager.generateId(), Status.IN_PROGRESS);
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Праздник", "Организация семейного праздника", manager.generateId());
        Epic epic2 = new Epic("Покупка квартиры", "Полный цикл покупки", manager.generateId());
        manager.addEpic(epic1);
        manager.addEpic(epic2);

        Subtask sub1 = new Subtask("Купить продукты", "Составить список и купить", manager.generateId(), Status.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Украсить зал", "Купить декор и украсить", manager.generateId(), Status.NEW, epic1.getId());
        Subtask sub3 = new Subtask("Найти агента", "Выбрать риелтора", manager.generateId(), Status.NEW, epic2.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        manager.addSubtask(sub3);

        System.out.println("=== Все задачи ===");
        System.out.println(manager.getAllTasks());

        System.out.println("=== Все эпики ===");
        System.out.println(manager.getAllEpics());

        System.out.println("=== Все подзадачи ===");
        System.out.println(manager.getAllSubtasks());

        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.DONE);
        sub3.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);
        manager.updateSubtask(sub3);

        System.out.println("=== Эпики после изменения статусов подзадач ===");
        System.out.println(manager.getAllEpics());

        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());

        System.out.println("=== После удаления задачи и эпика ===");
        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
    }
}