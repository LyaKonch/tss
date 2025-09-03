package bravo.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "Person")
@NamedQueries({
    @NamedQuery(name = "Person.findByUuid",
                query = "SELECT e FROM Person e WHERE e.uuid = :uuid"),
    @NamedQuery(name = "Timesheet.findAll",
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

    @Email
    @NotBlank
    private String emailAddress;  

    private LocalDate dateOfBirth; 
    private boolean consent;  
    
   @OneToMany(mappedBy = "person", orphanRemoval = false)
    private Set<Role> roles = new HashSet<>();

    //Getter&Setter
    public Person() {
        super();
    }
    public Person(boolean NewEntity) {
        super(NewEntity);
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

    public String getEmailAddress() { 
        return emailAddress; 
    }
    public void setEmailAddress(String emailAddress) { 
        this.emailAddress = emailAddress; 
    }

    public LocalDate getDateOfBirth() { 
        return dateOfBirth; 
    }
    public void setDateOfBirth(LocalDate dateOfBirth) { 
        this.dateOfBirth = dateOfBirth; 
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
        this.roles = roles;
    }



}


