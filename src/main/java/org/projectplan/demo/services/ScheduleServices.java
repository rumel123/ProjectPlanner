package org.projectplan.demo.services;

import org.projectplan.demo.entity.ScheduledTask;
import org.projectplan.demo.entity.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Service
public class ScheduleServices {
    public List<ScheduledTask> generateSchedule(LocalDate projectStartDate, List<Task> tasks) {
        if (projectStartDate == null) {
            throw new IllegalArgumentException("Set the project start date first.");
        }
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Add at least one task first.");
        }

        Map<Long, Task> taskById = new LinkedHashMap<>();
        for (Task task : tasks) {
            if (task == null || taskById.putIfAbsent(task.getId(), task) != null) {
                throw new IllegalArgumentException("Task IDs must be unique.");
            }
            if (task.getDurationDays() < 1) {
                throw new IllegalArgumentException("Task " + task.getId() + " has an invalid duration.");
            }
        }

        Map<Long, Integer> remainingDependencies = new HashMap<>();
        Map<Long, List<Long>> dependents = new HashMap<>();
        for (Task task : tasks) {
            Set<Long> uniqueDependencies = new HashSet<>();
            for (Long dependencyId : task.getDependencies()) {
                if (!taskById.containsKey(dependencyId)) {
                    throw new IllegalArgumentException("Task " + task.getId()
                            + " depends on missing task " + dependencyId + ".");
                }
                if (!uniqueDependencies.add(dependencyId)) {
                    throw new IllegalArgumentException("Task " + task.getId()
                            + " repeats dependency " + dependencyId + ".");
                }
                dependents.computeIfAbsent(dependencyId, ignored -> new ArrayList<>()).add(task.getId());
            }
            remainingDependencies.put(task.getId(), uniqueDependencies.size());
        }

        Queue<Long> ready = new ArrayDeque<>();
        for (Task task : tasks) {
            if (remainingDependencies.get(task.getId()) == 0) {
                ready.add(task.getId());
            }
        }

        Map<Long, ScheduledTask> scheduled = new HashMap<>();
        while (!ready.isEmpty()) {
            Task task = taskById.get(ready.remove());
            LocalDate startDate = projectStartDate;
            for (Long dependencyId : task.getDependencies()) {
                LocalDate nextDay = scheduled.get(dependencyId).getEndDate().plusDays(1);
                if (nextDay.isAfter(startDate)) {
                    startDate = nextDay;
                }
            }
            LocalDate endDate = startDate.plusDays(task.getDurationDays() - 1L);
            scheduled.put(task.getId(), new ScheduledTask(task, startDate, endDate));

            for (Long dependentId : dependents.getOrDefault(task.getId(), List.of())) {
                int remaining = remainingDependencies.merge(dependentId, -1, Integer::sum);
                if (remaining == 0) {
                    ready.add(dependentId);
                }
            }
        }

        if (scheduled.size() != tasks.size()) {
            throw new IllegalArgumentException("Circular dependency detected. Edit the task dependencies.");
        }
        return scheduled.values().stream()
                .sorted(Comparator.comparing(ScheduledTask::getStartDate)
                        .thenComparing(item -> item.getTask().getId()))
                .toList();
    }
}
