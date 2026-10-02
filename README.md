# Project Planner

Run the project with Maven:

```powershell
mvn clean package
java -jar target\project-planner-0.0.1-SNAPSHOT.jar
```

This project uses Java 17 or newer and Maven.

## Use the menu

1. Choose **1** and enter the project start date in `yyyy-MM-dd` format.
2. Choose **2** to add each task. Enter a name, duration in days, and dependency IDs separated by commas. Leave dependencies blank for none. Add prerequisite tasks first.
3. Choose **3** to view task IDs.
4. Choose **4** to edit a task. Leave a field blank to keep its value, or enter `-` for dependencies to clear them.
5. Choose **5** to generate the schedule.

Data is kept in memory while the app runs. Exiting clears it.

## Scheduling rules

- Durations count calendar days, including weekends and holidays.
- Start and end dates are inclusive. A 2-day task starting October 5 ends October 6.
- Tasks with no dependencies start on the project start date.
- A dependent task starts the day after its latest-finishing dependency.
- Independent tasks can run in parallel.
- Invalid task data or a circular dependency produces an error message.

Example with project start date `2026-10-05`:

| Task | Duration | Depends on | Start | End |
|---|---:|---|---|---|
| A | 2 | None | 2026-10-05 | 2026-10-06 |
| B | 3 | A | 2026-10-07 | 2026-10-09 |
| C | 1 | A | 2026-10-07 | 2026-10-07 |
| D | 2 | B, C | 2026-10-10 | 2026-10-11 |

`ScheduleServices` processes tasks in dependency order. If it cannot process all tasks, it reports a circular dependency.

## What is a dependency ID?

The app assigns an ID to each task when you add it: `Task added. ID: 1`, then `2`, `3`, and so on. A **dependency ID** is the ID of a task that must finish before the new task can start. Use menu option **3** to see task IDs.

- If the new task has no prerequisite, press **Enter** at the dependency prompt. Do not type `none`.
- If it depends on task 1, enter `1`.
- If it depends on tasks 2 and 3, enter `2,3`.
- Add a prerequisite task before entering its ID in another task.

## Happy path: sample inputs and expected answers

Start the app and enter these values in order. A blank dependency entry means **press Enter without typing anything**.

| Menu choice | Prompt | Input | Expected answer |
|---|---|---|---|
| `1` | Project start date | `2026-10-05` | `Project start date saved: 2026-10-05` |
| `2` | Task name / duration / dependencies | `A` / `2` / blank | `Task added. ID: 1` |
| `2` | Task name / duration / dependencies | `B` / `3` / `1` | `Task added. ID: 2` |
| `2` | Task name / duration / dependencies | `C` / `1` / `1` | `Task added. ID: 3` |
| `2` | Task name / duration / dependencies | `D` / `2` / `2,3` | `Task added. ID: 4` |
| `3` | View tasks | — | Tasks 1–4 and their dependency IDs appear. |
| `5` | Generate schedule | — | The schedule below appears. |

Expected schedule:

```text
ID | Task | Start date | End date
1 | A | 2026-10-05 | 2026-10-06
2 | B | 2026-10-07 | 2026-10-09
3 | C | 2026-10-07 | 2026-10-07
4 | D | 2026-10-10 | 2026-10-11
```

Tasks B and C start together because both depend on A. Task D waits for both B and C; B finishes later, so D starts on October 10.

## Wrong path: sample inputs and expected answers

Each row is a separate example. If an error occurs while adding or editing a task, that change is not saved.

| Scenario | Input | Expected answer |
|---|---|---|
| Generate a schedule before setting a start date | Choose `5` | `Error: Set the project start date first.` |
| Enter an impossible date | Choose `1`, enter `2026-02-30` | `Error: Invalid calendar date. Please enter a valid date.` The app asks for a date again; enter `0` to return to the menu. |
| Enter a zero-day task | Choose `2`, then `A` / `0` / blank | `Error: Duration must be at least 1 day.` |
| Depend on a task that does not exist | Choose `2`, then `B` / `3` / `99` | `Error: Dependency ID 99 does not exist. Add it first.` |
| Repeat a dependency | After task 1 exists, choose `2`, then `B` / `3` / `1,1` | `Error: Dependency ID 1 is repeated.` |
| Make a task depend on itself | Choose `4`, select task ID `1`, keep name and duration blank, enter dependency `1` | `Error: A task cannot depend on itself.` |
| Create a circular dependency | Add A as ID 1; add B as ID 2 depending on `1`. Edit A and set its dependency to `2`. Then choose `5`. | `Error: Circular dependency detected. Edit the task dependencies.` |
| Choose an unknown menu option | Enter `9` | `Invalid choice. Enter a number from 0 to 5.` |
