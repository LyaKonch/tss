package bravo.web;

import bravo.dto.PersonDTO;
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
import java.util.ResourceBundle;
import java.util.Set;

@Named("userDetailsBean")
@ViewScoped
public class UserDetailsBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final ResourceBundle locale = FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");

    @Inject
    private Authorization authorization;

    @Inject
    private UserLogic userLogic;


    private PersonDTO person;    //


    @PostConstruct
    public void init() {
        try {
            person = authorization.getLoggedInPerson();
        } catch (Exception e) {
            addError(locale.getString("error.person.not.found"));
            person = null;
        }
    }

    // Name
    public String getFullName() {
        if (person == null) return "-";
        String fn = person.getFirstName() != null ? person.getFirstName() : "";
        String ln = person.getLastName() != null ? person.getLastName() : "";
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
