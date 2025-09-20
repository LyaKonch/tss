
package bravo.dao;

import bravo.entities.Person;
import bravo.entities.Role;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@LocalBean
@Stateless
public class PersonDAO extends AbstractDAO<Person> {
    public PersonDAO(){
        super(Person.class);
    }
    
    public Optional<Person> findByEmailAddress(String emailAddress) {
        try {
            return Optional.of(em.createNamedQuery("Person.findByEmail", Person.class).
                    setParameter("emailAddress", emailAddress).
                    getSingleResult() //i am not sure if several persons with the same email can exist, but i return single result
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    public List<Person> findByFullName(String firstName, String lastName) {
        return em.createNamedQuery("Person.findByFullName", Person.class)
        .setParameter("firstName", firstName)
        .setParameter("lastName", lastName).getResultList();
    }
    
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
