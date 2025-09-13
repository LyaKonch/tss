package bravo.logic;

import bravo.dto.ContractDTO;
import bravo.entities.Contract;
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
    ContractDTO createContract(ContractDTO contract);

    /**
     * Updates an existing contract.
     *
     * @param contract the contract DTO with updated data
     * @return the updated contract
     */
    ContractDTO updateContract(ContractDTO contract);

    /**
     * Retrieves a contract by its UUID.
     *
     * @param uuid the UUID of the contract
     * @return the found contract or null if not found
     */
    ContractDTO getContractByUuid(String uuid);

    /**
     * Retrieves all contracts.
     *
     * @return a list of all contracts
     */
    List<ContractDTO> getAllContracts();

    /**
     * Retrieves all active contracts.
     *
     * @return a list of all active contracts
     */
    List<ContractDTO> getActiveContracts();

    /**
     * Terminates a contract by setting its status to TERMINATED and recording
     * the termination date.
     *
     * @param uuid the UUID of the contract to terminate
     * @param terminationDate the date when the contract is terminated
     * @return the updated contract
     */
    ContractDTO terminateContract(String uuid, LocalDate terminationDate);

    Double getHoursDue(Contract contract);
    
    void deleteContract(String uuid);
    
    ContractDTO startContract(String uuid); 
    
    ContractDTO archiveContract(String uuid);
}
