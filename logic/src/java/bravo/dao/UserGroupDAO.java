package bravo.dao;

import bravo.entities.UserGroup;
import bravo.entities.UserGroupId;
import bravo.enums.GroupName;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * DAO for managing {@link UserGroup} mappings.
 * <p>
 * Provides operations to add, delete, and query user-to-group relationships.
 * This DAO does not extend {@link AbstractDAO} because {@link UserGroup} has a composite key.
 */
@LocalBean
@Stateless
public class UserGroupDAO {

    @PersistenceContext(unitName = "BravoTSSPU")
    private EntityManager em;

    /**
     * Adds a mapping between a user's email and a group.
     * If the mapping already exists, no action is taken.
     *
     * @param email     the user's email (must not be null)
     * @param groupName the group to assign (must not be null)
     * @throws IllegalArgumentException if email or groupName is null
     */
    public void addMapping(String email, GroupName groupName) {
        if (email == null || groupName == null) {
            throw new IllegalArgumentException("email and groupName must be not null");
        }
        UserGroupId id = new UserGroupId(email, groupName);
        UserGroup existing = em.find(UserGroup.class, id);
        if (existing == null) {
            em.persist(new UserGroup(id));
        }
    }

    /**
     * Deletes a mapping between a user's email and a group.
     * If the mapping does not exist, no action is taken.
     *
     * @param email     the user's email
     * @param groupName the group to remove
     */
    public void deleteMapping(String email, GroupName groupName) {
        UserGroupId id = new UserGroupId(email, groupName);
        UserGroup ug = em.find(UserGroup.class, id);
        if (ug != null) {
            em.remove(ug);
        }
    }

    /**
     * Deletes all group mappings for a specific user.
     *
     * @param email the user's email
     */
    public void deleteAllMappingsForUser(String email) {
        em.createQuery("DELETE FROM UserGroup ug WHERE ug.id.email = :u")
                .setParameter("u", email)
                .executeUpdate();
    }

     /**
     * Finds all groups that a specific user belongs to.
     *
     * @param email the user's email
     * @return list of {@link GroupName} values for the user
     */
    public List<GroupName> findGroupsByEmail(String email) {
        TypedQuery<GroupName> q = em.createQuery(
                "SELECT ug.id.groupName FROM UserGroup ug WHERE ug.id.email = :u", GroupName.class);
        q.setParameter("u", email);
        return q.getResultList();
    }

     /**
     * Finds all emails of users who belong to a specific group.
     *
     * @param groupName the group
     * @return list of user emails
     */
    public List<String> findEmailByGroup(GroupName groupName) {
        TypedQuery<String> q = em.createQuery(
                "SELECT ug.id.email FROM UserGroup ug WHERE ug.id.groupName = :g", String.class);
        q.setParameter("g", groupName);
        return q.getResultList();
    }

    /**
     * Checks if a mapping exists between a user's email and a group.
     *
     * @param email     the user's email
     * @param groupName the group
     * @return true if the mapping exists, false otherwise
     */
    public boolean exists(String email, GroupName groupName) {
        return em.find(UserGroup.class, new UserGroupId(email, groupName)) != null;
    }

    /**
     * Finds a {@link UserGroup} mapping by its composite key.
     *
     * @param email     the user's email
     * @param groupName the group
     * @return an {@link Optional} containing the UserGroup if found, otherwise empty
     */
    public Optional<UserGroup> findById(String email, GroupName groupName) {
        UserGroup ug = em.find(UserGroup.class, new UserGroupId(email, groupName));
        return Optional.ofNullable(ug);
    }
}
