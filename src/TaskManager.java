import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private LocalDate deadline;
    private int priority;

    public Task(String description, LocalDate deadline, int priority) {
        this.description = description;
        this.deadline = deadline;
        this.priority = validatePriority(priority);
        LocalDate.now();
    }

    private int validatePriority(int priority) {
        return Math.max(1, Math.min(5, priority));
    }

    @Override
    public String toString() {
        return String.format("[Priority: %d] %s (Due: %s)", 
            priority, 
            description, 
            deadline.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    } 
}

public class TaskManager implements AutoCloseable {
    private static final String FILE_NAME = "tasks.ser";
    private static final String LOG_FILE = "taskmanager.log";
    private static final Logger LOGGER = Logger.getLogger(TaskManager.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final List<Task> tasks;
    private final Scanner scanner;

    public TaskManager() {
        this.tasks = loadTasks();
        this.scanner = new Scanner(System.in);
        setupLogger();
    }

    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Warning: Could not set up logging: " + e.getMessage());
        }
    }

    public void addTask() {
        try {
            System.out.print("Enter task description: ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) {
                throw new IllegalArgumentException("Description cannot be empty");
            }

            System.out.print("Enter deadline (YYYY-MM-DD): ");
            String deadlineStr = scanner.nextLine().trim();
            LocalDate deadline = LocalDate.parse(deadlineStr, DATE_FORMATTER);

            if (deadline.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Deadline cannot be in the past");
            }

            System.out.print("Enter priority (1-5): ");
            int priority = Integer.parseInt(scanner.nextLine().trim());

            Task task = new Task(description, deadline, priority);
            tasks.add(task);
            saveTasks();
            LOGGER.info("Task added successfully: " + description);
            System.out.println("Task added successfully!");
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format. Please use YYYY-MM-DD");
            LOGGER.warning("Failed to add task: Invalid date format");
        } catch (NumberFormatException e) {
            System.out.println("Error: Priority must be a number between 1 and 5");
            LOGGER.warning("Failed to add task: Invalid priority format");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            LOGGER.warning("Failed to add task: " + e.getMessage());
        }
    }

    public void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }

        // Sort tasks by priority (high to low) and then by deadline
        tasks.sort((t1, t2) -> {
            int priorityCompare = Integer.compare(t2.getPriority(), t1.getPriority());
            return priorityCompare != 0 ? priorityCompare 
                : t1.getDeadline().compareTo(t2.getDeadline());
        });

        System.out.println("\nCurrent Tasks:");
        System.out.println("-------------");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, tasks.get(i));
        }
    }

    public void removeTask() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks to remove.");
            return;
        }

        listTasks();
        System.out.print("Enter task number to remove (1-" + tasks.size() + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index >= 0 && index < tasks.size()) {
                Task removedTask = tasks.remove(index);
                saveTasks();
                LOGGER.info("Task removed: " + removedTask);
                System.out.println("Task removed successfully.");
            } else {
                System.out.println("Error: Invalid task number.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.");
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(tasks);
            LOGGER.info("Tasks saved successfully");
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            LOGGER.severe("Failed to save tasks: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Task> loadTasks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Task>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.severe("Failed to load tasks: " + e.getMessage());
            System.err.println("Error loading tasks. Starting with empty list.");
            return new ArrayList<>();
        }
    }

    @Override
    public void close() {
        scanner.close();
    }

    public static void main(String[] args) {
        try (TaskManager manager = new TaskManager()) {
            while (true) {
                System.out.println("\n=== Task Manager ===");
                System.out.println("1. Add Task");
                System.out.println("2. List Tasks");
                System.out.println("3. Remove Task");
                System.out.println("4. Exit");
                System.out.print("Choose an option (1-4): ");

                try {
                    int choice = Integer.parseInt(manager.scanner.nextLine().trim());
                    System.out.println();

                    switch (choice) {
                        case 1:
                            manager.addTask();
                            break;
                        case 2:
                            manager.listTasks();
                            break;
                        case 3:
                            manager.removeTask();
                            break;
                        case 4:
                            System.out.println("Thank you for using Task Manager!");
                            return;
                        default:
                            System.out.println("Invalid option. Please choose 1-4.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }
            }
        }
    }
}