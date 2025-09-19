package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.enums.RoleType;
import bravo.logic.PersonLogic;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Backing bean for contract edit view.
 * This bean provides functionality for creating and editing contracts.
 */
@Named
@ViewScoped
public class ContractDetailsBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ContractDetailsBean.class.getName());

    // Move to upcoming user bean or sth? since it could be a user setting and then its not duplicated with listBean
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @EJB
    private PersonLogic personLogic;

    private ContractDTO contract;

    // The logged-in user might have more than one role in a contract ;)
    private List<RoleType> roles = new ArrayList<>();

    private PersonDTO employee;

    private PersonDTO supervisor;

    private List<PersonDTO> secretaries = new ArrayList<>();

    private List<PersonDTO> assistants = new ArrayList<>();

    public void init(ContractDTO contract) {
        resetBean();

        // Find the logged-in user's Person
        String username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
        PersonDTO user = personLogic.findByEmail(username);
        logger.info("User: " + user.getFirstName());

        // Load associated persons
        employee = personLogic.findByUuid(contract.getEmployeeUuid());
        supervisor = personLogic.findByUuid(contract.getSupervisorUuid());
        for (String uuid : contract.getSecretaryUuids()) secretaries.add(personLogic.findByUuid(uuid));
        for (String uuid : contract.getAssistantUuids()) assistants.add(personLogic.findByUuid(uuid));

        // Add all roles of the logged-in user to the list for the "Your role" display
        if (user.equals(employee)) roles.add(RoleType.Employee);
        if (user.equals(supervisor)) roles.add(RoleType.Supervisor);
        if (secretaries.contains(user)) roles.add(RoleType.Secretary);
        if (assistants.contains(user)) roles.add(RoleType.Assistant);

        this.contract = contract;
    }

    /**
     * Resets the bean to its initial state.
     */
    private void resetBean() {
        contract = null;
        roles.clear();
        employee = null;
        supervisor = null;
        secretaries.clear();
        assistants.clear();
    }

    public String getFullName(PersonDTO person) {
        if (person == null) return "";
        return (person.getFirstName() + " " + person.getLastName()).trim();
    }

    public String formatDate(LocalDate date) {
        return (date != null ? date.format(formatter) : "Invalid");  // TODO: Use locale and maybe localized date format
    }

    public ContractDTO getContract() {
        return contract;
    }

    public void setContract(ContractDTO contract) {
        this.contract = contract;
    }

    public List<RoleType> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleType> roles) {
        this.roles = roles;
    }

    public PersonDTO getEmployee() {
        return employee;
    }

    public void setEmployee(PersonDTO employee) {
        this.employee = employee;
    }

    public PersonDTO getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(PersonDTO supervisor) {
        this.supervisor = supervisor;
    }

    public List<PersonDTO> getSecretaries() {
        return secretaries;
    }

    public void setSecretaries(List<PersonDTO> secretaries) {
        this.secretaries = secretaries;
    }

    public List<PersonDTO> getAssistants() {
        return assistants;
    }

    public void setAssistants(List<PersonDTO> assistants) {
        this.assistants = assistants;
    }


}
