package com.safinance.core.domain;

/**
 * Interface that marks a domain object as an Entity (having a stable identity).
 */
public interface Entity {
    /**
     * Retrieves the unique identifier of this entity.
     * 
     * @return the unique String identifier
     */
    String getId();
}
