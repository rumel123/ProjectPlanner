package org.projectplan.demo.entity;

import java.time.LocalDate;

public class ScheduledTask {
    private final Task task;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ScheduledTask(Task task, LocalDate startDate, LocalDate endDate) {
        this.task = task;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Task getTask() {
        return task;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
