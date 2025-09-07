package manager;

import java.io.File;

public class Managers {
    private static final String FILE_PATH = "resources/tasks.csv";

    public static TaskManager getDefault() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            return FileBackedTaskManager.loadFromFile(file);
        } else {
            return new FileBackedTaskManager(getDefaultHistory(), file);
        }
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
