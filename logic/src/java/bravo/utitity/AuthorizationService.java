package bravo.utitity;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.dao.UserDAO;
import bravo.dao.UserGroupDAO;
import bravo.entities.Contract;
import bravo.entities.Person;
import bravo.entities.Supervisor;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.Stateless;
import jakarta.ejb.SessionContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.faces.context.FacesContext;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Authorization service for business logic and JSF/EJB usage.
 */
@Stateless
@DeclareRoles({"ADMIN","USER","STAFF"})
public class AuthorizationService {

    private static final Logger LOG = Logger.getLogger(AuthorizationService.class.getName());

    @Resource
    private SessionContext sessionContext; // gives caller principal

    @Inject
    private UserGroupDAO userGroupDAO;

    @Inject
    private ContractDAO contractDAO;

    @Inject
    private PersonDAO personDAO;

    public String getCurrentEmail() {
        Principal p = sessionContext.getCallerPrincipal();
        return p == null ? null : p.getName(); // if principal isn't null it means the user has logined
    }

    public boolean isUserInRole(String role) {
        return sessionContext.isCallerInRole(role);
    }

    public List<String> getGroupsForCurrentUser() {
        String username = getCurrentEmail();
        if (username == null) return List.of();
        return userGroupDAO.findGroupsByEmail(username)
                .stream().map(Enum::name).collect(Collectors.toList());
    }

    public boolean isSupervisorForContract(Long contractId) {
        String username = getCurrentEmail();
        if (username == null) return false;
        Contract c = contractDAO.findById(contractId);
        if (c == null) return false;
        Supervisor sup = c.getSupervisor();
        return sup != null && username.equals(sup.getPerson().getEmailAddress());
    }

    public boolean canManageContract(Long contractId) {
        // admin always can
        if (isUserInRole("ADMIN")) return true;
        // staff + being supervisor for that contract
        if (isUserInRole("STAFF") && isSupervisorForContract(contractId)) return true;
        // additional domain rules can be added here
        return false;
    }



    public void logPrincipalAndRoles() {
        String p = getCurrentEmail();
        LOG.info("Principal (SessionContext): " + p);
        LOG.info("isCallerInRole(ADMIN) = " + sessionContext.isCallerInRole("ADMIN"));
        LOG.info("isCallerInRole(USER)  = " + sessionContext.isCallerInRole("USER"));
        LOG.info("isCallerInRole(STAFF) = " + sessionContext.isCallerInRole("STAFF"));
    }
}
