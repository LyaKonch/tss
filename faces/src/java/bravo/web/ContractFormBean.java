package bravo.web;

import bravo.dto.ContractDTO;
import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import bravo.logic.ContractLogic;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// TODO: Restructure to follow layers properly

/**
 * Backing bean for contract edit view.
 * This bean provides functionality for creating and editing contracts.
 */
@Named
@ViewScoped
public class ContractFormBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

    private ContractDTO contract;

    private List<ContractStatus> statuses;

    private List<TimesheetFrequency> frequencies;

    /**
     * Initializes the bean.
     */
    @PostConstruct
    public void init() {
        try {
            // Initialize the statuses and frequencies lists
            statuses = Arrays.asList(ContractStatus.values());
            frequencies = Arrays.asList(TimesheetFrequency.values());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error initializing bean: " + e.getMessage(), null));
        }
    }

    /**
     * Prepares a new contract for creation.
     *
     * @return the navigation outcome
     */
    public String prepareCreate() {
        contract = new ContractDTO();
        contract.setStatus(ContractStatus.PREPARED);
        contract.setStartDate(LocalDate.now());
        return "contractForm?faces-redirect=true";
    }

    /**
     * Loads a contract for editing.
     *
     * @param uuid the UUID of the contract to edit
     * @return the navigation outcome
     */
    public String prepareEdit(String uuid) {
        if (uuid == null || uuid.isEmpty() || "new".equals(uuid)) {
            return prepareCreate();
        }

        contract = contractLogic.getContractByUuid(uuid);
        if (contract == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Contract not found", null));
            return "contractView?faces-redirect=true";
        }

        return null; // Stay on the same page since we're already on the edit page
    }

    /**
     * Saves the current contract.
     *
     * @return the navigation outcome
     */
    public String save() {
        try {
            if (contract.isNew()) {
                contractLogic.createContract(contract);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Contract created successfully", null));
            } else {
                contractLogic.updateContract(contract);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Contract updated successfully", null));
            }

            // Store the message in flash scope so it's available after the redirect
            Flash flash = FacesContext.getCurrentInstance().getExternalContext().getFlash();
            flash.setKeepMessages(true);

            return "contractView?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error saving contract: " + e.getMessage(), null));
            return null;
        }
    }

    // Getters and setters

    public ContractDTO getContract() {
        if (contract == null) {
            contract = new ContractDTO();
        }
        return contract;
    }

    public void setContract(ContractDTO contract) {
        this.contract = contract;
    }

    public List<ContractStatus> getStatuses() {
        if (statuses == null) {
            statuses = Arrays.asList(ContractStatus.values());
        }
        return statuses;
    }

    public List<TimesheetFrequency> getFrequencies() {
        if (frequencies == null) {
            frequencies = Arrays.asList(TimesheetFrequency.values());
        }
        return frequencies;
    }
}
