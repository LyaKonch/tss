package bravo.dao;

import bravo.entities.Contract;
import bravo.enums.ContractStatus;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class ContractsAccess {

    @PersistenceContext(unitName = "BravoTSSPU")
    private EntityManager em;

    /**
     * Persists a new contract in the database.
     *
     * @param contract the contract to persist
     * @return the persisted contract with generated ID
     */
    public Contract createContract(Contract contract) {
        em.persist(contract);
        return contract;
    }

    /**
     * Updates an existing contract in the database.
     *
     * @param contract the contract to update
     * @return the updated contract
     */
    public Contract updateContract(Contract contract) {
        return em.merge(contract);
    }

    /**
     * Finds a contract by its UUID.
     *
     * @param uuid the UUID of the contract
     * @return the found contract or null if not found
     */
    public Contract findContractByUuid(String uuid) {
        return em.find(Contract.class, uuid);
    }

    /**
     * Retrieves all contracts from the database.
     *
     * @return a list of all contracts
     */
    public List<Contract> getAllContracts() {
        TypedQuery<Contract> query = em.createQuery("SELECT c FROM Contract c", Contract.class);
        return query.getResultList();
    }

    /**
     * Retrieves all active, i.e. started, contracts from the database.
     *
     * @return a list of all active contracts
     */
    public List<Contract> getActiveContracts() {
        TypedQuery<Contract> query = em.createQuery(
                "SELECT c FROM Contract c WHERE c.status = :status",
                Contract.class);
        query.setParameter("status", ContractStatus.STARTED);
        return query.getResultList();
    }

    /**
     * Deletes a contract from the database.
     *
     * @param uuid the UUID of the contract to delete
     */
    public void deleteContract(String uuid) {
        Contract contract = findContractByUuid(uuid);
        if (contract != null) {
            em.remove(contract);
        }
    }
}
