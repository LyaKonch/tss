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
 * - Unique identifier (uuid) for entity tracking
 * - JPA version for optimistic locking
 * - Serialization support for network transfer
 * - Proper equals and hashCode implementations for collections
 */
public abstract class AbstractDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 8473915468192745681L;

    private String uuid;
    private int version;

    protected AbstractDto() {
    }

    protected AbstractDto(String uuid, int version) {
        this.uuid = uuid;
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
     * @return the UUID as a string
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Determines if this DTO represents a new entity that hasn't been persisted yet.
     *
     * @return true if this is a new entity (uuid is null), false otherwise
     */
    public boolean isNew() {
        return uuid == null;
    }

    /**
     * Generates a hash code for this DTO based on its UUID.
     *
     * @return the hash code
     * @throws IllegalStateException if the UUID is not set
     */
    @Override
    public int hashCode() {
        if (uuid == null) {
            throw new IllegalStateException("uuid not set");
        }
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.uuid);
        return hash;
    }

    /**
     * Compares this DTO to another object for equality.
     * Two DTOs are considered equal if they have the same UUID.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     * @throws IllegalStateException if the UUID is not set
     */
    @Override
    public boolean equals(Object obj) {
        if (uuid == null) {
            throw new IllegalStateException("uuid not set");
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
        return Objects.equals(this.uuid, other.uuid);
    }
}
