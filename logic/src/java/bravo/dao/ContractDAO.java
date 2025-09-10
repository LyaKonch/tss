package bravo.dao;

import bravo.entities.Contract;
import bravo.enums.ContractStatus;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@LocalBean
@Stateless
public class ContractDAO extends AbstractDAO<Contract> {

    public ContractDAO() {
        super(Contract.class);
    }

    /**
     * Persists a new contract in the database.
     *
     * @param contract the contract to persist
     * @return the persisted contract with generated ID
     */
    public Contract createContract(Contract contract) {
        return save(contract);
    }

    /**
     * Updates an existing contract in the database.
     *
     * @param contract the contract to update
     * @return the updated contract
     */
    public Contract updateContract(Contract contract) {
        return save(contract);
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

    public Contract findByName(String name) {
        return em.createNamedQuery("Contract.findByName", Contract.class)
                .setParameter("name", name)
                .getSingleResult();
    }

}
