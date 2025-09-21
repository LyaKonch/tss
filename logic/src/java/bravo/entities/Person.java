package bravo.entities;

import bravo.enums.FederalState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static bravo.enums.FederalState.RHINELAND_PALATINATE;

/**
 * JPA Entity representing a Person.
 * <p>
 * A Person can have multiple Roles (Employee, Supervisor, Secretary, Assistant, etc.)
 * and is associated with attributes like name, date of birth, email, consent, and federal state.
 */
@Entity
@Table(name = "person")
@NamedQueries({
        @NamedQuery(name = "Person.findByUuid",
                query = "SELECT e FROM Person e WHERE e.uuid = :uuid"),
        @NamedQuery(name = "Person.findAll",
                query = "SELECT e FROM Person e"),
        @NamedQuery(name = "Person.findByEmail",
                query = "SELECT e FROM Person e WHERE e.emailAddress = :emailAddress"),
        @NamedQuery(name = "Person.findByFullName",
                query = "SELECT e FROM Person e WHERE e.firstName = :firstName AND e.lastName = :lastName"),
        @NamedQuery(name = "Person.findByRoleType",
                query = "SELECT e FROM Person e JOIN e.roles r WHERE TYPE(r) = :roleType")
})
public class Person extends AbstractEntity {

    /** The first name of the person. Cannot be blank. */
    @NotBlank
    private String firstName;

    /** The last name of the person. Cannot be blank. */
    @NotBlank
    private String lastName;

    /** The date of birth of the person. */
    private LocalDate dateOfBirth;

    /** Unique email address for the person. Cannot be null. */
    @Column(unique = true, nullable = false)
    private String emailAddress;

    /** Whether the person has given consent. */
    private boolean consent;

    /** Federal state of the person. Default is RHINELAND_PALATINATE. */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FederalState state = RHINELAND_PALATINATE;

    /** Roles associated with the person (one-to-many). */
    @OneToMany(mappedBy = "person", orphanRemoval = true)
    private Set<Role> roles = new HashSet<>();


    public Person() {
        super();
    }


    /** -- Getter and Setter -- */
    
    public FederalState getState() {
        return state;
    }

    public void setState(FederalState state) {
        if (state != null) this.state = state;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isConsent() {
        return consent;
    }

    public void setConsent(boolean consent) {
        this.consent = consent;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the roles for this person.
     * Clears the current roles and assigns new roles with bidirectional update.
     *
     * @param roles the new set of roles
     */
    public void setRoles(Set<Role> roles) {
        this.roles.clear();
        if (roles == null) return;
        this.roles.addAll(roles);
        roles.forEach(role -> role.setPerson(this));
    }

    /**
     * Adds a role to this person and updates the role's person reference.
     * Does not remove existing roles.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        if (role == null) return;
        boolean wasAdded = roles.add(role);
        if (wasAdded) role.setPerson(this);
    }

    /**
     * Removes a role from this person's collection.
     *
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        if (role == null) return;
        roles.remove(role);
    }


}


