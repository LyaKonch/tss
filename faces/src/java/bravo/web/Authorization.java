package bravo.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;

import java.util.logging.Logger;

@Named
@RequestScoped
public class Authorization {

    private static final Logger logger = Logger.getLogger(Authorization.class.getName());

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

}
