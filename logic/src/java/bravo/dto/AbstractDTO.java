package bravo.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for Data Transfer Objects (DTOs).
 * <p>
 * Provides common fields such as {@code id}, {@code uuid}, and {@code version},
 * as well as standard implementations of {@link #equals(Object)} and {@link #hashCode()}.
 * All DTOs in the system should extend this class.
 */
public abstract class AbstractDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Database primary key (optional for DTOs, mainly for persistence purposes).
     */
    private Long id;

    /**
     * Universally unique identifier for the DTO.
     * Used for distinguishing DTOs even before they are persisted.
     */
    private String uuid;

    /**
     * Version number for optimistic locking or DTO versioning.
     */
    private int version;

    /**
     * Default constructor for serialization frameworks.
     */
    protected AbstractDTO() {
    }

    /**
     * Constructor with UUID and version.
     *
     * @param uuid    the universally unique identifier
     * @param version the version number
     */
    protected AbstractDTO(String uuid, int version) {
        this.uuid = uuid;
        this.version = version;
    }

    /**
     * Returns the database ID of the DTO.
     *
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the DTO.
     *
     * @param id the ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the UUID of the DTO.
     *
     * @return the UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the UUID of the DTO.
     *
     * @param uuid the UUID to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the version number of the DTO.
     *
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version number of the DTO.
     *
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Checks whether this DTO is new (not yet persisted).
     *
     * @return {@code true} if the UUID is {@code null}, {@code false} otherwise
     */
    public boolean isNew() {
        return uuid == null;
    }

    /**
     * Computes a hash code based on the UUID.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    /**
     * Determines equality based on UUID.
     *
     * @param obj the object to compare
     * @return {@code true} if UUIDs are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractDTO that)) return false;
        return Objects.equals(this.uuid, that.getUuid());
    }
}
