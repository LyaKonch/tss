package bravo.logic.impl;

import bravo.dao.ContractDAO;
import bravo.dto.ContractDTO;
import bravo.entities.Contract;
import bravo.enums.ContractStatus;
import bravo.logic.ContractLogic;
import bravo.logic.TimesheetLogic;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ContractLogicBean implements ContractLogic  {
    @EJB
    private ContractDAO contractDAO;

    @EJB
    private TimesheetLogic timesheetLogic;
    
    /**
     * Creates a new contract.
     *
     * @param contractDto the contract DTO with the data
     * @return the created contract with generated UUID
     */
    @Override
    public ContractDTO createContract(ContractDTO contractDto) {
        if (contractDto == null) {
            throw new IllegalArgumentException("ContractDTO cannot be null.");
        }
        Contract contract = new Contract();
        mapDtoToEntity(contractDto, contract);
        contract.setStatus(ContractStatus.PREPARED);
        Contract savedContract = contractDAO.save(contract);
        return mapEntityToDto(savedContract);
    }
    
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

            timesheetLogic.createTimesheetsForContract(contract);
            Contract updatedContract = contractDAO.save(contract);
            return mapEntityToDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }
    
    /**
     * Terminates a contract by setting its status to TERMINATED and recording the termination date.
     * This action is triggered by a Supervisor or Assistant. All IN_PROGRESS timesheets are deleted.
     *
     * @param uuid            the UUID of the contract to terminate
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
            if (contract.getStatus() == ContractStatus.TERMINATED || contract.getStatus() == ContractStatus.ARCHIVED) {
                throw new IllegalStateException("Contract is already terminated or archived.");
            }
            contract.setStatus(ContractStatus.TERMINATED);
            contract.setTerminationDate(terminationDate);
            // All Timesheets with status IN_PROGRESS get deleted
            timesheetLogic.deleteTimesheetsInProgressForContract(contract);
            Contract updatedContract = contractDAO.save(contract);
            return mapEntityToDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }
    
    /**
     * Archives a contract. Transitions from STARTED or TERMINATED to ARCHIVED.
     * All timesheets for the contract are archived.
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
            
            // All Timesheets archived if it has SIGNED_BY_SUPERVISOR status, if not the exception is thrown
            timesheetLogic.archiveTimesheetsForContract(contract);
            
            contract.setStatus(ContractStatus.ARCHIVED);
            
            Contract updatedContract = contractDAO.save(contract);
            return mapEntityToDto(updatedContract);
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Contract with UUID " + uuid + " not found.");
        }
    }

    /**
     * Deletes a contract from the database.
     * This is the final step in the contract lifecycle.
     * @param uuid the UUID of the contract to delete
     */
    public void deleteContract(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        try {
            Contract contract = contractDAO.findByUUID(uuid);
            if (contract.getStatus() != ContractStatus.PREPARED) {
                 throw new IllegalStateException("Can only delete an ARCHIVED or PREPARED contract.");
            }
            // does EntityManager handle cascade deleting? or i need to prematurely delete all timesheets
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
     * Calculates the hours due for a contract based on working days per week and hours per week.
     * This calculation is performed for the full duration of the contract.
     *
     * @param contractDto the contract DTO to calculate hours for
     * @return the calculated hours due
     */
    @Override
    public double calculateHoursDue(ContractDTO contractDto) {
        if (contractDto.getStartDate() == null || contractDto.getEndDate() == null) {
            return 0.0;
        }
        LocalDate start = contractDto.getStartDate();
        LocalDate end = contractDto.getEndDate();
        
        int workingDays = timesheetLogic.countWorkingDays(start, end);
        int holidays = timesheetLogic.countHolidaysOnWorkingDays(start, end); // NOTE: Using default state
        
        int effectiveDays = workingDays - holidays;
        
        if (contractDto.getWorkingDaysPerWeek() <= 0) {
            return 0.0;
        }
        
        double hoursPerDay = contractDto.getHoursPerWeek() / contractDto.getWorkingDaysPerWeek();
        return effectiveDays * hoursPerDay;
    }

    /**
     * Calculates the vacation hours for a contract based on vacation days per year and hours per week.
     *
     * @param contractDto the contract DTO to calculate vacation hours for
     * @return the calculated vacation hours
     */
    @Override
    public double calculateVacationHours(ContractDTO contractDto) {
        if (contractDto.getStartDate() == null || contractDto.getEndDate() == null) {
            return 0.0;
        }

        long months = ChronoUnit.MONTHS.between(contractDto.getStartDate(), contractDto.getEndDate());
        double monthsFactor = (double) months / 12.0;

        if (contractDto.getWorkingDaysPerWeek() <= 0) {
            return 0.0;
        }

        double hoursPerDay = contractDto.getHoursPerWeek() / contractDto.getWorkingDaysPerWeek();
        return contractDto.getVacationDaysPerYear() * monthsFactor * hoursPerDay;
    }

    /**
     * Helper method to map a Contract entity to a ContractDTO.
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
        dto.setTerminationDate(entity.getTerminationDate());
        dto.setHoursPerWeek(entity.getHoursPerWeek());
        dto.setWorkingDaysPerWeek(entity.getWorkingDaysPerWeek());
        dto.setVacationDaysPerYear(entity.getVacationDaysPerYear());
        return dto;
    }

    /**
     * Helper method to map a ContractDTO to an existing Contract entity.
     * @param dto The ContractDTO to map from.
     * @param entity The existing Contract entity to update.
     */
    private void mapDtoToEntity(ContractDTO dto, Contract entity) {
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setFrequency(dto.getFrequency());
        entity.setHoursPerWeek(dto.getHoursPerWeek());
    }
}
