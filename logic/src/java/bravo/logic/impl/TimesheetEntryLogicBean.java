package bravo.logic.impl;

import bravo.dao.TimesheetDAO;
import bravo.dao.TimesheetEntryDAO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;
import bravo.logic.TimesheetEntryLogic;
import bravo.mapper.TimesheetEntryMapper;
import bravo.utitity.AuthorizationService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Stateless
public class TimesheetEntryLogicBean implements TimesheetEntryLogic {

    @Inject
    private AuthorizationService authService;

    @Inject
    private TimesheetEntryDAO timesheetEntryDAO;

    @Inject
    private TimesheetDAO timesheetDAO;

    @Inject
    private TimesheetEntryMapper timesheetEntryMapper;

    @Override
    public TimesheetEntryDTO create(TimesheetEntryDTO timesheetEntry) {
        TimesheetEntry entity = timesheetEntryMapper.toEntity(timesheetEntry);
        Timesheet parentTimesheet = timesheetDAO.findByUuid(timesheetEntry.getTimesheetUuid());
        if (!authService.canEditTimesheetEntry(parentTimesheet)) {
            throw new SecurityException("User is not authorized to create entries for this timesheet.");
        }
        timesheetEntryDAO.save(entity);
        return timesheetEntryMapper.toDto(entity);
    }

    @Override
    public TimesheetEntryDTO update(TimesheetEntryDTO dto) throws EntityNotFoundException {
        if (dto.getId() == null || dto.getUuid() == null)
            throw new EntityNotFoundException("Cannot update entries not persisted yet, i.e. one's missing ID/UUID.");
        Timesheet parentTimesheet = timesheetDAO.findByUuid(dto.getTimesheetUuid());
        if (!authService.canEditTimesheetEntry(parentTimesheet)) {
            throw new SecurityException("User is not authorized to create entries for this timesheet.");
        }
        return create(dto);
    }

    @Override
    public List<TimesheetEntryDTO> getByTimesheetUuid(String timesheetUuid) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        List<TimesheetEntryDTO> dtos = timesheetEntryDAO.findProjectedByTimesheetUuid(timesheetUuid);
        dtos.forEach(dto -> dto.setHours(calculateHours(dto)));
        return dtos;
    }

    private double calculateHours(TimesheetEntryDTO entry) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        return java.time.Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes() / 60.0;
    }
}
