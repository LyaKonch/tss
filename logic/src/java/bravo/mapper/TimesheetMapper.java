package bravo.mapper;

import bravo.dto.TimesheetDTO;
import bravo.entities.Timesheet;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class TimesheetMapper extends AbstractMapper<Timesheet, TimesheetDTO> {

    @Override
    public Timesheet toEntity(TimesheetDTO dto) {
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

    @Override
    public TimesheetDTO toDto(Timesheet entity) {
        TimesheetDTO dto = new TimesheetDTO();

        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().toString());

        dto.setStatus(entity.getStatus());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        // TODO: Calculate here or give as parameter or ...?
        // TODO: Or call .setHoursDue() in TimesheetLogicBean? <- using this right now
//        dto.setHoursDue(999);
        dto.setSignedByEmployee(entity.getSignedByEmployee());
        dto.setSignedBySupervisor(entity.getSignedBySupervisor());

        return dto;
    }
}
