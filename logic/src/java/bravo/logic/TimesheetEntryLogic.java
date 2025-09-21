package bravo.logic;

import bravo.dto.TimesheetEntryDTO;

import java.util.List;

public interface TimesheetEntryLogic {

    /**
     * Create a new timesheet entry.
     */
    TimesheetEntryDTO create(TimesheetEntryDTO timesheetEntry);

    /**
     * Update an existing timesheet entry.
     */
    TimesheetEntryDTO update(TimesheetEntryDTO timesheetEntry);

    void delete(TimesheetEntryDTO timesheetEntry);

    /**
     * Get all entry DTOs for a timesheet.
     */
    List<TimesheetEntryDTO> getByTimesheetUuid(String timesheetUuid);
}
