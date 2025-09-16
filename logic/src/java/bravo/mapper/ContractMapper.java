package bravo.mapper;

import bravo.dto.ContractDTO;
import bravo.entities.*;
import bravo.logic.PersonLogic;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public void updateExistingContractFromDto(ContractDTO contractDto, Contract existingContract) {
        if(!contractDto.getUuid().equals(existingContract.getUuid().toString())){
            logger.warning("Aborting function updateExistingContractFromDto.\n " +
                    "You are trying to update wrong contract(uuid)" + existingContract.getUuid().toString() +
                    " with dto(uuid)" + contractDto.getUuid());
            return;
        }

        // Map basic fields
        existingContract.setStatus(contractDto.getStatus());
        existingContract.setName(contractDto.getName());
        existingContract.setStartDate(contractDto.getStartDate());
        existingContract.setEndDate(contractDto.getEndDate());
        existingContract.setFrequency(contractDto.getFrequency());
        existingContract.setTerminationDate(contractDto.getTerminationDate());
        existingContract.setHoursPerWeek(contractDto.getHoursPerWeek());
        existingContract.setWorkingDaysPerWeek(contractDto.getWorkingDaysPerWeek());
        existingContract.setVacationDaysPerYear(contractDto.getVacationDaysPerYear());

        // Employee logic
        String newEmployeeUuid = contractDto.getEmployeeUuid();
        Person oldEmployeePerson = existingContract.getEmployee().getPerson();
        Person newEmployeePerson = (newEmployeeUuid != null) ? personMapper.toEntity(personLogic.findByUuid(newEmployeeUuid)) : null;
        if (newEmployeePerson != null && !newEmployeePerson.equals(oldEmployeePerson)) {
            Employee oldEmployeeRole = existingContract.getEmployee();
            if (oldEmployeeRole != null) {
                oldEmployeePerson.getRoles().remove(oldEmployeeRole);
            }
            Employee newEmployeeRole = new Employee();
            newEmployeePerson.addRole(newEmployeeRole);
            existingContract.setEmployee(newEmployeeRole);
        }

        // Supervisor logic
        String newSupervisorUuid = contractDto.getSupervisorUuid();
        Person oldSupervisorPerson = existingContract.getSupervisor().getPerson();
        Person newSupervisorPerson = (newSupervisorUuid != null) ? personMapper.toEntity(personLogic.findByUuid(newSupervisorUuid)) : null;

        if (newSupervisorPerson != null && !newSupervisorPerson.equals(oldSupervisorPerson)) {
            Supervisor oldSupervisorRole = existingContract.getSupervisor();
            if (oldSupervisorRole != null) {
                oldSupervisorPerson.getRoles().remove(oldSupervisorRole);
            }
            Supervisor newSupervisorRole = new Supervisor();
            newSupervisorPerson.addRole(newSupervisorRole);
            existingContract.setSupervisor(newSupervisorRole);
        }

        // Secretaries logic
        List<String> newSecretaryUuids = contractDto.getSecretaryUuids();
        List<Secretary> oldSecretaryRoles = new ArrayList<>(existingContract.getSecretaries());
        Set<String> newSecretaryUuidsSet = (newSecretaryUuids != null) ? new HashSet<>(newSecretaryUuids) : new HashSet<>();

        // Remove old secretaries that are not in the new list
        for (Secretary oldRole : oldSecretaryRoles) {
            if (!newSecretaryUuidsSet.contains(oldRole.getPerson().getUuid().toString())) {
                oldRole.getPerson().getRoles().remove(oldRole);
                existingContract.removeSecretary(oldRole);
            }
        }

        // Add new secretaries that are not in the old list
        Set<String> currentSecretaryUuids = oldSecretaryRoles.stream()
                .map(r -> r.getPerson().getUuid().toString())
                .collect(Collectors.toSet());

        if (newSecretaryUuids != null) {
            for (String uuid : newSecretaryUuids) {
                if (!currentSecretaryUuids.contains(uuid)) {
                    Person secretaryPerson = personMapper.toEntity(personLogic.findByUuid(uuid));
                    Secretary newSecretaryRole = new Secretary();
                    secretaryPerson.addRole(newSecretaryRole);
                    existingContract.addSecretary(newSecretaryRole);
                }
            }
        }

        // Assistants logic
        List<String> newAssistantUuids = contractDto.getAssistantUuids();
        List<Assistant> oldAssistantRoles = new ArrayList<>(existingContract.getAssistants());
        Set<String> newAssistantUuidsSet = (newAssistantUuids != null) ? new HashSet<>(newAssistantUuids) : new HashSet<>();

        // Remove old assistants that are not in the new list
        for (Assistant oldRole : oldAssistantRoles) {
            if (!newAssistantUuidsSet.contains(oldRole.getPerson().getUuid().toString())) {
                oldRole.getPerson().getRoles().remove(oldRole);
                existingContract.removeAssistant(oldRole);
            }
        }

        // Add new assistants that are not in the old list
        Set<String> currentAssistantUuids = oldAssistantRoles.stream()
                .map(r -> r.getPerson().getUuid().toString())
                .collect(Collectors.toSet());

        if (newAssistantUuids != null) {
            for (String uuid : newAssistantUuids) {
                if (!currentAssistantUuids.contains(uuid)) {
                    Person assistantPerson = personMapper.toEntity(personLogic.findByUuid(uuid));
                    Assistant newAssistantRole = new Assistant();
                    assistantPerson.addRole(newAssistantRole);
                    existingContract.addAssistant(newAssistantRole);
                }
            }
        }
    }

}
