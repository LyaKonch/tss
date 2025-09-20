package bravo.web;

import bravo.dto.PersonDTO;
import bravo.entities.Person;
import bravo.logic.PersonLogic;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;

import java.util.Optional;
import java.util.logging.Logger;

@Named
@RequestScoped
public class Authorization {



    private static final Logger logger = Logger.getLogger(Authorization.class.getName());

    @Inject
    PersonLogic personLogic;

    private Boolean userHasConsent;


    @PostConstruct
    public void init() {
        userHasConsent = hasUserConsent();
        Logger.getLogger(getClass().getName()).info("init: userHasConsent=" + userHasConsent);
    }

    public String getEmail(){
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
        return req.getUserPrincipal().getName();
    }

    public boolean isUserInRole(String roleName) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
        logger.warning("isUserInRole(): " + req.getUserPrincipal().getName() + ", " + req.isUserInRole(roleName));
        return req.isUserInRole(roleName);
    }

    public boolean getUserHasConsent() { return Boolean.TRUE.equals(userHasConsent); }

    public boolean hasUserConsent(){
        String username = getEmail();
        if (username == null) return false;
        PersonDTO currentPerson = personLogic.findByEmail(username);
        return currentPerson.isConsent();
    }

    public void updateUserConsent(){
        String username = getEmail();
        PersonDTO currentPerson = personLogic.findByEmail(username);
        currentPerson.setConsent(true);
        personLogic.update(currentPerson);
        userHasConsent = true;
    }

}
