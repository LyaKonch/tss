package bravo.dto;

import bravo.enums.FederalState;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object (DTO) for {@link bravo.entities.Person} information.
 * <p>
 * Extends {@link AbstractDTO} to inherit common DTO properties such as {@code id}, {@code uuid}, and {@code version}.
 * Contains personal details, consent information, state, and associated role UUIDs.
 */
public class PersonDTO extends AbstractDTO {

    /** First name of the person. */
    private String firstName;

    /** Last name of the person. */
    private String lastName;

    /** Date of birth of the person. */
    private LocalDate dateOfBirth;

    /** Email address of the person. */
    private String emailAddress;

    /** Federal state of residence. */
    private FederalState state;

    /** Indicates whether the person has given consent. */
    private boolean consent;

    /** UUIDs of roles associated with this person. */
    private Set<String> roleUuids = new HashSet<>();

    /** Default constructor. */
    public PersonDTO() {
        super();
    }

    /**
     * Constructor with full initialization.
     *
     * @param uuid          UUID of the person
     * @param jpaVersion    JPA version number
     * @param firstName     first name
     * @param lastName      last name
     * @param emailAddress  email address
     * @param dateOfBirth   date of birth
     * @param consent       consent flag
     * @param roleUuids     set of associated role UUIDs
     */
    public PersonDTO(String uuid, int jpaVersion, String firstName, String lastName, String emailAddress, LocalDate dateOfBirth, boolean consent, Set<String> roleUuids) {
        super(uuid, jpaVersion);
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.dateOfBirth = dateOfBirth;
        this.consent = consent;
        this.roleUuids = roleUuids;
    }

    @Override
    public String toString() {
        return "PersonDTO{" + "firstName=" + firstName + ", lastName=" + lastName + ", dateOfBirth=" + dateOfBirth + ", emailAddress=" + emailAddress + ", consent=" + consent + ", roleUuids=" + roleUuids + '}';
    }

     // --- Getters and Setters ---
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

    public Set<String> getRoleUuids() {
        return roleUuids;
    }

    public void setRoleUuids(Set<String> roleUuids) {
        this.roleUuids.clear();
        if (roleUuids == null) return;
        this.roleUuids.addAll(roleUuids);
    }

    public FederalState getState() {
        return state;
    }

    public void setState(FederalState state) {
        this.state = state;
    }
}
