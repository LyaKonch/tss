package bravo.logic;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import jakarta.ejb.Local;

import java.time.LocalDate;
import java.util.List;

@Local
public interface ContractLogic {

    /**
     * Creates a new contract entity from the given DTO.
     *
     * @return the created ContractDTO with generated ID
     */
    ContractDTO create(ContractDTO contractDto);

    /**
     * Updates an existing contract.
     *
     * @param contract the contract DTO with updated data
     * @return the updated contract
     */
    ContractDTO update(ContractDTO contract);

    /**
     * Retrieves a contract by its UUID.
     *
     * @param uuid the UUID of the contract
     * @return the found contract or null if not found
     */
    ContractDTO findByUuid(String uuid);

    /**
     * Retrieves all contracts for a given email address that are active with the person being an employee.
     *
     * @return list of contracts by a person
     */
    List<ContractDTO> findActiveEmployeeContractsByEmail(String email);

    /**
     * Retrieves all contracts.
     *
     * @return a list of all contracts
     */
    List<ContractDTO> findAll();

    /**
     * Retrieves all active contracts.
     *
     * @return a list of all active contracts
     */
    List<ContractDTO> getActive();

    /**
     * Terminates a contract by setting its status to TERMINATED and recording the termination date.
     *
     * @param uuid            the UUID of the contract to terminate
     * @param terminationDate the date when the contract is terminated
     * @return the updated contract
     */
    ContractDTO terminate(String uuid, LocalDate terminationDate);

    /**
     * Calculates the hours due for a contract based on working days per week and hours per week.
     *
     * @param contract the contract to calculate hours for
     * @return the calculated hours due
     */
    double calculateHoursDue(ContractDTO contract);

    /**
     * Calculates the vacation hours for a contract based on vacation days per year and hours per week.
     *
     * @param contract the contract to calculate vacation hours for
     * @return the calculated vacation hours
     */
    double calculateVacationHours(ContractDTO contract);

    void deleteContract(String uuid);

    ContractDTO startContract(ContractDTO contractDto);

    ContractDTO archiveContract(String uuid);

    List<ContractDTO> findContractsAssociatedWithPerson(PersonDTO person);

    ContractDTO abortContract(String uuid);
}
