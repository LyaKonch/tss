package bravo.mapper;

import bravo.dao.TimesheetDAO;
import bravo.dao.TimesheetEntryDAO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Mapper for converting between {@link TimesheetEntry} entities and {@link TimesheetEntryDTO} objects.
 * <p>
 * Handles both simple fields (type, description, start/end time, entry date) and relationships
 * ({@link Timesheet}). Can create new {@link TimesheetEntry} instances or update existing ones.
 */
@ApplicationScoped
public class TimesheetEntryMapper extends AbstractMapper<TimesheetEntry, TimesheetEntryDTO> {

    @Inject
    private TimesheetDAO timesheetDAO;

    @Inject
    private TimesheetEntryDAO timesheetEntryDAO;

    /**
     * Converts a {@link TimesheetEntryDTO} to a {@link TimesheetEntry} entity.
     * <p>
     * If the DTO contains an ID, the existing entity is retrieved and updated.
     * Otherwise, a new entity is created. The associated {@link Timesheet} is resolved
     * using its UUID.
     *
     * @param dto the DTO to convert
     * @return the mapped {@link TimesheetEntry} entity
     */
    @Override
    public TimesheetEntry toEntity(TimesheetEntryDTO dto) {
        TimesheetEntry entity;

        // If id is null, we are creating a new entity, otherwise we are updating the existing one
        if (dto.getId() == null) entity = new TimesheetEntry();
        else entity = timesheetEntryDAO.findByUuid(dto.getUuid());


        if (dto.getId() != null) entity.setId(dto.getId());
        if (dto.getUuid() != null) entity.setUuid(UUID.fromString(dto.getUuid()));

        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        // hours is a transient value and not saved to DB
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setEntryDate(dto.getEntryDate());

        Timesheet timesheet = timesheetDAO.findByUuid(dto.getTimesheetUuid());
        entity.setTimesheet(timesheet);

        return entity;
    }

    /**
     * Converts a {@link TimesheetEntry} entity to a {@link TimesheetEntryDTO}.
     * <p>
     * Maps all simple fields and stores the related {@link Timesheet} as a UUID.
     * The {@code hours} field is not mapped here because it is transient and
     * calculated in {@code TimesheetEntryLogicBean}.
     *
     * @param entity the entity to convert
     * @return the mapped {@link TimesheetEntryDTO}
     */
    @Override
    public TimesheetEntryDTO toDto(TimesheetEntry entity) {
        TimesheetEntryDTO dto = new TimesheetEntryDTO();

        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().toString());

        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setStartTime(entity.getStartTime());
        // TODO: Calculate here or give as parameter or ...?
        // TODO: Or call .setHoursDue() in TimesheetEntryLogicBean? <- using this right now
//        dto.setHours(999);
        dto.setEndTime(entity.getEndTime());
        dto.setEntryDate(entity.getEntryDate());

        dto.setTimesheetUuid(entity.getTimesheet().getUuid().toString());

        return dto;
    }
}
