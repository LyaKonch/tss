package bravo.entities;

import jakarta.persistence.*;

/**
 * Abstract base entity representing a Role in the system.
 * <p>
 * Roles can be assigned to a Person (Employee, Supervisor, Secretary, Assistant, etc.).
 * Uses JOINED inheritance strategy for different role types.
 */
@Entity
@Table(name = "role")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role_type")
@NamedQueries({
        @NamedQuery(name = "Role.findByPerson",
                query = "SELECT r FROM Role r WHERE r.person = :person"),
        @NamedQuery(name = "Role.findByUuid",
                query = "SELECT r FROM Role r WHERE r.uuid = :uuid")
})
public abstract class Role extends AbstractEntity {

    /**
     * The person associated with this role.
     * <p>
     * Optional relationship: a Person may exist without a role.
     * Lazy fetch is used to avoid unnecessary data loading.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /**
     * Getter for the associated person.
     *
     * @return the person assigned to this role, or null if none
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Setter for the associated person.
     * Can be null if the role is not assigned to any person.
     *
     * @param person the person to assign this role to
     */
    public void setPerson(Person person) {
        this.person = person;
    }
}
