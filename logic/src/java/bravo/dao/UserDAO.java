/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bravo.dao;

import bravo.entities.User;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author minschloen
 */
@LocalBean
@Stateless
public class UserDAO extends AbstractDAO<User>{


    public UserDAO(){
        super(User.class);
    }

    public User getUser(String emailAddress) {
        User ue;
        try {
            ue = em.createNamedQuery("findByEmailAddress", User.class)
                    .setParameter("emailAddress", emailAddress)
                    .getSingleResult();
        } catch (NoResultException e) {
            ue = new User(true);
            ue.setEmailAddress(emailAddress);
            em.persist(ue);
        }
        return ue;
    }
}

