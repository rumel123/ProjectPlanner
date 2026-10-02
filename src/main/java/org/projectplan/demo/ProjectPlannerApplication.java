package org.projectplan.demo;

import org.projectplan.demo.entity.ScheduledTask;
import org.projectplan.demo.entity.Task;
import org.projectplan.demo.services.ProjectDatesServices;
import org.projectplan.demo.services.ScheduleServices;
import org.projectplan.demo.services.TaskServices;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class ProjectPlannerApplication implements CommandLineRunner {
    private final ProjectDatesServices projectDatesServices;
    private final TaskServices taskServices;
    private final ScheduleServices scheduleServices;

    public ProjectPlannerApplication(ProjectDatesServices projectDatesServices,
                                     TaskServices taskServices,
                                     ScheduleServices scheduleServices) {
        this.projectDatesServices = projectDatesServices;
        this.taskServices = taskServices;
        this.scheduleServices = scheduleServices;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProjectPlannerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                displayMenu();
                String choice = read(scanner, "Enter your choice (0-5): ");
                if (choice == null || choice.equals("0")) {
                    System.out.println("Goodbye!");
                    return;
                }
                try {
                    switch (choice) {
                        case "1" -> setProjectStartDate(scanner);
                        case "2" -> addTask(scanner);
                        case "3" -> viewTasks();
                        case "4" -> editTask(scanner);
                        case "5" -> viewSchedule();
                        default -> System.out.println("Invalid choice. Enter a number from 0 to 5.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private void setProjectStartDate(Scanner scanner) {
        while (true) {
            String input = read(scanner, "Enter project start date (yyyy-MM-dd), or 0 to go back: ");
            if (input == null || input.equals("0")) {
                return;
            }
            try {
                projectDatesServices.setProjectStartDate(input);
                System.out.println("Project start date saved: " + projectDatesServices.getProjectStartDate());
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void addTask(Scanner scanner) {
        String name = read(scanner, "Task name: ");
        String duration = read(scanner, "Duration in calendar days: ");
        String dependencies = read(scanner, "Dependency IDs (comma separated, blank for none): ");
        if (name == null || duration == null || dependencies == null) {
            return;
        }
        Task task = taskServices.addTask(name, parseDuration(duration), parseDependencies(dependencies));
        System.out.println("Task added. ID: " + task.getId());
    }

    private void viewTasks() {
        List<Task> tasks = taskServices.getTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks yet. Choose option 2 to add one.");
            return;
        }
        System.out.println("ID | Task | Duration | Dependencies");
        for (Task task : tasks) {
            System.out.println(task.getId() + " | " + task.getName() + " | "
                    + task.getDurationDays() + " day(s) | " + task.getDependencies());
        }
    }

    private void editTask(Scanner scanner) {
        viewTasks();
        if (taskServices.getTasks().isEmpty()) {
            return;
        }
        String idInput = read(scanner, "Task ID to edit: ");
        if (idInput == null) {
            return;
        }
        Task current = taskServices.getTask(parseId(idInput));
        System.out.println("Leave blank to keep the current value.");
        String name = read(scanner, "New name: ");
        String duration = read(scanner, "New duration: ");
        String dependencies = read(scanner, "New dependency IDs (use - to clear): ");
        if (name == null || duration == null || dependencies == null) {
            return;
        }
        String updatedName = name.isBlank() ? current.getName() : name;
        int updatedDuration = duration.isBlank() ? current.getDurationDays() : parseDuration(duration);
        List<Long> updatedDependencies = dependencies.isBlank() ? current.getDependencies()
                : dependencies.equals("-") ? List.of() : parseDependencies(dependencies);
        taskServices.editTask(current.getId(), updatedName, updatedDuration, updatedDependencies);
        System.out.println("Task " + current.getId() + " updated.");
    }

    private void viewSchedule() {
        List<ScheduledTask> schedule = scheduleServices.generateSchedule(
                projectDatesServices.getProjectStartDate(), taskServices.getTasks());
        System.out.println("ID | Task | Start date | End date");
        for (ScheduledTask item : schedule) {
            System.out.println(item.getTask().getId() + " | " + item.getTask().getName()
                    + " | " + item.getStartDate() + " | " + item.getEndDate());
        }
    }

    private void displayMenu() {
        System.out.println();
        System.out.println("+----------------------------------------------+");
        System.out.println("|              PROJECT PLANNER                 |");
        System.out.println("+----------------------------------------------+");
        System.out.println("| 1. Set project start date                    |");
        System.out.println("| 2. Add task                                  |");
        System.out.println("| 3. View tasks                                |");
        System.out.println("| 4. Edit task                                 |");
        System.out.println("| 5. Generate and view schedule                |");
        System.out.println("| 0. Exit                                      |");
        System.out.println("+----------------------------------------------+");
        LocalDate date = projectDatesServices.getProjectStartDate();
        System.out.println("Current project start date: " + (date == null ? "Not set" : date));
    }

    private String read(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }

    private int parseDuration(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Duration must be a whole number of days.");
        }
    }

    private long parseId(String input) {
        try {
            long id = Long.parseLong(input.trim());
            if (id < 1) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Task IDs must be positive whole numbers.");
        }
    }

    private List<Long> parseDependencies(String input) {
        if (input.isBlank()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String part : input.split(",", -1)) {
            ids.add(parseId(part));
        }
        return ids;
    }
}
