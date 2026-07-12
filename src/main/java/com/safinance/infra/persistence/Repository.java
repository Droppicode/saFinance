package com.safinance.infra.persistence;

import java.util.List;

/**
 * Generic repository interface for managing persistence of entities.
 * 
 * @param <T> the entity type
 * @param <ID> the identifier type
 */
public interface Repository<T, ID> {
    /**
     * Finds an entity by its identifier.
     * 
     * @param id the identifier
     * @return the entity, or null if not found
     */
    T findById(ID id);
    
    /**
     * Retrieves all entities managed by this repository.
     * 
     * @return a list of all entities
     */
    List<T> findAll();
    
    /**
     * Saves or updates the entity in the repository.
     * 
     * @param entity the entity to save
     */
    void save(T entity);
    /**
     * Saves or updates multiple entities in the repository atomically (or as close to atomic as possible).
     * 
     * @param entities the list of entities to save
     */
    void saveAll(List<T> entities);
}
