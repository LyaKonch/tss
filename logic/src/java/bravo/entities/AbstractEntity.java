package bravo.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // database primary key identifier

    @Column(unique = true, length = 36) // additional info for JPA how to map this column 
    private String uuid; // program level identifier just in case 

    @Version
    private int version;


    public AbstractEntity() {
    }

    public AbstractEntity(boolean newEntity) {
        if (newEntity) { // if true it means new object is being created which implies new uuid need to be assigned
            uuid = UUID.randomUUID().toString();
        }
    }


    @PrePersist
    private void generateUuidOnPersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.uuid);
        return hash;
    }

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
