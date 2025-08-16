/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bravo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Женіч
 */
@MappedSuperclass
public class AbstractEntity implements Serializable  {
    // likewise AbstractDTO
    // it's not really necessary, it provides integrity during tranfer between beans or for remote interfaces
    // basicaly it abstracts id field for every entity
    // it's up to us whether to implement this or not
    
    // i add this only to root classes that have its own table and are sort of independant requiring own unique identifier 
    
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
