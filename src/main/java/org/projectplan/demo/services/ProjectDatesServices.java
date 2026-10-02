package org.projectplan.demo.services;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

@Service
public class ProjectDatesServices {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);

    private LocalDate projectStartDate;

    public void setProjectStartDate(String startDate) {

        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Project start date is required."
            );
        }

        String enteredDate = startDate.trim();

        if (!enteredDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException(
                    "Use yyyy-MM-dd, for example 2026-10-02."
            );
        }

        LocalDate parsedDate;

        try {
            parsedDate = LocalDate.parse(enteredDate, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid calendar date. Please enter a valid date.",
                    e
            );
        }

        this.projectStartDate = parsedDate;
    }

    public LocalDate getProjectStartDate() {
        return projectStartDate;
    }
}