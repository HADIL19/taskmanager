import java.io.*;
import java.util.*;

class Task implements Serializable {
    private String description;
    private String deadline;
    private int priority;

    public Task(String description, String deadline, int priority) {
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
    }

    public String toString() {
        return "[Priority: " + priority + "] " + description + " (Due: " + deadline + ")";
    }

    public int getPriority() {
        return priority;
    }
}

public class TaskManager {
    private static final String FILE_NAME = "tasks.ser";
    private List<Task> tasks;

    public TaskManager() {
        tasks = loadTasks();
    }

    public void addTask(String description, String deadline, int priority) {
        tasks.add(new Task(description, deadline, priority));
        saveTasks();
    }

    public void listTasks() {
        tasks.sort(Comparator.comparingInt(Task::getPriority));
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    public void removeTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
            saveTasks();
            System.out.println("Task removed successfully.");
        } else {
            System.out.println("Invalid task number.");
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(tasks);
        } catch (IOException e) {
            System.out.println("Error saving tasks.");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Task> loadTasks() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<Task>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            TaskManager manager = new TaskManager();

            while (true) {
                System.out.println("\nTask Manager");
                System.out.println("1. Add Task");
                System.out.println("2. List Tasks");
                System.out.println("3. Remove Task");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter task description: ");
                        String description = scanner.nextLine();
                        System.out.print("Enter deadline (YYYY-MM-DD): ");
                        String deadline = scanner.nextLine();
                        System.out.print("Enter priority (1-5): ");
                        int priority = scanner.nextInt();
                        manager.addTask(description, deadline, priority);
                        break;
                    case 2:
                        manager.listTasks();
                        break;
                    case 3:
                        System.out.print("Enter task number to remove: ");
                        int index = scanner.nextInt();
                        manager.removeTask(index);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        }
    }
}
