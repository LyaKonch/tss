package bravo.logic.impl;

import bravo.dao.ContractDAO;
import bravo.dao.RoleDAO;
import bravo.dto.ContractDTO;
import bravo.entities.Contract;
import bravo.entities.Timesheet;
import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import bravo.enums.TimesheetStatus;
import bravo.logic.ContractLogic;
import bravo.logic.TimesheetLogic;
import bravo.mapper.ContractMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public ContractDTO create(ContractDTO contractDto) {
        if (contractDto == null) throw new IllegalArgumentException("ContractDTO cannot be null.");
        Contract entity = ContractMapper.toEntity(contractDto);
        return ContractMapper.toDto(entity);
    }

    @Override
    public ContractDTO update(ContractDTO contractDto) {
        if (contractDto == null || contractDto.getUuid() == null) {
            throw new IllegalArgumentException("Contract Update: ContractDTO or its UUID is null");
        }
        try {
            Contract existingContract = contractDAO.findByUuid(contractDto.getUuid());
            if (!(existingContract.getStatus() == ContractStatus.PREPARED)) {
                throw new IllegalStateException("Contract Update: Updates only allowed in status PREPARED.");
            }
            existingContract = ContractMapper.toEntity(contractDto);
            return ContractMapper.toDto(existingContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract Update: Contract with UUID " + contractDto.getUuid() + " not found.");
        }
    }

    @Override
    public ContractDTO startContract(ContractDTO contractDto) {
        if (contractDto.getUuid() == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUuid(contractDto.getUuid());
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
    public ContractDTO findByUuid(String uuid) {
        Contract entity = contractDAO.findByUuid(uuid);
        return entity != null ? ContractMapper.toDto(entity) : null;
    }

    @Override
    public List<ContractDTO> findActiveEmployeeContractsByEmail(String email) {
        List<Contract> entities = contractDAO.findActiveEmployeeContractByEmail(email);
        List<ContractDTO> dtos = new ArrayList<>(entities.size());
        for (Contract entity : entities) dtos.add(ContractMapper.toDto(entity));
        return dtos;
    }

    @Override
    public List<ContractDTO> findAll() {
        List<Contract> entities = contractDAO.findAll();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());
        for (Contract entity : entities) dtos.add(ContractMapper.toDto(entity));
        return dtos;
    }

    @Override
    public List<ContractDTO> getActive() {
        List<Contract> entities = contractDAO.getActiveContracts();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());
        for (Contract entity : entities) dtos.add(ContractMapper.toDto(entity));
        return dtos;
    }

    @Override
    public ContractDTO terminate(String uuid, LocalDate terminationDate) {
        Contract entity = contractDAO.findByUuid(uuid);

        if (entity != null) {
            if (entity.getStatus() != ContractStatus.STARTED) {
                throw new IllegalStateException("Contract is allowed for termination only in STARTED state.");
            }

            if (terminationDate.isBefore(LocalDate.now())) entity.setStatus(ContractStatus.TERMINATED);
            entity.setTerminationDate(terminationDate);
            entity.getTimesheets().removeIf(ts -> ts.getStatus() == TimesheetStatus.IN_PROGRESS);
            contractDAO.save(entity);
            return ContractMapper.toDto(entity);
        } else {
            throw new IllegalArgumentException("Cannot find a contract by UUID provided");
        }


    }

    @Override
    public double calculateHoursDue(ContractDTO contract) {
        // Logic for calculating hours due
        return contract.getTimesheetUuids().stream().map(uuid -> timesheetBean.hoursDue(uuid)).mapToDouble(Double::doubleValue).sum();
    }

    @Override
    public double calculateVacationHours(ContractDTO contract) {
        // Logic for calculating vacation hours
        return contract.getVacationDaysPerYear() * (contract.getHoursPerWeek() / 5.0);
    }

    public ContractDTO archiveContract(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUuid(uuid);
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
            if (contract.getStatus() != ContractStatus.PREPARED || contract.getStatus() != ContractStatus.ARCHIVED) {
                throw new IllegalStateException("Can only delete an ARCHIVED or PREPARED contract.");
                // archived state is not a requirement, it just uses the same method
                // when checking for archived timesheets being old for more than two years and if no timesheets is related to contract by then, it uses this method
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


}
