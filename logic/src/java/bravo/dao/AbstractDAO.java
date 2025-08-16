/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bravo.dao;

import bravo.entities.AbstractEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 *
 * @author Женіч
 * @param <T>
 */
public class AbstractDAO<T extends AbstractEntity> {
     // should i add separate persistence unit for this?
    @PersistenceContext(unitName = "BravoTSSPU")
    protected EntityManager em;
    
    // this is done because all DAO classes will have the same CRUD operations, thus make sense to make it common
    private Class<T> entityClass; // represent object of Class T
    // it may take up some space, but it is a good compromise for universality it brings
    // it's created only once when DAO object with specific type created
    
    public AbstractDAO(){
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]; 
    }
    
    public AbstractDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
     public T findByID(Long id){
        return em.find(entityClass, id);
    }
    
     public T findByUUID(String uuid){
        return em.createNamedQuery(entityClass.getSimpleName()+".findByUuid", entityClass)
                .setParameter("uuid", uuid)
                    .getSingleResult();
    }
    
    public void save(T entry) { // create and update
        if (entry.getId() == null) { // if no id => then create
            em.persist(entry);
        } else { // if there is id then update
            em.merge(entry);
        }
    }
    
    public List<T> findAll(){
        return em.createNamedQuery(entityClass.getSimpleName()+".findAll", entityClass).getResultList();
    }
    
    public void delete(T entry) { // delete
        em.remove(em.merge(entry));
    }
    
    public void deleteById(Long id) {
        T entity = findByID(id);
        if (entity != null) {
            delete(entity);
        }
    }
    
    public long count() {
        return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
             .getSingleResult();
    }
}
