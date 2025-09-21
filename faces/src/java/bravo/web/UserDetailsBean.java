package bravo.web;

import bravo.dto.PersonDTO;
import bravo.dto.UserDTO;
import bravo.logic.PersonLogic;
import bravo.logic.UserLogic;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.Logger;

@Named("userDetailsBean")
@ViewScoped
public class UserDetailsBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(UserDetailsBean.class.getName());

    @Inject
    private PersonLogic personLogic;
    @Inject
    private UserLogic userLogic;
    
    
    private PersonDTO person;    //
   

    @PostConstruct
    public void init() {
        try {
            String email = FacesContext.getCurrentInstance()
                                       .getExternalContext()
                                       .getUserPrincipal()
                                       .getName();
            UserDTO user = userLogic.findByEmail(email);
            //person = personLogic.findByEmail(email);
            if (user == null) { //or person
                addError("Keine Person zu E-Mail gefunden: " + email);
            }
        } catch (Exception e) {
            addError("Fehler beim Laden: " + e.getMessage());
            person = null;
        }
    }

    // Name
    public String getFullName() {
        if (person == null) return "-";
        String fn = person.getFirstName() != null ? person.getFirstName() : "";
        String ln = person.getLastName()  != null ? person.getLastName()  : "";
        String out = (fn + " " + ln).trim();
        return out.isEmpty() ? "-" : out;
    }
    //Birth
    public String getBirthDate() {
        if (person == null || person.getDateOfBirth() == null) return "-";
        try {
            return person.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return person.getDateOfBirth().toString();
        }
    }

    // Rolle
    public String getRole() {
        if (person == null) return "-";
        Set<String> roles = person.getRoleUuids();
        if (roles == null || roles.isEmpty()) return "Keine Rolle";
        return roles.iterator().next(); 
    }

    private void addError(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, m, ""));
    }

    public PersonDTO getPerson() {
        return person;
    }
}
