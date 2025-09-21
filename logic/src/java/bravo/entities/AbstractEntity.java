package bravo.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all JPA entities.
 * <p>
 * Provides common fields such as:
 * <ul>
 *     <li>{@code id} - primary key (auto-generated)</li>
 *     <li>{@code uuid} - unique identifier across instances</li>
 *     <li>{@code version} - for optimistic locking</li>
 * </ul>
 * <p>
 * Equality and hashCode are based on the {@code uuid} field.
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    /**
     * Primary key for the entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Globally unique identifier (UUID).
     */
    @Column(unique = true, length = 36)
    private UUID uuid;

    /**
     * Version field for optimistic locking.
     */
    @Version
    private int version;


    /**
     * Default constructor that initializes a new UUID.
     */
    public AbstractEntity() {
        uuid = UUID.randomUUID();
    }


    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    /**
     * Equality is based on UUID, not other field contents.
     *
     * @param obj the object to compare with
     * @return {@code true} if the UUIDs are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractEntity other = (AbstractEntity) obj;
        return Objects.equals(this.uuid, other.uuid);
    }
}
