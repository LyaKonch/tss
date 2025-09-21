/**
 * It's called mapper but also creates new Instances if necessary.
 */
package bravo.mapper;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.dao.RoleDAO;
import bravo.dto.ContractDTO;
import bravo.entities.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContractMapper extends AbstractMapper<Contract, ContractDTO> {

    @Inject
    ContractDAO contractDAO;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private RoleDAO roleDAO;

    /**
     * Converts a Contract DTO to a Contract entity.
     */
    @Override
    public Contract toEntity(ContractDTO dto) {
        // Hello, my name is JPA, and I can't do basic operations without running into my own errors
        Person newEmployee = personDAO.findByUuid(dto.getEmployeeUuid());
        Person newSupervisor = personDAO.findByUuid(dto.getSupervisorUuid());
        Set<Person> newSecretaries = dto.getSecretaryUuids().stream().map(personDAO::findByUuid).collect(Collectors.toSet());
        Set<Person> newAssistants = dto.getAssistantUuids().stream().map(personDAO::findByUuid).collect(Collectors.toSet());

        // If id is null, we are creating a new entity, otherwise we are updating the existing one
        Contract entity = dto.getId() == null ? new Contract() : contractDAO.findByUuid(dto.getUuid());

        if (dto.getId() != null) entity.setId(dto.getId());
        if (dto.getUuid() != null) entity.setUuid(UUID.fromString(dto.getUuid()));

        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) entity.setEndDate(dto.getEndDate());
        if (dto.getFrequency() != null) entity.setFrequency(dto.getFrequency());
        entity.setTerminationDate(dto.getTerminationDate());
        if (dto.getHoursPerWeek() != null) entity.setHoursPerWeek(dto.getHoursPerWeek());
        if (dto.getWorkingDaysPerWeek() != null) entity.setWorkingDaysPerWeek(dto.getWorkingDaysPerWeek());
        if (dto.getVacationDaysPerYear() != null) entity.setVacationDaysPerYear(dto.getVacationDaysPerYear());

        // Get the current Employee of the entity or null if it doesn't exist
        Employee employeeRole = entity.getEmployee() != null ? entity.getEmployee() : new Employee();
        Person currentEmployee = entity.getEmployee() != null ? entity.getEmployee().getPerson() : null;
        // Remove the role from the current employee person if they exist
        if (currentEmployee != null) currentEmployee.removeRole(employeeRole);
        // The setters take care of bidirectional updates,
        // i.e., linking Employee to Contract and Contract to Employee
        // and linking Person to Employee and Employee to Person
        entity.setEmployee(employeeRole);
        newEmployee.addRole(employeeRole);
        roleDAO.save(employeeRole);

        Supervisor supervisorRole = entity.getSupervisor() != null ? entity.getSupervisor() : new Supervisor();
        Person currentSupervisor = entity.getSupervisor() != null ? entity.getSupervisor().getPerson() : null;
        if (currentSupervisor != null) currentSupervisor.removeRole(supervisorRole);
        entity.setSupervisor(supervisorRole);
        newSupervisor.addRole(supervisorRole);
        roleDAO.save(supervisorRole);

        // Collect all secretaries that are not in the newSecretaries set and remove them
        List<Secretary> firedSecretaries = entity.getSecretaries().stream()
                .filter(secretary -> !newSecretaries.contains(secretary.getPerson()))
                .toList();
        for (Secretary secretary : firedSecretaries) {
            secretary.getPerson().removeRole(secretary);
            entity.removeSecretary(secretary);
        }
        // Add all newSecretaries to the entity (setters don't add duplicates) and update the role's side, too
        for (Person newSecretary : newSecretaries) {
            boolean alreadyAssigned = entity.getSecretaries().stream()
                    .anyMatch(existing -> existing.getPerson().equals(newSecretary));
            if (alreadyAssigned) continue;
            Secretary secretaryRole = new Secretary();
            entity.addSecretary(secretaryRole);
            newSecretary.addRole(secretaryRole);
            roleDAO.save(secretaryRole);
        }

        List<Assistant> firedAssistants = entity.getAssistants().stream()
                .filter(assistant -> !newAssistants.contains(assistant.getPerson()))
                .toList();
        for (Assistant assistant : firedAssistants) {
            assistant.getPerson().removeRole(assistant);
            entity.removeAssistant(assistant);
        }
        for (Person newAssistant : newAssistants) {
            boolean alreadyAssigned = entity.getAssistants().stream()
                    .anyMatch(existing -> existing.getPerson().equals(newAssistant));
            if (alreadyAssigned) continue;
            Assistant assistantRole = new Assistant();
            entity.addAssistant(assistantRole);
            newAssistant.addRole(assistantRole);
            roleDAO.save(assistantRole);
        }

        contractDAO.save(entity);
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

        // No Role relation can be null
        dto.setEmployeeUuid(entity.getEmployee().getPerson().getUuid().toString());
        dto.setSupervisorUuid(entity.getSupervisor().getPerson().getUuid().toString());
        dto.setSecretaryUuids(entity.getSecretaries().stream()
                .map(secretary -> secretary.getPerson().getUuid().toString())
                .toList());
        dto.setAssistantUuids(entity.getAssistants().stream()
                .map(assistant -> assistant.getPerson().getUuid().toString())
                .toList());

        dto.setTimesheetUuids(entity.getTimesheets().stream().map(Timesheet::getUuid).map(UUID::toString).toList());

        return dto;
    }
}
