package bravo.mapper;

import bravo.dao.ContractDAO;
import bravo.dao.RoleDAO;
import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.entities.*;
import bravo.logic.PersonLogic;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Null;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContractMapper extends AbstractMapper<Contract, ContractDTO> {

    @Inject
    private PersonLogic personLogic;

    @Inject
    private PersonMapper personMapper;

    @Inject
    RoleDAO roleDAO;

    @Inject
    ContractDAO contractDAO;

    private static final Logger logger = Logger.getLogger(ContractMapper.class.getName());

    /**
     * Converts a Contract DTO to a Contract entity.
     */
    @Override
    public Contract toEntity(ContractDTO dto) {

        if (dto.getUuid() != null) {
            return contractDAO.findByUuid(dto.getUuid());
        }

        Person employeePerson = personMapper.toEntity(personLogic.findByUuid(dto.getEmployeeUuid()));

        Person supervisorPerson = null;
        if (dto.getSupervisorUuid() != null) {
            supervisorPerson = personMapper.toEntity(personLogic.findByUuid(dto.getSupervisorUuid()));
        }

        List<Person> secretaryPersons = new ArrayList<>();
        if (dto.getSecretaryUuids() != null) {
            for (String uuid : dto.getSecretaryUuids()) {
                secretaryPersons.add(personMapper.toEntity(personLogic.findByUuid(uuid)));
            }
        }

        List<Person> assistantPersons = new ArrayList<>();
        if (dto.getAssistantUuids() != null) {
            for (String uuid : dto.getAssistantUuids()) {
                assistantPersons.add(personMapper.toEntity(personLogic.findByUuid(uuid)));
            }
        }

        Contract entity = new Contract();

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

        Employee employeeRole = new Employee();

        // Встановлюємо двонаправлені зв'язки
        entity.setEmployee(employeeRole);
        employeePerson.addRole(employeeRole);

        if (supervisorPerson != null) {
            Supervisor supervisorRole = new Supervisor();
            entity.setSupervisor(supervisorRole);
            supervisorPerson.addRole(supervisorRole);
        }

        for (Person secretaryPerson : secretaryPersons) {
            Secretary secretaryRole = new Secretary();
            entity.addSecretary(secretaryRole);
            secretaryPerson.addRole(secretaryRole);
        }

        for (Person assistantPerson : assistantPersons) {
            Assistant assistantRole = new Assistant();
            entity.addAssistant(assistantRole);
            assistantPerson.addRole(assistantRole);
        }

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
        if (!contractDto.getUuid().equals(existingContract.getUuid().toString())) {
            logger.warning("Aborting function updateExistingContractFromDto.\n " +
                    "You are trying to update wrong contract(uuid)" + existingContract.getUuid().toString() +
                    " with dto(uuid)" + contractDto.getUuid());
            return;
        }


        Person newEmployeePerson = null;
        if (contractDto.getEmployeeUuid() != null) {
            newEmployeePerson = personMapper.toEntity(personLogic.findByUuid(contractDto.getEmployeeUuid()));
        }

        Person newSupervisorPerson = null;
        if (contractDto.getSupervisorUuid() != null) {
            newSupervisorPerson = personMapper.toEntity(personLogic.findByUuid(contractDto.getSupervisorUuid()));
        }


        Map<String, Person> secretaryPersonMap = new HashMap<>();
        if (contractDto.getSecretaryUuids() != null) {
            for (String uuid : contractDto.getSecretaryUuids()) {
                secretaryPersonMap.put(uuid, personMapper.toEntity(personLogic.findByUuid(uuid)));
            }
        }

        Map<String, Person> assistantPersonMap = new HashMap<>();
        if (contractDto.getAssistantUuids() != null) {
            for (String uuid : contractDto.getAssistantUuids()) {
                assistantPersonMap.put(uuid, personMapper.toEntity(personLogic.findByUuid(uuid)));
            }
        }


        existingContract.setStatus(contractDto.getStatus());
        existingContract.setName(contractDto.getName());
        existingContract.setStartDate(contractDto.getStartDate());
        existingContract.setEndDate(contractDto.getEndDate());
        existingContract.setFrequency(contractDto.getFrequency());
        existingContract.setTerminationDate(contractDto.getTerminationDate());
        existingContract.setHoursPerWeek(contractDto.getHoursPerWeek());
        existingContract.setWorkingDaysPerWeek(contractDto.getWorkingDaysPerWeek());
        existingContract.setVacationDaysPerYear(contractDto.getVacationDaysPerYear());


        if (newEmployeePerson != null && !newEmployeePerson.getUuid().toString().equals(existingContract.getEmployee().getPerson().getUuid().toString())) {

            existingContract.getEmployee().getPerson().getRoles().remove(existingContract.getEmployee());

            Employee newEmployeeRole = new Employee();
            newEmployeePerson.addRole(newEmployeeRole);
            existingContract.setEmployee(newEmployeeRole);
        }


        if (newSupervisorPerson != null && (existingContract.getSupervisor() == null || !newSupervisorPerson.getUuid().toString().equals(existingContract.getSupervisor().getPerson().getUuid().toString()))) {
            if (existingContract.getSupervisor() != null) {
                existingContract.getSupervisor().getPerson().getRoles().remove(existingContract.getSupervisor());
            }
            Supervisor newSupervisorRole = new Supervisor();
            newSupervisorPerson.addRole(newSupervisorRole);
            existingContract.setSupervisor(newSupervisorRole);
        }

        updateSecretaries(existingContract, secretaryPersonMap);

        updateAssistants(existingContract, assistantPersonMap);

    }

    private void updateSecretaries(Contract existingContract, Map<String, Person> newSecretaryPersonMap) {
        Set<String> newUuids = newSecretaryPersonMap.keySet();
        List<Secretary> existingSecretaries = existingContract.getSecretaries();

        List<Secretary> secretariesToRemove = new ArrayList<>();
        for (Secretary role : existingSecretaries) {
            if (!newUuids.contains(role.getPerson().getUuid().toString())) {
                secretariesToRemove.add(role);
            }
        }

        for (Secretary role : secretariesToRemove) {
            role.getPerson().getRoles().remove(role);
            existingSecretaries.remove(role);
        }

        Set<String> currentUuids = existingSecretaries.stream()
                .map(role -> role.getPerson().getUuid().toString())
                .collect(Collectors.toSet());

        for (String newUuid : newUuids) {
            if (!currentUuids.contains(newUuid)) {
                Person person = newSecretaryPersonMap.get(newUuid);

                Secretary newSecretaryRole = new Secretary();
                person.addRole(newSecretaryRole);
                existingContract.addSecretary(newSecretaryRole);
            }
        }
    }

    private void updateAssistants(Contract existingContract, Map<String, Person> newAssistantPersonMap) {
        Set<String> newUuids = newAssistantPersonMap.keySet();
        List<Assistant> existingAssistants = existingContract.getAssistants();

        List<Assistant> assistantsToRemove = new ArrayList<>();
        for (Assistant role : existingAssistants) {
            if (!newUuids.contains(role.getPerson().getUuid().toString())) {
                assistantsToRemove.add(role);
            }
        }

        for (Assistant role : assistantsToRemove) {
            role.getPerson().getRoles().remove(role);
            existingAssistants.remove(role);
        }

        Set<String> currentUuids = existingAssistants.stream()
                .map(role -> role.getPerson().getUuid().toString())
                .collect(Collectors.toSet());

        for (String newUuid : newUuids) {
            if (!currentUuids.contains(newUuid)) {
                Person person = newAssistantPersonMap.get(newUuid);
                Assistant newAssistantRole = new Assistant();
                person.addRole(newAssistantRole);
                existingContract.addAssistant(newAssistantRole);
            }
        }
    }
}
