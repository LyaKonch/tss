package bravo.dao;

import bravo.entities.Person;
import bravo.entities.Role;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

/**
 * DAO for the {@link Person} entity.
 * <p>
 * Provides database operations specific to {@link Person}, 
 * extending the generic functionality of {@link AbstractDAO}.
 */
@LocalBean
@Stateless
public class PersonDAO extends AbstractDAO<Person> {
     /**
     * Default constructor.
     * Initializes this DAO with the {@link Person} entity class.
     */
    public PersonDAO() {
        super(Person.class);
    }

     /**
     * Finds a {@link Person} by their email address.
     * <p>
     * Expects a named query with the signature {@code Person.findByEmail}.
     *
     * @param emailAddress the email address to search for
     * @return an {@link Optional} containing the matching Person if found, otherwise empty
     */
    public Optional<Person> findByEmailAddress(String emailAddress) {
        try {
            return Optional.of(em.createNamedQuery("Person.findByEmail", Person.class).
                    setParameter("emailAddress", emailAddress).
                    getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Finds a list of {@link Person} entities by their full name.
     * <p>
     * Expects a named query with the signature {@code Person.findByFullName}.
     *
     * @param firstName the first name to search for
     * @param lastName  the last name to search for
     * @return list of matching Person entities
     */
    public List<Person> findByFullName(String firstName, String lastName) {
        return em.createNamedQuery("Person.findByFullName", Person.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName).getResultList();
    }

    /**
     * Finds all {@link Person} entities by their associated role type.
     * <p>
     * Since the discriminator column stores the role as a string (class name),
     * the class name of the role is used as the query parameter.
     * <p>
     * Expects a named query with the signature {@code Person.findByRoleType}.
     *
     * @param roleClass the role class (e.g., {@code Employee.class})
     * @return list of Person entities with the given role type
     */
    public List<Person> findByRoleType(Class<? extends Role> roleClass) {
        String roleTypeName = roleClass.getSimpleName();
        // this one is tricky
        // roleClass parameter  represents the specific Role type
        //but in our discriminator column it would store as the string. eg name of the class like "Employee"
        // so i need the name of the class, not the class itself
        return em.createNamedQuery("Person.findByRoleType", Person.class).
                setParameter("roleType", roleTypeName).getResultList();
    }
}
