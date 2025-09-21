package bravo.utitity;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.dao.UserGroupDAO;
import bravo.entities.Contract;
import bravo.entities.Supervisor;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.Stateless;
import jakarta.ejb.SessionContext;
import jakarta.inject.Inject;

import java.security.Principal;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides authorization-related operations for business logic and JSF/EJB usage.
 * <p>
 * Wraps container-managed security (e.g., {@link SessionContext}) with application-specific
 * rules such as role checks and contract-based permissions.
 */
@Stateless
@DeclareRoles({"ADMIN", "USER", "STAFF"})
public class AuthorizationService {

    private static final Logger LOG = Logger.getLogger(AuthorizationService.class.getName());

    @Resource
    private SessionContext sessionContext;

    @Inject
    private UserGroupDAO userGroupDAO;

    @Inject
    private ContractDAO contractDAO;

    @Inject
    private PersonDAO personDAO;

    /**
     * Retrieves the email address (username) of the currently authenticated user.
     *
     * @return the user's email, or {@code null} if no user is authenticated
     */
    public String getCurrentEmail() {
        Principal p = sessionContext.getCallerPrincipal();
        return p == null ? null : p.getName();
    }

    /**
     * Checks if the current user has the specified role.
     *
     * @param role the role to check (e.g., "ADMIN", "USER", "STAFF")
     * @return {@code true} if the user has the role, {@code false} otherwise
     */
    public boolean isUserInRole(String role) {
        return sessionContext.isCallerInRole(role);
    }

    /**
     * Retrieves all group names assigned to the currently authenticated user.
     *
     * @return a list of group names, or an empty list if the user is not authenticated
     */
    public List<String> getGroupsForCurrentUser() {
        String username = getCurrentEmail();
        if (username == null) return List.of();
        return userGroupDAO.findGroupsByEmail(username)
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the current user is the supervisor of the given contract.
     *
     * @param contractId the ID of the contract
     * @return {@code true} if the user is the contract's supervisor, {@code false} otherwise
     */
    public boolean isSupervisorForContract(Long contractId) {
        String username = getCurrentEmail();
        if (username == null) return false;
        Contract c = contractDAO.findById(contractId);
        if (c == null) return false;
        Supervisor sup = c.getSupervisor();
        return sup != null && username.equals(sup.getPerson().getEmailAddress());
    }

    /**
     * Determines whether the current user can manage a given contract.
     * <p>
     * Rules:
     * <ul>
     *   <li>Admins can always manage contracts.</li>
     *   <li>Staff can manage contracts they supervise.</li>
     *   <li>Other roles cannot manage contracts by default.</li>
     * </ul>
     *
     * @param contractId the ID of the contract
     * @return {@code true} if the user can manage the contract, {@code false} otherwise
     */
    public boolean canManageContract(Long contractId) {
        if (isUserInRole("ADMIN")) return true;
        if (isUserInRole("STAFF") && isSupervisorForContract(contractId)) return true;
        return false;
    }

    /**
     * Logs the current principal and their roles (ADMIN, USER, STAFF)
     * for debugging and auditing purposes.
     */
    public void logPrincipalAndRoles() {
        String p = getCurrentEmail();
        LOG.info("Principal (SessionContext): " + p);
        LOG.info("isCallerInRole(ADMIN) = " + sessionContext.isCallerInRole("ADMIN"));
        LOG.info("isCallerInRole(USER)  = " + sessionContext.isCallerInRole("USER"));
        LOG.info("isCallerInRole(STAFF) = " + sessionContext.isCallerInRole("STAFF"));
    }
}