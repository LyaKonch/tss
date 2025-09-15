package bravo.mapper;

import bravo.dto.ContractDTO;
import bravo.entities.*;
import bravo.logic.PersonLogic;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class ContractMapper extends AbstractMapper<Contract, ContractDTO> {

    @Inject
    private PersonLogic personLogic;

    @Inject
    private PersonMapper personMapper;

    private static final Logger logger = Logger.getLogger(ContractMapper.class.getName());

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

        Person employeeEntity = personMapper.toEntity(personLogic.findByUuid(dto.getEmployeeUuid()));
        Employee employee = new Employee();
        employeeEntity.addRole(employee);
        entity.setEmployee(employee);

        Person supervisorEntity = personMapper.toEntity(personLogic.findByUuid(dto.getSupervisorUuid()));
        Supervisor supervisor = new Supervisor();
        supervisorEntity.addRole(supervisor);
        entity.setSupervisor(supervisor);

        List<Secretary> secretaries = new ArrayList<>();
        for (String secretaryUuid : dto.getSecretaryUuids()) {
            Person secretaryEntity = personMapper.toEntity(personLogic.findByUuid(secretaryUuid));
            Secretary secretary = new Secretary();
            secretaryEntity.addRole(secretary);
            secretaries.add(secretary);
        }
        entity.setSecretaries(secretaries);

        List<Assistant> assistants = new ArrayList<>();
        for (String assistantUuid : dto.getAssistantUuids()) {
            Person assistantEntity = personMapper.toEntity(personLogic.findByUuid(assistantUuid));
            Assistant assistant = new Assistant();
            assistantEntity.addRole(assistant);
            assistants.add(assistant);
        }
        entity.setAssistants(assistants);

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

        if (entity.getEmployee() != null) dto.setEmployeeUuid(entity.getEmployee().getPerson().getUuid().toString());
        else logger.warning("Contract without Employee: " + entity.getName());


        if (entity.getSupervisor() != null)
            dto.setSupervisorUuid(entity.getSupervisor().getPerson().getUuid().toString());
        else logger.warning("Contract without Supervisor: " + entity.getName());


        if (entity.getSecretaries() != null) dto.setSecretaryUuids(entity.getSecretaries().stream()
                .map(secretary -> secretary.getPerson().getUuid().toString())
                .toList());
        else logger.warning("Contract without Secretary: " + entity.getName());


        if (entity.getAssistants() != null) dto.setAssistantUuids(entity.getAssistants().stream()
                .map(assistant -> assistant.getPerson().getUuid().toString())
                .toList());
        else logger.warning("Contract without Assistant (can be null): " + entity.getName());

        return dto;
    }
}
