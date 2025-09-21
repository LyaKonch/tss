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

    public boolean isUserInRole(String role) {
        return sessionContext.isCallerInRole(role);
    }

    public boolean canAdministrateContract(Contract contract) {

        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSupervisorForContract(currentUser, contract)
                || isAssistantForContract(currentUser, contract)
                || isSecretaryForContract(currentUser, contract);
    }


    public boolean canTerminateContract(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSupervisorForContract(currentUser, contract) || isAssistantForContract(currentUser, contract);
    }

    public boolean canArchiveContract(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSecretaryForContract(currentUser, contract);
    }

    public boolean canEditTimesheetEntry(Timesheet timesheet) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || timesheet == null) return false;

        return isEmployeeForContract(currentUser, timesheet.getContract());
    }

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

    public boolean canSignTimesheetAsEmployee(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isEmployeeForContract(currentUser, contract) ;
    }


    public boolean canSignTimesheetAsSupervisor(Contract contract) {
        if (isUserInRole("ADMIN")) return true;

        Person currentUser = getCurrentPerson();
        if (currentUser == null || contract == null) return false;

        return isSupervisorForContract(currentUser, contract);
    }


    private boolean isEmployeeForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getEmployee() == null) return false;
        return Objects.equals(person.getUuid(), contract.getEmployee().getPerson().getUuid());
    }

    private boolean isSupervisorForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getSupervisor() == null) return false;
        return Objects.equals(person.getUuid(), contract.getSupervisor().getPerson().getUuid());
    }

    private boolean isAssistantForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getAssistants() == null) return false;
        return contract.getAssistants().stream()
                .anyMatch(assistant -> Objects.equals(person.getUuid(),assistant.getPerson().getUuid()));
    }

    private boolean isSecretaryForContract(Person person, Contract contract) {
        if (person == null || contract == null || contract.getSecretaries() == null) return false;
        return contract.getSecretaries().stream()
                .anyMatch(secretary -> Objects.equals(person.getUuid(),secretary.getPerson().getUuid()));
    }
}