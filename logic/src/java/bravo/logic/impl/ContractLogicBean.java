package bravo.logic.impl;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.dao.RoleDAO;
import bravo.dto.ContractDTO;
import bravo.entities.Assistant;
import bravo.entities.Contract;
import bravo.entities.Employee;
import bravo.entities.Person;
import bravo.entities.Role;
import bravo.entities.Secretary;
import bravo.entities.Supervisor;
import bravo.entities.Timesheet;
import bravo.enums.ContractStatus;
import bravo.enums.FederalState;
import static bravo.enums.FederalState.RHINELAND_PALATINATE;
import bravo.enums.TimesheetFrequency;
import bravo.enums.TimesheetStatus;
import bravo.logic.ContractLogic;
import bravo.logic.TimesheetLogic;
import bravo.utitity.PublicHolidayService;
import com.sun.istack.logging.Logger;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class ContractLogicBean implements ContractLogic {

    private static final Logger LOGGER = Logger.getLogger(ContractLogicBean.class);

    @EJB
    private RoleDAO roleDAO;

    @EJB
    private PersonDAO personDAO;

    @EJB
    private ContractDAO contractDAO;

    @EJB
    private TimesheetLogic tmshtlBean;

    /**
     * Creates a new contract.
     *
     * @param contractDto the contract DTO with the data
     * @return the created contract with generated UUID
     */
    @Override
    @Transactional
    public ContractDTO createContract(ContractDTO contractDto) {
        if (contractDto == null) {
            throw new IllegalArgumentException("ContractDTO cannot be null.");
        }
        Contract contract = new Contract();
        mapDtoToEntity(contractDto, contract);

        Person employeePerson = personDAO.findByUUID(contractDto.getEmployeeUuid());
        Employee newEmployee = new Employee();
        employeePerson.addRole(newEmployee);
        contract.setEmployee(newEmployee);
        personDAO.save(employeePerson);

        contract.setEmployee(newEmployee);

        Person chefPerson = personDAO.findByUUID(contractDto.getSupervisorUuid());
        Supervisor newSupervisor = new Supervisor();
        chefPerson.addRole(newSupervisor);
        contract.setSupervisor(newSupervisor);
        personDAO.save(chefPerson); // i am not sure if i need explicitly save it, the method is transactional

        List<Secretary> secretaries = contractDto.getSecretaryUuids().stream()
                .map(uuid -> {
                    Person p = personDAO.findByUUID(uuid);
                    if (p == null) {
                        LOGGER.warning("Secretary with UUID " + uuid + " not found. Skipping.");
                        return null;
                    }
                    Secretary newSecretary = new Secretary();
                    p.addRole(newSecretary);
                    personDAO.save(p);
                    return newSecretary;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        contract.setSecretaries(secretaries);

        List<Assistant> assistants = contractDto.getAssistantUuids().stream()
                .map(uuid -> {
                    Person p = personDAO.findByUUID(uuid);
                    if (p == null) {
                        LOGGER.warning("Assistant with UUID " + uuid + " not found. Skipping.");
                        return null;
                    }
                    Assistant newAssistant = new Assistant();
                    p.addRole(newAssistant);
                    personDAO.save(p);
                    return newAssistant;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!(assistants.isEmpty())) {
            contract.setAssistants(assistants);
        }

        contract.setStatus(ContractStatus.PREPARED);
        Contract savedContract = contractDAO.save(contract);
        return mapEntityToDto(savedContract);
    }

    @Transactional
    public ContractDTO startContract(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUUID(uuid);
            if (contract.getStatus() != ContractStatus.PREPARED) {
                throw new IllegalStateException("Can only start a contract in PREPARED status.");
            }
            contract.setStatus(ContractStatus.STARTED);

            createTimesheetsForContract(contract);

            Contract updatedContract = contractDAO.save(contract);
            return mapEntityToDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }

    /**
     * Terminates a contract by setting its status to TERMINATED and recording
     * the termination date. This action is triggered by a Supervisor or
     * Assistant. All IN_PROGRESS timesheets are deleted.
     *
     * @param uuid the UUID of the contract to terminate
     * @param terminationDate the date when the contract is terminated
     * @return the updated contract
     */
    @Override
    public ContractDTO terminateContract(String uuid, LocalDate terminationDate) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUUID(uuid);
            if (contract.getStatus() != ContractStatus.STARTED) {
                throw new IllegalStateException("Contract is allowed for termination only in STARTED state.");
            }
            contract.setStatus(ContractStatus.TERMINATED);
            contract.setTerminationDate(terminationDate);

            contract.getTimesheets().removeIf(ts -> ts.getStatus() == TimesheetStatus.IN_PROGRESS);

            Contract updatedContract = contractDAO.save(contract);
            return mapEntityToDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }

    /**
     * Archives a contract. Transitions from STARTED or TERMINATED to ARCHIVED.
     * All timesheets for the contract are archived.
     *
     * @param uuid the UUID of the contract to archive
     * @return the updated contract
     */
    public ContractDTO archiveContract(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUUID(uuid);
            if (contract.getStatus() != ContractStatus.STARTED && contract.getStatus() != ContractStatus.TERMINATED) {
                throw new IllegalStateException("Can only archive a STARTED or TERMINATED contract.");
            }

            contract.getTimesheets().forEach(ts -> {
                if (ts.getStatus() == TimesheetStatus.SIGNED_BY_SUPERVISOR) {
                    ts.setStatus(TimesheetStatus.ARCHIVED);
                }
            });

            contract.setStatus(ContractStatus.ARCHIVED);

            Contract updatedContract = contractDAO.save(contract);
            return mapEntityToDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }

    /**
     * Deletes a contract from the database. This is the final step in the
     * contract lifecycle.
     *
     * @param uuid the UUID of the contract to delete
     */
    @Override
    public void deleteContract(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUUID(uuid);
            if (contract.getStatus() != ContractStatus.PREPARED || contract.getStatus() != ContractStatus.ARCHIVED) {
                throw new IllegalStateException("Can only delete an ARCHIVED or PREPARED contract.");
                // archived state is not a requirement, it just uses the same method 
                // when checking for archived timesheets being old for more than two years and if no timesheets is related to contract by then, it uses this method
            }
            if (!contract.getTimesheets().isEmpty()) {
                throw new IllegalStateException("Contract still has some related to it archived timesheets.");
            }
            contractDAO.delete(contract);

        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }

    /**
     * Updates an existing contract.
     *
     * @param contractDto the contract DTO with updated data
     * @return the updated contract
     */
    @Override
    @Transactional
    public ContractDTO updateContract(ContractDTO contractDto) {
        if (contractDto == null || contractDto.getUuid() == null) {
            throw new IllegalArgumentException("ContractDTO and its UUID cannot be null.");
        }
        try {
            Contract existingContract = contractDAO.findByUUID(contractDto.getUuid());
            if (!(existingContract.getStatus() == ContractStatus.PREPARED)) {
                throw new IllegalStateException("Update and deletion of contracts is only possible in state PREPARED. ");
            }
            mapDtoToEntity(contractDto, existingContract);

            String newSupervisorUuid = contractDto.getSupervisorUuid();
            Person oldSupervisorPerson = existingContract.getSupervisor().getPerson();
            Person newSupervisorPerson = (newSupervisorUuid != null) ? personDAO.findByUUID(newSupervisorUuid) : null;

            // if new superviser is differrent from current
            if (newSupervisorPerson != null && !newSupervisorPerson.equals(oldSupervisorPerson)) {

                Supervisor oldSupervisorRole = roleDAO.findSupervisorByContract(existingContract);
                if (oldSupervisorRole != null) {
                    // deletes old role from db and person
                    roleDAO.deleteWithPersonUpdate(oldSupervisorRole);
                }

                // creates new supervisor role for new superviser person and links it to contract
                Supervisor newSupervisorRole = new Supervisor();
                newSupervisorPerson.addRole(newSupervisorRole);
                newSupervisorRole.setContract(existingContract);
                roleDAO.save(newSupervisorRole);
                existingContract.setSupervisor(newSupervisorRole);
            }

            // updates secretary 
            List<String> newSecretaryUuids = contractDto.getSecretaryUuids();
            List<Secretary> oldSecretaryRoles = roleDAO.findSecretariesByContract(existingContract);

            // if new secretary's uuid is already present in the list of secretaries, we dont touch then and delete only another ones
            oldSecretaryRoles.stream()
                    .filter(r -> newSecretaryUuids == null || !newSecretaryUuids.contains(r.getPerson().getUuid()))
                    .forEach(r -> roleDAO.deleteWithPersonUpdate(r));

            // Додавання нових ролей
            if (newSecretaryUuids != null) {
                List<String> currentSecretaryUuids = oldSecretaryRoles.stream()
                        .map(r -> r.getPerson().getUuid())
                        .collect(Collectors.toList());

                newSecretaryUuids.stream()
                        .filter(uuid -> !currentSecretaryUuids.contains(uuid))
                        .forEach(uuid -> {
                            Person secretaryPerson = personDAO.findByUUID(uuid);
                            Secretary newSecretaryRole = new Secretary();
                            secretaryPerson.addRole(newSecretaryRole);
                            existingContract.addSecretary(newSecretaryRole);
                            roleDAO.save(newSecretaryRole);
                        });
            }

            // analougosly as with secretaries
            List<String> newAssistantUuids = contractDto.getAssistantUuids();
            List<Assistant> oldAssistantRoles = roleDAO.findAssistantsByContract(existingContract);

            // deletion of old roles that aren't present in new list
            oldAssistantRoles.stream()
                    .filter(r -> newAssistantUuids == null || !newAssistantUuids.contains(r.getPerson().getUuid()))
                    .forEach(r -> roleDAO.deleteWithPersonUpdate(r));

            // adding new roles
            if (newAssistantUuids != null) {
                List<String> currentAssistantUuids = oldAssistantRoles.stream()
                        .map(r -> r.getPerson().getUuid())
                        .collect(Collectors.toList());

                newAssistantUuids.stream()
                        .filter(uuid -> !currentAssistantUuids.contains(uuid))
                        .forEach(uuid -> {
                            Person assistantPerson = personDAO.findByUUID(uuid);
                            Assistant newAssistantRole = new Assistant();
                            assistantPerson.addRole(newAssistantRole);
                            existingContract.addAssistant(newAssistantRole);
                            roleDAO.save(newAssistantRole);
                        });
            }

            Contract updatedContract = contractDAO.save(existingContract);
            return mapEntityToDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + contractDto.getUuid() + " not found.");
        }
    }

    /**
     * Retrieves a contract by its UUID.
     *
     * @param uuid the UUID of the contract
     * @return the found contract or null if not found
     */
    @Override
    public ContractDTO getContractByUuid(String uuid) {
        try {
            Contract contract = contractDAO.findByUUID(uuid);
            return mapEntityToDto(contract);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Retrieves all contracts.
     *
     * @return a list of all contracts
     */
    @Override
    public List<ContractDTO> getAllContracts() {
        return contractDAO.getAllContracts().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all active contracts.
     *
     * @return a list of all active contracts
     */
    @Override
    public List<ContractDTO> getActiveContracts() {
        return contractDAO.getActiveContracts().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a Contract entity to a ContractDTO.
     *
     * @param entity The Contract entity to map.
     * @return A new ContractDTO populated with the entity's data.
     */
    private ContractDTO mapEntityToDto(Contract entity) {
        ContractDTO dto = new ContractDTO(entity.getUuid(), entity.getVersion());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setFrequency(entity.getFrequency());
        dto.setHoursPerWeek(entity.getHoursPerWeek());
        dto.setWorkingDaysPerWeek(entity.getWorkingDaysPerWeek());
        dto.setVacationDaysPerYear(entity.getVacationDaysPerYear());

        if (entity.getSupervisor() != null) {
            dto.setSupervisorUuid(entity.getSupervisor().getUuid());
        }

        if (entity.getEmployee() != null) {
            dto.setEmployeeUuid(entity.getEmployee().getUuid());
        }

        if (entity.getSecretaries() != null) {
            dto.setSecretaryUuids(entity.getSecretaries().stream()
                    .map(secretary -> secretary.getUuid())
                    .collect(Collectors.toList()));
        }
        if (entity.getAssistants() != null) {
            dto.setAssistantUuids(entity.getAssistants().stream()
                    .map(assistant -> assistant.getUuid())
                    .collect(Collectors.toList()));
        }

        if (entity.getTimesheets() != null) {
            dto.setTimesheetUuids(entity.getTimesheets().stream()
                    .map(timesheet -> timesheet.getUuid())
                    .collect(Collectors.toList()));
        }

        return dto;

    }

    /**
     * Helper method to map a ContractDTO to an existing Contract entity.
     *
     * @param dto The ContractDTO to map from.
     * @param entity The existing Contract entity to update.
     */
    private void mapDtoToEntity(ContractDTO dto, Contract entity) {
        // i could have moved the logic of assigning  dto to entity relations( employee etc) from startContract and createContract methods
        // but this is tedious to sit and take into account cases where it's merely the creation or modification of a contract.
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setFrequency(dto.getFrequency());
        entity.setHoursPerWeek(dto.getHoursPerWeek());
        entity.setWorkingDaysPerWeek(dto.getWorkingDaysPerWeek());
        entity.setVacationDaysPerYear(dto.getVacationDaysPerYear());

    }

    public void createTimesheetsForContract(Contract contract) {
        if (contract.getFrequency() == TimesheetFrequency.MONTHLY) {
            createMonthlyTimesheets(contract);
        } else if (contract.getFrequency() == TimesheetFrequency.WEEKLY) {
            createWeeklyTimesheets(contract);
        }
    }

    // these entirely relies on CascadeType.ALL in OneToMany relation to timesheets
    // jpa with this option propagates the effect of an operation to associated entities., timesheet in this case.
    // thus means when i add timesheet to ListArray<Timesheet> timesheets, it is automatically persisted.
    // previously it was handled by TimesheetDAO in TimesheetLogicBean
    private void createMonthlyTimesheets(Contract contract) {
        LocalDate currentPeriodStart = contract.getStartDate();
        while (!currentPeriodStart.isAfter(contract.getEndDate())) {
            LocalDate currentPeriodEnd = currentPeriodStart.withDayOfMonth(currentPeriodStart.lengthOfMonth());
            if (currentPeriodEnd.isAfter(contract.getEndDate())) {
                currentPeriodEnd = contract.getEndDate();
            }

            Timesheet timesheet = new Timesheet();
            timesheet.setContract(contract);
            timesheet.setStartDate(currentPeriodStart);
            timesheet.setEndDate(currentPeriodEnd);
            timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
            contract.addTimesheet(timesheet); // ensuring bidirectional associations

            currentPeriodStart = currentPeriodStart.plusMonths(1).withDayOfMonth(1);
        }
    }

    private void createWeeklyTimesheets(Contract contract) {
        LocalDate currentPeriodStart = contract.getStartDate();
        while (!currentPeriodStart.isAfter(contract.getEndDate())) {
            LocalDate currentPeriodEnd = currentPeriodStart.with(DayOfWeek.SUNDAY);
            if (currentPeriodEnd.isAfter(contract.getEndDate())) { //
                currentPeriodEnd = contract.getEndDate();
            }

            Timesheet timesheet = new Timesheet();
            timesheet.setContract(contract);
            timesheet.setStartDate(currentPeriodStart);
            timesheet.setEndDate(currentPeriodEnd);
            timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
            contract.addTimesheet(timesheet); // ensuring bidirectional associations

            currentPeriodStart = currentPeriodEnd.plusDays(1);
        }
    }

    @Override
    public Double getHoursDue(Contract contract) {
        return contract.getTimesheets().stream().
                map(ts -> tmshtlBean.hoursDue(ts.getUuid())).mapToDouble(Double::doubleValue).sum();
    }
}
