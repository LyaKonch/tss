package bravo.logic.impl;

import bravo.dao.TimesheetDAO;
import bravo.dao.TimesheetEntryDAO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;
import bravo.enums.ContractStatus;
import bravo.enums.TimesheetStatus;
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

    private boolean isEditable(TimesheetEntryDTO dto) {
        if (dto.getId() == null)
            throw new EntityNotFoundException("Cannot update entry not persisted yet, i.e. one's missing ID.");
        TimesheetEntry entity = timesheetEntryDAO.findByUuid(dto.getUuid());
        if (entity == null) throw new EntityNotFoundException("Cannot update entry was not found.");
        if (entity.getTimesheet() == null || entity.getTimesheet().getContract() == null)
            throw new IllegalStateException("Cannot update entry if its not editable.");
        return entity.getTimesheet().getStatus() == TimesheetStatus.IN_PROGRESS
                && entity.getTimesheet().getContract().getStatus() == ContractStatus.STARTED;
    }

    @Override
    public TimesheetEntryDTO update(TimesheetEntryDTO dto) throws EntityNotFoundException {
        // Can reuse create. The Mapper checks if a new Entity should be created or not.
        return isEditable(dto) ? create(dto) : null;
    }

    @Override
    public void delete(TimesheetEntryDTO dto) {
        if (isEditable(dto)) timesheetEntryDAO.delete(timesheetEntryDAO.findByUuid(dto.getUuid()));
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
