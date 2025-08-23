package bravo.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * AbstractDTO serves as a base class for all Data Transfer Objects (DTOs) in the application.
 * <p>
 * DTOs in Jakarta EE are used to:
 * 1. Transfer data between different layers of the application (presentation, business, persistence)
 * 2. Decouple the client-facing API from the internal domain model
 * 3. Reduce network overhead by transferring only the required data
 * 4. Provide a clear contract for data exchange
 * 5. Protect sensitive information by exposing only what's necessary
 * <p>
 * This AbstractDTO provides common functionality for all DTOs:
 * - Unique identifier (id) for entity tracking
 * - JPA version for optimistic locking
 * - Serialization support for network transfer
 * - Proper equals and hashCode implementations for collections
 */
public abstract class AbstractDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 8473915468192745681L;

    private Long id;

    private int version;

    protected AbstractDto() {
    }

    protected AbstractDto(Long id, int version) {
        this.id = id;
        this.version = version;
    }

    /**
     * Returns the JPA version used.
     *
     * @return the JPA version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the unique identifier of this DTO.
     *
     * @return the ID as a string
     */
    public Long getId() {
        return id;
    }

    /**
     * Determines if this DTO represents a new entity that hasn't been persisted yet.
     *
     * @return true if this is a new entity (id is null), false otherwise
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * Generates a hash code for this DTO based on its ID.
     *
     * @return the hash code
     * @throws IllegalStateException if the ID is not set
     */
    @Override
    public int hashCode() {
        if (id == null) {
            throw new IllegalStateException("id not set");
        }
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }

    /**
     * Compares this DTO to another object for equality.
     * Two DTOs are considered equal if they have the same ID.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     * @throws IllegalStateException if the ID is not set
     */
    @Override
    public boolean equals(Object obj) {
        if (id == null) {
            throw new IllegalStateException("id not set");
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDto other = (AbstractDto) obj;
        return Objects.equals(this.id, other.id);
    }
}
