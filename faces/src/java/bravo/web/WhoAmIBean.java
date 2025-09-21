package bravo.web;

import bravo.utitity.AuthorizationService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.security.enterprise.SecurityContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Named
@RequestScoped
public class WhoAmIBean {

    private static final Logger logger = Logger.getLogger(WhoAmIBean.class.getName());

    @Inject
    private AuthorizationService auth;


    private List<String> groups;
    private String username;

    public void load() {
        logger.info("Invocation load() in WhoAmIBean.");
        username = auth.getCurrentEmail();
        groups = new ArrayList<>();
        if(auth.isUserInRole("ADMIN")) groups.add("ADMIN");
        if(auth.isUserInRole("USER")) groups.add("USER");
        if(auth.isUserInRole("STAFF")) groups.add("STAFF");
        if (groups != null) {
            logger.info("User Groups: " + groups);
        } else {
            logger.warning("Groups returned null.");
            groups = Collections.emptyList();
        }
    }

    // getters
    public List<String> getGroups() { return groups; }
    public String getUsername() { return username; }
}
