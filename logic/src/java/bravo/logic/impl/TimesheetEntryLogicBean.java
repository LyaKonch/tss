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

@Stateless
public class TimesheetEntryLogicBean implements TimesheetEntryLogic {

    @Inject
    private TimesheetEntryDAO timesheetEntryDAO;

    @Inject
    private TimesheetEntryMapper timesheetEntryMapper;

    @Override
    public TimesheetEntryDTO create(TimesheetEntryDTO timesheetEntry) {
        TimesheetEntry entity = timesheetEntryMapper.toEntity(timesheetEntry);
        timesheetEntryDAO.save(entity);
        return timesheetEntryMapper.toDto(entity);
    }

    @Override
    public TimesheetEntryDTO update(TimesheetEntryDTO dto) throws EntityNotFoundException {
        if (dto.getId() == null || dto.getUuid() == null)
            throw new EntityNotFoundException("Cannot update entries not persisted yet, i.e. one's missing ID/UUID.");
        // Can reuse create. The Mapper checks if a new Entity should be created or not.
        return create(dto);
    }

    @Override
    public List<TimesheetEntryDTO> getByTimesheetUuid(String timesheetUuid) {
        List<TimesheetEntryDTO> dtos = timesheetEntryDAO.findProjectedByTimesheetUuid(timesheetUuid);
        dtos.forEach(dto -> dto.setHours(calculateHours(dto)));
        return dtos;
    }

    private double calculateHours(TimesheetEntryDTO entry) {
        return java.time.Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes() / 60.0;
    }
}
