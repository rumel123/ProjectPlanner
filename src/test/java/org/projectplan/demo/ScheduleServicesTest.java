package org.projectplan.demo;

import org.junit.jupiter.api.Test;
import org.projectplan.demo.entity.ScheduledTask;
import org.projectplan.demo.entity.Task;
import org.projectplan.demo.services.ScheduleServices;
import org.projectplan.demo.services.TaskServices;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleServicesTest {
    private final ScheduleServices scheduler = new ScheduleServices();

    @Test
    void schedulesParallelTasksAndWaitsForAllDependencies() {
        List<Task> tasks = List.of(
                new Task(1, "A", 2, List.of()),
                new Task(2, "B", 3, List.of(1L)),
                new Task(3, "C", 1, List.of(1L)),
                new Task(4, "D", 2, List.of(2L, 3L))
        );

        List<ScheduledTask> result = scheduler.generateSchedule(LocalDate.of(2026, 10, 5), tasks);

        assertEquals(LocalDate.of(2026, 10, 5), find(result, 1).getStartDate());
        assertEquals(LocalDate.of(2026, 10, 6), find(result, 1).getEndDate());
        assertEquals(LocalDate.of(2026, 10, 7), find(result, 2).getStartDate());
        assertEquals(LocalDate.of(2026, 10, 7), find(result, 3).getStartDate());
        assertEquals(LocalDate.of(2026, 10, 10), find(result, 4).getStartDate());
        assertEquals(LocalDate.of(2026, 10, 11), find(result, 4).getEndDate());
    }

    @Test
    void detectsMissingDependenciesAndCycles() {
        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
                () -> scheduler.generateSchedule(LocalDate.of(2026, 10, 5),
                        List.of(new Task(1, "A", 1, List.of(99L)))));
        assertTrue(missing.getMessage().contains("missing task"));

        IllegalArgumentException cycle = assertThrows(IllegalArgumentException.class,
                () -> scheduler.generateSchedule(LocalDate.of(2026, 10, 5), List.of(
                        new Task(1, "A", 1, List.of(2L)),
                        new Task(2, "B", 1, List.of(1L)))));
        assertTrue(cycle.getMessage().contains("Circular dependency"));
    }

    @Test
    void validatesTaskInputAndCountsWeekends() {
        TaskServices tasks = new TaskServices();
        assertThrows(IllegalArgumentException.class, () -> tasks.addTask("A", 0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> tasks.addTask("A", 1, List.of(99L)));
        Task task = tasks.addTask("A", 3, List.of());
        assertThrows(IllegalArgumentException.class,
                () -> tasks.editTask(task.getId(), "A", 3, List.of(task.getId())));

        ScheduledTask result = scheduler.generateSchedule(LocalDate.of(2026, 10, 9),
                tasks.getTasks()).get(0);
        assertEquals(LocalDate.of(2026, 10, 11), result.getEndDate());
    }

    private ScheduledTask find(List<ScheduledTask> schedule, long id) {
        return schedule.stream()
                .filter(item -> item.getTask().getId() == id)
                .findFirst().orElseThrow();
    }
}
