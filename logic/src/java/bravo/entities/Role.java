package bravo.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role_type")
@NamedQueries({
        @NamedQuery(name = "Role.findByPerson",
                query = "SELECT r FROM Role r WHERE r.person = :person")
})
public abstract class Role extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }


}

