package bravo.logic;

import bravo.dto.TimesheetEntryDTO;

import java.util.List;

public interface TimesheetEntryLogic {

    /**
     * Creates a new timesheet entry.
     *
     * @param timesheetEntry the DTO representing the timesheet entry to create
     * @return the created TimesheetEntryDTO with generated ID and UUID
     */
    TimesheetEntryDTO create(TimesheetEntryDTO timesheetEntry);

    /**
     * Updates an existing timesheet entry.
     *
     * @param timesheetEntry the DTO containing updated timesheet entry data
     * @return the updated TimesheetEntryDTO
     */
    TimesheetEntryDTO update(TimesheetEntryDTO timesheetEntry);

    /**
     * Retrieves all timesheet entries associated with a specific timesheet UUID.
     *
     * @param timesheetUuid the UUID of the timesheet
     * @return a list of TimesheetEntryDTOs for the given timesheet
     */
    List<TimesheetEntryDTO> getByTimesheetUuid(String timesheetUuid);
}