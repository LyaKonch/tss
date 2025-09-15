package bravo.dao;

import bravo.entities.Person;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

@LocalBean
@Stateless
public class PersonDAO extends AbstractDAO<Person> {

    public PersonDAO() {
        super(Person.class);
    }

    public Person findByEmail(String email) {
        return em.createNamedQuery("Person.findByEmail", Person.class)
                .setParameter("email", email)
                .getSingleResult();
    }
}
