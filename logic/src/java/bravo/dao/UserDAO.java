package bravo.dao;

import bravo.entities.User;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

@LocalBean
@Stateless
public class UserDAO extends AbstractDAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public User findByEmail(String email) {
        if (email == null) return null;
        List<User> list = em.createNamedQuery("User.findByUsername", User.class)
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }


}