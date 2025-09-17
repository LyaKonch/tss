package bravo.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import bravo.entities.Person;
import jakarta.persistence.*;
import java.io.Serializable;


/**
 * Entity class for the 'users' database table.
 * Represents a user and their associated groups.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", referencedColumnName = "id", nullable = false, unique = true)
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setPerson(Person person) {
        this.person = person;
        if (person != null) {
            this.username = person.getEmailAddress();
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", group='" + group.getGroupName() + '\'' +
                '}';
    }
}
