package bravo.dao;

import bravo.entities.*;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

/**
 * DAO for {@link Role} entities.
 * <p>
 * Provides database operations for roles, including retrieval by person or contract,
 * and deletion of roles.
 */
@LocalBean
@Stateless
public class RoleDAO extends AbstractDAO<Role> {

    /**
     * Default constructor.
     * Initializes this DAO with the {@link Role} entity class.
     */
    public RoleDAO() {
        super(Role.class);
    }

    /**
     * Finds all roles associated with a given {@link Person}.
     *
     * @param person the person to search roles for
     * @return list of roles assigned to the person
     */
    public List<Role> findByPerson(Person person) {
        return em.createNamedQuery("Role.findByPerson", Role.class)
                .setParameter("person", person)
                .getResultList();
    }

    /**
     * Finds the {@link Supervisor} role associated with a specific {@link Contract}.
     *
     * @param contract the contract to search for
     * @return the Supervisor role, or {@code null} if none exists
     */
    public Supervisor findSupervisorByContract(Contract contract) {
        try {
            return em.createNamedQuery("Supervisor.findByContract", Supervisor.class)
                    .setParameter("contract", contract)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Finds the {@link Employee} role associated with a specific {@link Contract}.
     *
     * @param contract the contract to search for
     * @return the Employee role, or {@code null} if none exists
     */
    public Employee findEmployeeByContract(Contract contract) {
        try {
            return em.createNamedQuery("Employee.findByContract", Employee.class)
                    .setParameter("contract", contract)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Finds all {@link Secretary} roles associated with a specific {@link Contract}.
     *
     * @param contract the contract to search for
     * @return list of Secretary roles
     */
    public List<Secretary> findSecretariesByContract(Contract contract) {
        return em.createNamedQuery("Secretary.findByContract", Secretary.class)
                .setParameter("contract", contract)
                .getResultList();
    }

    /**
     * Finds all {@link Assistant} roles associated with a specific {@link Contract}.
     *
     * @param contract the contract to search for
     * @return list of Assistant roles
     */
    public List<Assistant> findAssistantsByContract(Contract contract) {
        return em.createNamedQuery("Assistant.findByContract", Assistant.class)
                .setParameter("contract", contract)
                .getResultList();
    }

    /**
     * Deletes all roles associated with a given contract ID using native queries.
     * <p>
     * Executes deletion for employee, supervisor, secretary, and assistant tables.
     *
     * @param contractId the contract ID
     * @return total number of deleted records
     */
    public int deleteRolesByContractIdNative(Long contractId) {
        if (contractId == null) return 0;


        int total = 0;
        total += em.createNativeQuery("DELETE FROM employee WHERE contract_id = ?")
                .setParameter(1, contractId)
                .executeUpdate();

        total += em.createNativeQuery("DELETE FROM supervisor WHERE contract_id = ?")
                .setParameter(1, contractId)
                .executeUpdate();

        total += em.createNativeQuery("DELETE FROM secretary WHERE contract_id = ?")
                .setParameter(1, contractId)
                .executeUpdate();

        total += em.createNativeQuery("DELETE FROM assistant WHERE contract_id = ?")
                .setParameter(1, contractId)
                .executeUpdate();

        em.flush();
        em.clear();

        return total;
    }

    /**
     * Deletes a role and updates the corresponding {@link Person}'s roles collection.
     * <p>
     * JPA only removes the entity from the database; this method also ensures
     * the role is removed from the Person's in-memory collection to keep it consistent.
     *
     * @param role the role to delete
     */
    public void deleteWithPersonUpdate(Role role) {
        if (role == null) {
            return;
        }
        // update role collection in person
        Person person = role.getPerson();
        if (person != null) {
            person.getRoles().remove(role);
        }

        // delete from db
        em.remove(em.contains(role) ? role : em.merge(role));
    }
}
