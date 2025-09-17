package bravo.dao;

import bravo.entities.AbstractEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


public class AbstractDAO<T extends AbstractEntity> {

    @PersistenceContext(unitName = "BravoTSSPU")
    protected EntityManager em;

    private static final Logger logger = Logger.getLogger(AbstractDAO.class.getName());

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

    public T findByUuid(String uuid) {
        return em.createNamedQuery(entityClass.getSimpleName() + ".findByUuid", entityClass)
                .setParameter("uuid", UUID.fromString(uuid))
                .getSingleResult();
    }


    public T save(T entry) {
        try {
            if (entry.getId() == null) {
                em.persist(entry);
            } else {
                entry = em.merge(entry);
            }
            return entry;
        } catch (ConstraintViolationException exc) {
            for (ConstraintViolation<?> violation : exc.getConstraintViolations()) {
                logger.severe("Validation failed: "
                        + "Property=" + violation.getPropertyPath()
                        + ", Invalid value=" + violation.getInvalidValue()
                        + ", Message=" + violation.getMessage());
            }
            throw exc;
        }
    }

    public List<T> findAll() {
        return em.createNamedQuery(entityClass.getSimpleName() + ".findAll", entityClass).getResultList();
    }

    public void delete(T entry) {
        if (entry == null) return;
        T managed = em.contains(entry) ? entry : em.merge(entry);
        em.remove(managed);
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
