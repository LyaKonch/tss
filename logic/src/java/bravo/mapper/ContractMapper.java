package bravo.mapper;

import bravo.dto.ContractDTO;
import bravo.entities.Contract;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ContractMapper extends AbstractMapper<Contract, ContractDTO> {

    /**
     * Converts a Contract DTO to a Contract entity.
     */
    @Override
    public Contract toEntity(ContractDTO dto) {
        Contract entity = new Contract();

        if (dto.getId() != null) entity.setId(dto.getId());
        if (dto.getUuid() != null) entity.setUuid(UUID.fromString(dto.getUuid()));

        //noinspection Duplicates --- IntelliJ Duplication Inspection Suppression - No easy way around similar calls
        entity.setStatus(dto.getStatus());
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setFrequency(dto.getFrequency());
        entity.setTerminationDate(dto.getTerminationDate());
        entity.setHoursPerWeek(dto.getHoursPerWeek());
        entity.setWorkingDaysPerWeek(dto.getWorkingDaysPerWeek());
        entity.setVacationDaysPerYear(dto.getVacationDaysPerYear());

//        entity.setEmployee(dto.getEmployeeUuid());

        return entity;
    }

    /**
     * Converts a Contract entity to a Contract DTO.
     */
    @Override
    public ContractDTO toDto(Contract entity) {
        ContractDTO dto = new ContractDTO();

        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().toString());

        //noinspection Duplicates --- IntelliJ Duplication Inspection Suppression - No easy way around similar calls
        dto.setStatus(entity.getStatus());
        dto.setName(entity.getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setFrequency(entity.getFrequency());
        dto.setTerminationDate(entity.getTerminationDate());
        dto.setHoursPerWeek(entity.getHoursPerWeek());
        dto.setWorkingDaysPerWeek(entity.getWorkingDaysPerWeek());
        dto.setVacationDaysPerYear(entity.getVacationDaysPerYear());

        return dto;
    }
}
