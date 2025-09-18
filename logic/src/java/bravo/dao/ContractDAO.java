package bravo.dao;

import bravo.entities.Contract;
import bravo.enums.ContractStatus;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

import java.util.logging.Logger;
@LocalBean
@Stateless
public class ContractDAO extends AbstractDAO<Contract> {

    private static final Logger logger = Logger.getLogger(ContractDAO.class.getName());

    public ContractDAO() {
        super(Contract.class);
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

    /**
     * Deletes a contract from the database.
     *
     * @param uuid the UUID of the contract to delete
     */
    public void deleteByUuid(String uuid) {
        Contract contract = findByUuid(uuid);
        if (contract != null) {
            delete(contract);
        }
    }
}
