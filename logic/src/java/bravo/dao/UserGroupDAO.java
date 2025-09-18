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

@LocalBean
@Stateless
public class UserGroupDAO {

    @PersistenceContext(unitName = "BravoTSSPU")
    private EntityManager em;

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

    public void deleteMapping(String email, GroupName groupName) {
        UserGroupId id = new UserGroupId(email, groupName);
        UserGroup ug = em.find(UserGroup.class, id);
        if (ug != null) {
            em.remove(ug);
        }
    }

    public void deleteAllMappingsForUser(String email) {
        em.createQuery("DELETE FROM UserGroup ug WHERE ug.id.email = :u")
                .setParameter("u", email)
                .executeUpdate();
    }

    public List<GroupName> findGroupsByEmail(String email) {
        TypedQuery<GroupName> q = em.createQuery(
                "SELECT ug.id.groupName FROM UserGroup ug WHERE ug.id.email = :u", GroupName.class);
        q.setParameter("u", email);
        return q.getResultList();
    }

    public List<String> findEmailByGroup(GroupName groupName) {
        TypedQuery<String> q = em.createQuery(
                "SELECT ug.id.email FROM UserGroup ug WHERE ug.id.groupName = :g", String.class);
        q.setParameter("g", groupName);
        return q.getResultList();
    }

    public boolean exists(String email, GroupName groupName) {
        return em.find(UserGroup.class, new UserGroupId(email, groupName)) != null;
    }

    public Optional<UserGroup> findById(String email, GroupName groupName) {
        UserGroup ug = em.find(UserGroup.class, new UserGroupId(email, groupName));
        return Optional.ofNullable(ug);
    }
}
