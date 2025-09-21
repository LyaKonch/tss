package bravo.utitity;

import bravo.dao.PersonDAO;
import bravo.dao.UserGroupDAO;
import bravo.entities.Contract;
import bravo.entities.Person;
import bravo.entities.Timesheet;
import bravo.enums.GroupName;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.List;

import java.util.Objects;
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

    @Resource
    private SessionContext sessionContext;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private UserGroupDAO userGroupDAO;

    public boolean isAuthorized() {
        // returns null if not authorized
        return sessionContext.getCallerPrincipal() != null;
    }

    /**
     * Retrieves the email address (username) of the currently authenticated user.
     *
     * @return the user's email, or {@code null} if no user is authenticated
     */
    public String getCurrentEmail() {
        Principal principal = sessionContext.getCallerPrincipal();
        return (principal != null) ? principal.getName() : null;
    }

    public Person getCurrentPerson() {
        String email = getCurrentEmail();
        return (email != null) ? personDAO.findByEmailAddress(email).orElse(null) : null;
    }

    public List<GroupName> getCurrentUserGroups() {
        String email = getCurrentEmail();
        if (email == null) {
            return List.of();
        }
        return userGroupDAO.findGroupsByEmail(email);
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
     * Checks if the current user is authorized to administrate a contract (create, edit, update).
     * <p>
     * Authorization is granted to users with the 'ADMIN' role, or to users who are
     * a {@code Supervisor}, {@code Assistant}, or {@code Secretary} specifically for the given contract.
     *
     * @param contract The contract entity to check permissions against.
     * @return {@code true} if the user is authorized, otherwise {@code false}.
     */
    public boolean canAdministrateContract(Contract contract) {

        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSupervisorForContract(currentUser, contract)
                || isAssistantForContract(currentUser, contract)
                || isSecretaryForContract(currentUser, contract);
    }

    /**
     * Checks if the current user is authorized to terminate a contract.
     * <p>
     * Authorization is granted to users with the 'ADMIN' role, or to users who are
     * a {@code Supervisor} or {@code Assistant} for the given contract. Secretaries are excluded from this action.
     *
     * @param contract The contract entity to check permissions against.
     * @return {@code true} if the user is authorized, otherwise {@code false}.
     */
    public boolean canTerminateContract(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSupervisorForContract(currentUser, contract) || isAssistantForContract(currentUser, contract);
    }

    /**
     * Checks if the current user is authorized to archive a contract's timesheets.
     * <p>
     * According to business requirements, this action is restricted to users with the 'ADMIN' role
     * or users who hold the {@code Secretary} role for the specified contract.
     *
     * @param contract The contract whose timesheets are to be archived.
     * @return {@code true} if the user is authorized, otherwise {@code false}.
     */
    public boolean canArchiveContract(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSecretaryForContract(currentUser, contract);
    }
    /**
     * Checks if the current user is authorized to create, update, or delete entries within a timesheet.
     * <p>
     * This is a highly restrictive action. Authorization is granted only to users with the 'ADMIN' role
     * or the specific {@code Employee} to whom the timesheet's contract belongs.
     *
     * @param timesheet The timesheet entity to check permissions against.
     * @return {@code true} if the user is authorized, otherwise {@code false}.
     */
    public boolean canEditTimesheetEntry(Timesheet timesheet) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || timesheet == null) return false;

        return isEmployeeForContract(currentUser, timesheet.getContract());
    }

    /**
     * Checks if the current user is authorized to abort a contract.
     * <p>
     * Authorization is granted to users with the 'ADMIN' role, or to users who are
     * a {@code Supervisor} or {@code Assistant} for the given contract.
     *
     * @param contract The contract entity to be aborted.
     * @return {@code true} if the user is authorized, otherwise {@code false}.
     */
    public boolean canAbortContract(Contract contract) {
        if (isUserInRole("ADMIN")) {
            return true;
        }
        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) {
            return false;
        }
        return isSupervisorForContract(currentUser, contract) || isAssistantForContract(currentUser, contract);
    }

    /**
     * Checks if the current user is authorized to sign a timesheet as the employee.
     * <p>
     * Authorization is granted to users with the 'ADMIN' role or the specific {@code Employee}
     * associated with the contract.
     *
     * @param contract The contract to which the timesheet belongs.
     * @return {@code true} if the user is authorized, otherwise {@code false}.
     */
    public boolean canSignTimesheetAsEmployee(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isEmployeeForContract(currentUser, contract) ;
    }

    /**
     * Checks if the current user is authorized to sign a timesheet as the supervisor.
     * <p>
     * Authorization is granted to users with the 'ADMIN' role or the specific {@code Supervisor}
     * associated with the contract.
     *
     * @param contract The contract to which the timesheet belongs.
     * @return {@code true} if the user is authorized, otherwise {@code false}.
     */
    public boolean canSignTimesheetAsSupervisor(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSupervisorForContract(currentUser, contract);
    }

    /**
     * Verifies if the given Person is the designated Employee for the specified Contract.
     *
     * @param person   The Person entity to check.
     * @param contract The Contract entity to check against.
     * @return {@code true} if the person is the employee for the contract, otherwise {@code false}.
     */
    private boolean isEmployeeForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getEmployee() == null) return false;
        return Objects.equals(person.getUuid(), contract.getEmployee().getPerson().getUuid());
    }

    /**
     * Verifies if the given Person is the designated Supervisor for the specified Contract.
     *
     * @param person   The Person entity to check.
     * @param contract The Contract entity to check against.
     * @return {@code true} if the person is the supervisor for the contract, otherwise {@code false}.
     */
    private boolean isSupervisorForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getSupervisor() == null) return false;
        return Objects.equals(person.getUuid(), contract.getSupervisor().getPerson().getUuid());
    }

    /**
     * Verifies if the given Person is one of the designated Assistants for the specified Contract.
     *
     * @param person   The Person entity to check.
     * @param contract The Contract entity to check against.
     * @return {@code true} if the person is an assistant for the contract, otherwise {@code false}.
     */
    private boolean isAssistantForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getAssistants() == null) return false;
        return contract.getAssistants().stream()
                .anyMatch(assistant -> Objects.equals(person.getUuid(),assistant.getPerson().getUuid()));
    }

    /**
     * Verifies if the given Person is one of the designated Secretaries for the specified Contract.
     *
     * @param person   The Person entity to check.
     * @param contract The Contract entity to check against.
     * @return {@code true} if the person is a secretary for the contract, otherwise {@code false}.
     */
    private boolean isSecretaryForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getSecretaries() == null) return false;
        return contract.getSecretaries().stream()
                .anyMatch(secretary -> Objects.equals(person.getUuid(),secretary.getPerson().getUuid()));
    }
}