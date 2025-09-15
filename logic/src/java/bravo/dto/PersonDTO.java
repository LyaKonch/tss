package bravo.dto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PersonDTO extends AbstractDTO {

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String emailAddress;
    private boolean consent;

    private Set<String> roleUuids = new HashSet<>();

    public PersonDTO() {
        super();
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

    public Set<String> getRoleUuids() {
        return roleUuids;
    }

    public void setRoleUuids(Set<String> roleUuids) {
        this.roleUuids = roleUuids;
    }
}
