package bravo.logic;

import bravo.dto.ContractDto;
import jakarta.ejb.Local;

import java.time.LocalDate;
import java.util.List;

@Local
public interface ContractLogic {

    /**
     * Creates a new contract.
     *
     * @param contract the contract DTO with the data
     * @return the created contract with generated ID
     */
    ContractDto createContract(ContractDto contract);

    /**
     * Updates an existing contract.
     *
     * @param contract the contract DTO with updated data
     * @return the updated contract
     */
    ContractDto updateContract(ContractDto contract);

    /**
     * Retrieves a contract by its UUID.
     *
     * @param uuid the UUID of the contract
     * @return the found contract or null if not found
     */
    ContractDto getContractByUuid(String uuid);

    /**
     * Retrieves all contracts.
     *
     * @return a list of all contracts
     */
    List<ContractDto> getAllContracts();

    /**
     * Retrieves all active contracts.
     *
     * @return a list of all active contracts
     */
    List<ContractDto> getActiveContracts();

    /**
     * Terminates a contract by setting its status to TERMINATED and recording the termination date.
     *
     * @param uuid            the UUID of the contract to terminate
     * @param terminationDate the date when the contract is terminated
     * @return the updated contract
     */
    ContractDto terminateContract(String uuid, LocalDate terminationDate);

    /**
     * Calculates the hours due for a contract based on working days per week and hours per week.
     *
     * @param contract the contract to calculate hours for
     * @return the calculated hours due
     */
    double calculateHoursDue(ContractDto contract);

    /**
     * Calculates the vacation hours for a contract based on vacation days per year and hours per week.
     *
     * @param contract the contract to calculate vacation hours for
     * @return the calculated vacation hours
     */
    double calculateVacationHours(ContractDto contract);
}
