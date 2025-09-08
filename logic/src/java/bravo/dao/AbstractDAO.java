package bravo.dao;

import bravo.entities.AbstractEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.lang.reflect.ParameterizedType;
import java.util.List;


public class AbstractDAO<T extends AbstractEntity> {

    @PersistenceContext(unitName = "BravoTSSPU")
    protected EntityManager em;


    private Class<T> entityClass;


    public AbstractDAO() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public AbstractDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T findById(Long id) {
        return em.find(entityClass, id);
    }

    public T findByUUID(String uuid) {
        return em.createNamedQuery(entityClass.getSimpleName() + ".findByUuid", entityClass)
                .setParameter("uuid", uuid)
                .getSingleResult();
    }

    public void save(T entry) {
        if (entry.getId() == null) {
            em.persist(entry);
        } else {
            em.merge(entry);
        }
    }

    public List<T> findAll() {
        return em.createNamedQuery(entityClass.getSimpleName() + ".findAll", entityClass).getResultList();
    }

    public void delete(T entry) { // delete
        em.remove(em.merge(entry));
    }

    public void deleteById(Long id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        }
    }

    public long count() {
        return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }
}
