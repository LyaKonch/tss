package bravo.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role_type")
@NamedQueries({@NamedQuery(name = "Role.findByPerson", query = "SELECT r FROM Role r WHERE r.person = :person")})
public abstract class Role extends AbstractEntity {

    // now Person can be created without a role, which also matchs domain model.
    @ManyToOne(optional = true, fetch = FetchType.LAZY) 
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

}

