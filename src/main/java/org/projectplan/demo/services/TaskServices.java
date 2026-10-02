package org.projectplan.demo.services;

import org.projectplan.demo.entity.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TaskServices {
    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private long nextId = 1;

    public Task addTask(String name, int durationDays, List<Long> dependencies) {
        validate(null, name, durationDays, dependencies);
        Task task = new Task(nextId++, name.trim(), durationDays, dependencies);
        tasks.put(task.getId(), task);
        return task;
    }

    public Task editTask(long id, String name, int durationDays, List<Long> dependencies) {
        getTask(id);
        validate(id, name, durationDays, dependencies);
        Task task = new Task(id, name.trim(), durationDays, dependencies);
        tasks.put(id, task);
        return task;
    }

    public Task getTask(long id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new IllegalArgumentException("Task ID " + id + " does not exist.");
        }
        return task;
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    private void validate(Long taskId, String name, int durationDays, List<Long> dependencies) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Task name is required.");
        }
        if (durationDays < 1) {
            throw new IllegalArgumentException("Duration must be at least 1 day.");
        }
        if (dependencies == null) {
            throw new IllegalArgumentException("Dependencies are required; use an empty list for none.");
        }
        Set<Long> seen = new HashSet<>();
        for (Long dependencyId : dependencies) {
            if (dependencyId == null || !tasks.containsKey(dependencyId)) {
                throw new IllegalArgumentException("Dependency ID " + dependencyId + " does not exist. Add it first.");
            }
            if (dependencyId.equals(taskId)) {
                throw new IllegalArgumentException("A task cannot depend on itself.");
            }
            if (!seen.add(dependencyId)) {
                throw new IllegalArgumentException("Dependency ID " + dependencyId + " is repeated.");
            }
        }
    }
}
