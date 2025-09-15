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
     * Retrieves all active contracts from the database for a given employee.
     *
     * @param email the email address of the employee
     * @return a list of all active contracts for the employee
     */
    public List<Contract> findActiveEmployeeContractByEmail(String email) {
        return em.createQuery(
                        "SELECT e.contract FROM Employee e " +
                                "WHERE e.person.emailAddress = :email " +
                                "AND e.contract.status = :status",
                        Contract.class
                )
                .setParameter("email", email)
                .setParameter("status", ContractStatus.STARTED)
                .getResultList();
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
