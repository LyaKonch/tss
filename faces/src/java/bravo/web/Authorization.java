/**
 * Authorization serves as a logged-in user hub.
 * It provides functionality for checking various attributes of the logged-in user.
 * DO NOT use this class on the login page or other pages where the user is not yet logged in.
 */
package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.logic.PersonLogic;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

@Named
@RequestScoped
public class Authorization {

    private static final Logger logger = Logger.getLogger(Authorization.class.getName());

    @Inject
    PersonLogic personLogic;

    private PersonDTO loggedInPerson;

    private Boolean userHasConsent;

    private String currentPersonUuid;

    private String userEmail;

    private List<String> currentUserRoles;

    @PostConstruct
    public void init() {
        userHasConsent = hasUserConsent();
        userEmail = getEmail();
        if (userEmail == null) return;

        loggedInPerson = personLogic.findByEmail(userEmail);
        currentUserRoles = personLogic.findPersonRoles(loggedInPerson);
        currentPersonUuid = loggedInPerson != null ? loggedInPerson.getUuid() : null;

        logger.info("Authorization.init: userEmail=" + userEmail + " has " + currentUserRoles.size() + " roles.\nloggedInPerson=" + loggedInPerson);

    }

    public PersonDTO getLoggedInPerson() {
        return loggedInPerson;
    }

    public String getUserEmail() {
        return userEmail;
    }

    private String getEmail() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
        return req.getUserPrincipal().getName();
    }

    public boolean isUserInRole(String roleName) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return false;
        HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
        if (req == null || req.getUserPrincipal() == null) return false;
        boolean val = req.isUserInRole(roleName);
        logger.fine("isUserInRole(): " + req.getUserPrincipal().getName() + ", " + roleName + " -> " + val);
        return val;
    }

    public boolean getUserHasConsent() {
        return Boolean.TRUE.equals(userHasConsent);
    }

    public boolean hasUserConsent() {
        String username = getEmail();
        if (username == null) return false;
        PersonDTO currentPerson = personLogic.findByEmail(username);
        return currentPerson != null && currentPerson.isConsent();
    }

    public void updateUserConsent() {
        String username = getEmail();
        if (username == null) return;
        PersonDTO currentPerson = personLogic.findByEmail(username);
        if (currentPerson == null) return;
        currentPerson.setConsent(true);
        personLogic.update(currentPerson);
        userHasConsent = true;
    }

    public boolean isEmployee(ContractDTO contract) {
        if (contract == null || contract.getEmployeeUuid() == null || currentUserRoles.isEmpty()) {
            return false;
        }
        String employeePersonUuid = contract.getEmployeeUuid();
        return Objects.equals(employeePersonUuid, currentPersonUuid);
    }

    public boolean isSupervisor(ContractDTO contract) {
        if (contract == null || contract.getSupervisorUuid() == null || currentUserRoles.isEmpty()) {
            return false;
        }
        String supervisorRoleUuid = contract.getSupervisorUuid();
        return Objects.equals(supervisorRoleUuid, currentPersonUuid);
    }

    public boolean isAssistant(ContractDTO contract) {
        if (contract == null || contract.getAssistantUuids() == null || contract.getAssistantUuids().isEmpty() || currentUserRoles.isEmpty()) {
            return false;
        }
        Set<String> assistantRoleUuids = new HashSet<>(contract.getAssistantUuids());

        return assistantRoleUuids.contains(currentPersonUuid);
    }

    public boolean isSecretary(ContractDTO contract) {
        if (contract == null || contract.getSecretaryUuids() == null || contract.getSecretaryUuids().isEmpty() || currentUserRoles.isEmpty()) {
            return false;
        }
        Set<String> secretaryRoleUuids = new HashSet<>(contract.getSecretaryUuids());

        return secretaryRoleUuids.contains(currentPersonUuid);
    }
}
