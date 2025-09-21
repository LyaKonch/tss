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

/**
 * Generic abstract DAO base class for JPA entities.
 * Provides standard CRUD operations such as find, save, delete, and count.
 * Concrete DAO classes should extend this class.
 *
 * @param <T> type of used Entity
 */
public class AbstractDAO<T extends AbstractEntity> {

    /**
     * Logger for DAO-related messages and validation errors.
     */
    private static final Logger logger = Logger.getLogger(AbstractDAO.class.getName());

    /**
     * EntityManager for interacting with the persistence unit.
     * Injected by the application server (e.g., Payara, WildFly).
     */
    @PersistenceContext(unitName = "BravoTSSPU")
    protected EntityManager em;

    /**
     * The entity class (e.g., Contract.class).
     * Used when creating queries and with the EntityManager.
     */
    private Class<T> entityClass;

    /**
     * Default constructor.
     * <p>
     * Determines the generic entity type via reflection.
     * Works only if the subclass specifies the type explicitly,
     * e.g. {@code class ContractDAO extends AbstractDAO<Contract>}.
     */
    public AbstractDAO() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Constructor with explicit entity class.
     *
     * @param entityClass class of the managed entity
     */
    public AbstractDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Finds an entity by its database ID.
     *
     * @param id primary key of the entity
     * @return the found entity or {@code null} if not present
     */
    public T findById(Long id) {
        return em.find(entityClass, id);
    }

    /**
     * Finds an entity by its UUID.
     * Requires a named query with the signature {@code Entity.findByUuid}.
     *
     * @param uuid UUID as a string
     * @return the found entity or {@code null} if not present
     */
    public T findByUuid(String uuid) {
        return getSingleResultOrNull(
                em.createNamedQuery(entityClass.getSimpleName() + ".findByUuid", entityClass)
                        .setParameter("uuid", UUID.fromString(uuid))
        );
    }

    /**
     * Helper method to safely extract a single result from a query.
     * Returns {@code null} if no results are found.
     * Logs a warning and returns the first result if multiple are found.
     *
     * @param q JPA TypedQuery
     * @return single result or {@code null}
     */
    protected T getSingleResultOrNull(jakarta.persistence.TypedQuery<T> q) {
        List<T> list = q.getResultList();
        if (list == null || list.isEmpty()) return null;
        if (list.size() > 1) {
            logger.warning("Expected single result for query but found " + list.size() +
                    ". Returning first result. Query: " + q);
        }
        return list.get(0);
    }

    /**
     * Saves or updates an entity.
     * <ul>
     *     <li>New entities (id = null) are persisted via {@link EntityManager#persist}.</li>
     *     <li>Existing entities are updated via {@link EntityManager#merge}.</li>
     * </ul>
     * If validation fails (Bean Validation), violations are logged
     * and the {@link ConstraintViolationException} is rethrown.
     *
     * @param entry entity to save
     * @return the persisted or merged entity
     * @throws ConstraintViolationException if Bean Validation fails
     */
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

    /**
     * Retrieves all entities of type {@code T}.
     * Requires a named query with the signature {@code Entity.findAll}.
     *
     * @return list of all entities
     */
    public List<T> findAll() {
        return em.createNamedQuery(entityClass.getSimpleName() + ".findAll", entityClass).getResultList();
    }

    /**
     * Deletes an entity from the database.
     * If the entity is not managed in the current persistence context,
     * it will be merged first.
     *
     * @param entry entity to delete (can be {@code null})
     */
    public void delete(T entry) {
        if (entry == null) return;
        T managed = em.contains(entry) ? entry : em.merge(entry);
        em.remove(managed);
    }

    /**
     * Deletes an entity by its ID.
     *
     * @param id primary key of the entity
     */
    public void deleteById(Long id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        }
    }

    /**
     * Counts the number of entities in the database.
     *
     * @return entity count
     */
    public long count() {
        return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }
}
