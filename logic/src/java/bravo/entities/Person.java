package bravo.entities;

import bravo.enums.FederalState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static bravo.enums.FederalState.RHINELAND_PALATINATE;

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

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = false)
    private String emailAddress;

    private boolean consent;

    // state should be chosen by user.
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FederalState state = RHINELAND_PALATINATE;

    // Person 1 < --- > 0..* Role
    @OneToMany(mappedBy = "person", orphanRemoval = true)
    private Set<Role> roles = new HashSet<>();


    public Person() {
        super();
    }


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

    public void setRoles(Set<Role> roles) {
        this.roles.clear();
        if (roles == null) return;
        this.roles.addAll(roles);
        roles.forEach(role -> role.setPerson(this));
    }

    /**
     * Bidirectional update for adding a new role.
     * Updates the new role's person reference.
     * Doesn't remove the old role from this person.
     */
    public void addRole(Role role) {
        if (role == null) return;
        boolean wasAdded = roles.add(role);
        if (wasAdded) role.setPerson(this);
    }

    /**
     * Remove the role from the person's collection.
     */
    public void removeRole(Role role) {
        if (role == null) return;
        roles.remove(role);
    }


}


