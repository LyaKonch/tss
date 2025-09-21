package bravo.mapper;

import bravo.dao.TimesheetDAO;
import bravo.dto.TimesheetDTO;
import bravo.entities.Timesheet;
import bravo.logic.TimesheetLogic;
import bravo.logic.impl.TimesheetLogicBean;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;


/**
 * Mapper for converting between {@link Timesheet} entities and {@link TimesheetDTO} objects.
 * <p>
 * Handles simple field mapping (status, dates, signatures) and basic entity instantiation.
 * Does not handle related entries directly; those are managed separately via {@link bravo.mapper.TimesheetEntryMapper}.
 */
@ApplicationScoped
public class TimesheetMapper extends AbstractMapper<Timesheet, TimesheetDTO> {

    @Inject
    TimesheetDAO timesheetDAO;

    @EJB
    TimesheetLogic timesheetBean;
    /**
     * Converts a {@link TimesheetDTO} to a {@link Timesheet} entity.
     * <p>
     * Creates a new entity instance and sets its fields. If the DTO contains
     * an ID or UUID, these are applied to the entity as well.
     *
     * @param dto the DTO to convert
     * @return the mapped {@link Timesheet} entity
     */
    @Override
    public Timesheet toEntity(TimesheetDTO dto) {
        if(dto.getId() != null){return timesheetDAO.findByUuid(dto.getUuid());}
        Timesheet entity = new Timesheet();

        if (dto.getId() != null) entity.setId(dto.getId());
        if (dto.getUuid() != null) entity.setUuid(UUID.fromString(dto.getUuid()));

        entity.setStatus(dto.getStatus());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setSignedByEmployee(dto.getSignedByEmployee());
        entity.setSignedBySupervisor(dto.getSignedBySupervisor());
        return entity;
    }

    /**
     * Converts a {@link Timesheet} entity to a {@link TimesheetDTO}.
     * <p>
     * Maps all simple fields. The {@code hoursDue} field is intentionally
     * left unmapped here since it is calculated at the logic layer
     * (e.g. in {@code TimesheetLogicBean}).
     *
     * @param entity the entity to convert
     * @return the mapped {@link TimesheetDTO}
     */
    @Override
    public TimesheetDTO toDto(Timesheet entity) {
        TimesheetDTO dto = new TimesheetDTO();

        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().toString());

        dto.setStatus(entity.getStatus());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setSignedByEmployee(entity.getSignedByEmployee());
        dto.setSignedBySupervisor(entity.getSignedBySupervisor());
        dto.setHoursDue(timesheetBean.getReportedHours(entity));
        return dto;
    }
}
