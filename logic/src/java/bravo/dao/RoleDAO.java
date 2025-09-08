package bravo.dao;

import bravo.entities.Person;
import bravo.entities.Role;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

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
}
