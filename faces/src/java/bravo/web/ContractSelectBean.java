package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.enums.RoleType;
import bravo.logic.PersonLogic;
import jakarta.enterprise.event.Event;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

@Named
@ViewScoped
public class ContractSelectBean implements Serializable {

    private static final Logger logger = Logger.getLogger(ContractSelectBean.class.getName());

    private final ResourceBundle locale = FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");

    @Inject
    private PersonLogic personLogic;

    @Inject
    private Authorization authorization;

    @Inject
    private Event<ContractDTO> contractSelectedEvent;

    private ContractDTO selectedContract;

    private List<ContractDTO> contracts = new ArrayList<>();

    private List<RoleType> roles = new ArrayList<>();


    public void init(List<ContractDTO> contracts) {
        if (contracts == null) contracts = new ArrayList<>();
        this.contracts.clear();
        this.contracts = contracts;
        logger.info("ContractSelectBean.init: Got " + contracts.size() + " contracts.");

        if (contracts.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractNotAvailable"), null));
            logger.info("ContractSelectBean.init: No contracts found.");
            return;
        }

        selectedContract = contracts.getFirst();
        onContractSelect();
    }

    public void onContractSelect() {
        logger.info("ContractSelectBean.onSelect: Selected Contract\n" + selectedContract);
        contractSelectedEvent.fire(selectedContract);

        roles.clear();
        PersonDTO user = authorization.getLoggedInPerson();
        List<PersonDTO> secretaries = new ArrayList<>();
        List<PersonDTO> assistants = new ArrayList<>();

        // Load associated persons
        PersonDTO employee = personLogic.findByUuid(selectedContract.getEmployeeUuid());
        PersonDTO supervisor = personLogic.findByUuid(selectedContract.getSupervisorUuid());
        for (String uuid : selectedContract.getSecretaryUuids()) secretaries.add(personLogic.findByUuid(uuid));
        for (String uuid : selectedContract.getAssistantUuids()) assistants.add(personLogic.findByUuid(uuid));

        // Add all roles of the logged-in user to the list for the "Your role" display
        if (user.equals(employee)) roles.add(RoleType.Employee);
        if (user.equals(supervisor)) roles.add(RoleType.Supervisor);
        if (secretaries.contains(user)) roles.add(RoleType.Secretary);
        if (assistants.contains(user)) roles.add(RoleType.Assistant);
    }


    public RoleType mainRole(ContractDTO contract) {
        if (authorization.isEmployee(contract)) return RoleType.Employee;
        if (authorization.isSupervisor(contract)) return RoleType.Supervisor;
        if (authorization.isSecretary(contract)) return RoleType.Secretary;
        if (authorization.isAssistant(contract)) return RoleType.Assistant;
        return null;
    }

    public ContractDTO getSelectedContract() {
        return selectedContract;
    }

    public void setSelectedContract(ContractDTO selectedContract) {
        this.selectedContract = selectedContract;
    }

    public List<ContractDTO> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractDTO> contracts) {
        if (contracts == null) return;
        this.contracts = contracts;
    }

    public List<RoleType> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleType> roles) {
        this.roles = roles;
    }

}
