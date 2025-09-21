package bravo.logic.impl;

import bravo.dao.ContractDAO;
import bravo.dao.RoleDAO;
import bravo.dao.TimesheetDAO;
import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.dto.TimesheetDTO;
import bravo.entities.*;
import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import bravo.enums.TimesheetStatus;
import bravo.logic.ContractLogic;
import bravo.logic.TimesheetLogic;
import bravo.mapper.ContractMapper;
import bravo.mapper.PersonMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import bravo.utitity.AuthorizationService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Stateless
public class ContractLogicBean implements ContractLogic {

    private static final Logger logger = Logger.getLogger(ContractLogicBean.class.getName());

    @EJB
    private ContractDAO contractDAO;

    @EJB
    private TimesheetLogic timesheetBean;

    @Inject
    private ContractMapper ContractMapper;

    @Inject
    RoleDAO roleDAO;

    @Inject
    private PersonMapper personMapper;

    @Inject
    private AuthorizationService authService;

    @Override
    public ContractDTO create(ContractDTO contractDto) {
        if (!authService.isUserInRole("ADMIN") && !authService.isUserInRole("STAFF")) {
            throw new SecurityException("Only ADMIN or STAFF can create contracts.");
        }
        if (contractDto == null) throw new IllegalArgumentException("ContractDTO cannot be null.");
        Contract entity = ContractMapper.toEntity(contractDto);
        contractDAO.save(entity);
        return ContractMapper.toDto(entity);
    }

    @Override
    public ContractDTO startContract(ContractDTO contractDto) {
        if (contractDto.getUuid() == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUuid(contractDto.getUuid());
            if (!authService.canAdministrateContract(contract)) {
                throw new SecurityException("User not authorized to start this contract.");
            }
            if (contract.getStatus() != ContractStatus.PREPARED) {
                throw new IllegalStateException("Can only start a contract in PREPARED status.");
            }
            contract.setStatus(ContractStatus.STARTED);

            createTimesheetsForContract(contract);

            Contract updatedContract = contractDAO.save(contract);
            return ContractMapper.toDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + contractDto.getUuid() + " not found.");
        }
    }

    @Override
    public ContractDTO update(ContractDTO contractDto) {
        if (contractDto == null || contractDto.getUuid() == null) {
            throw new IllegalArgumentException("ContractDTO and its UUID cannot be null.");
        }
        try {
            Contract existingContract = contractDAO.findByUuid(contractDto.getUuid());
            if (!authService.canAdministrateContract(existingContract)) {
                throw new SecurityException("User not authorized to update this contract.");
            }
            if (!(existingContract.getStatus() == ContractStatus.PREPARED)) {
                throw new IllegalStateException("Update and deletion of contracts is only possible in state PREPARED. ");
            }
            Contract contract = ContractMapper.toEntity(contractDto);
            contractDAO.save(contract);
            return ContractMapper.toDto(existingContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + contractDto.getUuid() + " not found.");
        }
    }

    @Override
    public ContractDTO findByUuid(String uuid) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        Contract entity = contractDAO.findByUuid(uuid);
        return entity != null ? ContractMapper.toDto(entity) : null;
    }

    @Override
    public List<ContractDTO> findActiveEmployeeContractsByEmail(String email) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        List<Contract> entities = contractDAO.findActiveEmployeeContractByEmail(email);
        List<ContractDTO> dtos = new ArrayList<>(entities.size());
        for (Contract entity : entities) dtos.add(ContractMapper.toDto(entity));
        return dtos;
    }

    @Override
    public List<ContractDTO> findAll() {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can get all users info.");
        }

        List<Contract> entities = contractDAO.findAll();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());
        for (Contract entity : entities) dtos.add(ContractMapper.toDto(entity));
        return dtos;
    }

    @Override
    public List<ContractDTO> getActive() {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can get all users info.");
        }
        List<Contract> entities = contractDAO.getActiveContracts();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());
        for (Contract entity : entities) dtos.add(ContractMapper.toDto(entity));
        return dtos;
    }

    @Override
    public ContractDTO terminate(String uuid, LocalDate terminationDate) {
        Contract entity = contractDAO.findByUuid(uuid);
        if (!authService.canTerminateContract(entity)) {
            throw new SecurityException("User not authorized to terminate this contract.");
        }
        if (entity != null) {
            if (entity.getStatus() != ContractStatus.STARTED) {
                throw new IllegalStateException("Contract is allowed for termination only in STARTED state.");
            }

            entity.setStatus(ContractStatus.TERMINATED);
            entity.setTerminationDate(terminationDate);
            logger.info("I am here");
            List<Timesheet> timesheetsToRemove = new ArrayList<>();
            for (Timesheet ts : entity.getTimesheets()) {
                if (ts.getStatus() == TimesheetStatus.IN_PROGRESS) {
                    timesheetsToRemove.add(ts);
                }
            }
            logger.info("Removing timesheets " + timesheetsToRemove.size() + " from timesheets list.");
            for (Timesheet ts : timesheetsToRemove) {
                entity.removeTimesheet(ts);
            }
            contractDAO.save(entity);
            return ContractMapper.toDto(entity);
        } else {
            throw new IllegalArgumentException("Cannot find a contract by UUID provided");
        }


    }

    @Override
    public List<ContractDTO> findContractsAssociatedWithPerson(PersonDTO person){
        Set<ContractDTO> contracts = new HashSet<>();
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        List<Role> roles = roleDAO.findByPerson(personMapper.toEntity(person));

        for (Role role : roles) {
            if (role instanceof Employee) {
                contracts.add(ContractMapper.toDto(((Employee) role).getContract()));
            } else if (role instanceof Supervisor) {
                contracts.add(ContractMapper.toDto(((Supervisor) role).getContract()));
            } else if (role instanceof Assistant) {
                contracts.add(ContractMapper.toDto(((Assistant) role).getContract()));
            } else if (role instanceof Secretary) {
                contracts.add(ContractMapper.toDto(((Secretary) role).getContract()));
            }
        }

        return contracts.stream().toList();
    }

    @Override
    public double calculateHoursDue(ContractDTO contract) {
        // Logic for calculating hours due
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        return contract.getTimesheetUuids().stream().map(uuid -> timesheetBean.hoursDue(uuid)).mapToDouble(Double::doubleValue).sum();
    }

    @Override
    public double calculateVacationHours(ContractDTO contract) {
        // Logic for calculating vacation hours
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        return contract.getVacationDaysPerYear() * (contract.getHoursPerWeek() / 5.0);
    }

    public ContractDTO archiveContract(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUuid(uuid);
            if (!authService.canArchiveContract(contract)) {
                throw new SecurityException("User not authorized to archive this contract.");
            }
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
            return ContractMapper.toDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }

    @Override
    public ContractDTO abortContract(String uuid) {
        Contract entity = contractDAO.findByUuid(uuid);
        if (entity == null) {
            throw new IllegalArgumentException("Cannot find a contract by UUID provided: " + uuid);
        }

        if (!authService.canAbortContract(entity)) {
            throw new SecurityException("User not authorized to abort this contract.");
        }

        boolean hasSignedByEmployeeTimesheets = entity.getTimesheets().stream()
                .anyMatch(timesheet -> timesheet.getStatus() == TimesheetStatus.SIGNED_BY_EMPLOYEE);

        if (hasSignedByEmployeeTimesheets) {
            throw new IllegalStateException("Contract cannot be aborted because one or more timesheets are in SIGNED_BY_EMPLOYEE status.");
        }
        List<Timesheet> timesheetsToRemove = new ArrayList<>();
        for (Timesheet ts : entity.getTimesheets()) {
            if (ts.getStatus() == TimesheetStatus.IN_PROGRESS) {
                timesheetsToRemove.add(ts);
            }
        }
        for (Timesheet ts : timesheetsToRemove) {
            entity.removeTimesheet(ts);
        }
        entity.setTerminationDate(LocalDate.now());
        entity.setStatus(ContractStatus.ARCHIVED);

        contractDAO.save(entity);

        return ContractMapper.toDto(entity);
    }

    @Override
    public List<TimesheetDTO> getAllTimesheetsForContract(String uuid){
        return timesheetBean.findByContractUuid(uuid);
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
            Contract contract = contractDAO.findByUuid(uuid);
            if (!authService.canAdministrateContract(contract)) {
                throw new SecurityException("User not authorized to delete this contract.");
            }
            if (contract.getStatus() != ContractStatus.PREPARED || contract.getStatus() != ContractStatus.ARCHIVED) {
                throw new IllegalStateException("Can only delete an ARCHIVED or PREPARED contract.");
            }
            if (!contract.getTimesheets().isEmpty()) {
                throw new IllegalStateException("Contract still has some related to it timesheets.");
            }

            roleDAO.deleteRolesByContractIdNative(contract.getId());

            contractDAO.deleteById(contract.getId());

        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }

    private void createTimesheetsForContract(Contract contract) {
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


}
