package bravo.dao;

import bravo.entities.Assistant;
import bravo.entities.Contract;
import bravo.entities.Person;
import bravo.entities.Role;
import bravo.entities.Secretary;
import bravo.entities.Supervisor;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

@LocalBean
@Stateless
public class RoleDAO extends AbstractDAO<Role> {

    public RoleDAO() {
        super(Role.class);
    }

    public List<Role> findByPerson(Person person) {
        return em.createNamedQuery("Role.findByPerson", Role.class)
                .setParameter("person", person)
                .getResultList();
    }

    // Finds the Supervisor role associated with a specific Contract.
    public Supervisor findSupervisorByContract(Contract contract) {
        try {
            return em.createNamedQuery("Supervisor.findByContract", Supervisor.class)
                    .setParameter("contract", contract)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // Finds all Secretary roles associated with a specific Contract 
    public List<Secretary> findSecretariesByContract(Contract contract) {
        return em.createNamedQuery("Secretary.findByContract", Secretary.class)
                .setParameter("contract", contract)
                .getResultList();
    }

    // Finds all Assistant roles associated with a specific Contract.
    public List<Assistant> findAssistantsByContract(Contract contract) {
        return em.createNamedQuery("Assistant.findByContract", Assistant.class)
                .setParameter("contract", contract)
                .getResultList();
    }

    
    //Deletes a role and updates the person's roles collection because JPA only deletes from db, not Person's set of roles
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
