package bravo.web;

import bravo.dto.UserDTO;
import bravo.entities.Person;
import bravo.entities.User;
import bravo.logic.UserLogic;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;


@SessionScoped
@Named
public class LoginBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(LoginBean.class.getName());
    
    private UserDTO actUser;
    @EJB
    private UserLogic ul;
    
    private Principal oldPrincipal = null;
    public boolean isLoggedIn() {
        return getUserDTO() != null;
    }
    public UserDTO getUserDTO(){
    Principal p = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();
        if (p == null) {
            actUser = null;
        } else {
            if (oldPrincipal == null || !p.getName().equals(oldPrincipal.getName())) {
                LOG.log(Level.INFO, "TSS: LOGIN user {0}", p.getName());
                actUser = ul.getCurrentUserDTO();
            }
        }
        oldPrincipal = p;
        return actUser;
    }
    public void invalidateSession() {
        LOG.log(Level.INFO, "invalidateSession()");
        Principal p = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();
        if (p != null) {
            LOG.log(Level.INFO, "Contacts: LOGOUT user {0}", p.getName());
        }
        actUser = null;
        oldPrincipal = null;
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .invalidateSession();
    }

    }
