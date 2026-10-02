package org.projectplan.demo.entity;

import java.util.List;

public class Task {
    private final long id;
    private final String name;
    private final int durationDays;
    private final List<Long> dependencies;

    public Task(long id, String name, int durationDays, List<Long> dependencies) {
        this.id = id;
        this.name = name;
        this.durationDays = durationDays;
        this.dependencies = List.copyOf(dependencies);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public List<Long> getDependencies() {
        return dependencies;
    }
}
