package bravo.dao;

import bravo.entities.Contract;
import bravo.enums.ContractStatus;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.logging.Logger;

@LocalBean
@Stateless
public class ContractDAO extends AbstractDAO<Contract> {

    private static final Logger logger = Logger.getLogger(ContractDAO.class.getName());

    public ContractDAO() {
        super(Contract.class);
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
    public void deleteByUuid(String uuid) {
        Contract contract = findByUuid(uuid);
        if (contract != null) {
            delete(contract);
        }
    }
}
