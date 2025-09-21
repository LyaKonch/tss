package bravo.logic.impl;

import bravo.dao.TimesheetEntryDAO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.TimesheetEntry;
import bravo.logic.TimesheetEntryLogic;
import bravo.mapper.TimesheetEntryMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

/**
 * Stateless EJB implementing the {@link TimesheetEntryLogic} interface.
 * <p>
 * Provides business logic for creating, updating, and retrieving timesheet entries.
 * Handles mapping between {@link TimesheetEntryDTO} and {@link TimesheetEntry} entities
 * and delegates persistence operations to {@link TimesheetEntryDAO}.
 */
@Stateless
public class TimesheetEntryLogicBean implements TimesheetEntryLogic {

    @Inject
    private TimesheetEntryDAO timesheetEntryDAO;

    @Inject
    private TimesheetEntryMapper timesheetEntryMapper;

    /**
     * Creates a new timesheet entry.
     *
     * @param timesheetEntry the DTO representing the timesheet entry to create
     * @return the persisted timesheet entry as a DTO
     */
    @Override
    public TimesheetEntryDTO create(TimesheetEntryDTO timesheetEntry) {
        TimesheetEntry entity = timesheetEntryMapper.toEntity(timesheetEntry);
        timesheetEntryDAO.save(entity);
        return timesheetEntryMapper.toDto(entity);
    }

    /**
     * Updates an existing timesheet entry.
     * <p>
     * Throws {@link EntityNotFoundException} if the entry is not yet persisted (missing ID or UUID).
     * Internally delegates to {@link #create(TimesheetEntryDTO)} for saving.
     *
     * @param dto the DTO representing the timesheet entry to update
     * @return the updated timesheet entry as a DTO
     * @throws EntityNotFoundException if the entry does not have an ID or UUID
     */
    @Override
    public TimesheetEntryDTO update(TimesheetEntryDTO dto) throws EntityNotFoundException {
        if (dto.getId() == null || dto.getUuid() == null) {
            throw new EntityNotFoundException(
                    "Cannot update entries not persisted yet, i.e. one's missing ID/UUID."
            );
        }
        return create(dto);
    }

    /**
     * Retrieves all timesheet entries for a given timesheet UUID.
     * <p>
     * The hours for each entry are calculated and set in the DTO.
     *
     * @param timesheetUuid the UUID of the timesheet
     * @return a list of DTOs representing the timesheet entries
     */
    @Override
    public List<TimesheetEntryDTO> getByTimesheetUuid(String timesheetUuid) {
        List<TimesheetEntryDTO> dtos = timesheetEntryDAO.findProjectedByTimesheetUuid(timesheetUuid);
        dtos.forEach(dto -> dto.setHours(calculateHours(dto)));
        return dtos;
    }

    /**
     * Calculates the number of hours for a given timesheet entry based on start and end time.
     *
     * @param entry the timesheet entry DTO
     * @return the calculated hours
     */
    private double calculateHours(TimesheetEntryDTO entry) {
        return java.time.Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes() / 60.0;
    }
}
