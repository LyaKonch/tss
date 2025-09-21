package bravo.dao;

import bravo.entities.User;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

/**
 * DAO for {@link User} entities.
 * <p>
 * Provides database operations for users, including retrieval by email.
 */
@LocalBean
@Stateless
public class UserDAO extends AbstractDAO<User> {

    /**
     * Default constructor.
     * Initializes this DAO with the {@link User} entity class.
     */
    public UserDAO() {
        super(User.class);
    }

    /**
     * Finds a {@link User} by their email address.
     * <p>
     * Executes the named query {@code User.findByUsername}.
     *
     * @param email the email address to search for
     * @return the User if found, otherwise {@code null}
     */
    public User findByEmail(String email) {
        if (email == null) return null;
        List<User> list = em.createNamedQuery("User.findByUsername", User.class)
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }


}