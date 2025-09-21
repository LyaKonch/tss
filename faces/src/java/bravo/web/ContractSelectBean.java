package bravo.web;

import bravo.dto.ContractDTO;
import bravo.enums.RoleType;
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
    private EntryBlockBean entryBlockBean;

    @Inject
    private Authorization authorization;

    @Inject
    private Event<ContractDTO> contractSelectedEvent;

    private ContractDTO selectedContract;

    private List<ContractDTO> contracts = new ArrayList<>();

    public void init(List<ContractDTO> contracts) {
        logger.info("ContractSelectBean.init: Start...");
        if (contracts == null) contracts = new ArrayList<>();
        this.contracts = contracts;
        logger.info("ContractSelectBean.init: Got " + contracts.size() + " contracts.");

        if (contracts.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractNotFound"), null));
            logger.info("ContractSelectBean.init: No contracts found.");
            return;
        }

        selectedContract = contracts.getFirst();
        onContractSelect();
    }

    public void onContractSelect() {
        logger.info("ContractSelectBean.onSelect: Selected Contract\n" + selectedContract);
        contractSelectedEvent.fire(selectedContract);
    }


    public RoleType mainRole(ContractDTO contract) {
        if (authorization.isEmployee(contract)) return RoleType.Employee;
        if (authorization.isSupervisor(contract)) return RoleType.Supervisor;
        if (authorization.isSecretary(contract)) return RoleType.Secretary;
        if (authorization.isAssistant(contract)) return RoleType.Assistant;
        return null;
    }

    public void prepareTimesheetEntries() {
        entryBlockBean.init(selectedContract);
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

}
