package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.enums.ContractStatus;
import bravo.logic.ContractLogic;
import bravo.logic.PersonLogic;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Backing bean for contract list view.
 * This bean provides functionality for viewing and searching lists of contracts.
 */
@Named
@ViewScoped
public class ContractListBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ResourceBundle locale = FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    @Inject
    private Authorization auth;

    @Inject
    private ContractDetailsBean contractDetailsBean;

    @Inject
    private ContractEditBean contractEditBean;

    @Inject
    private Authorization authorization;

    private List<ContractDTO> contractList;

    /**
     * Initializes the bean by loading contracts.
     */
    @PostConstruct
    public void init() {
        // TODO: Get user from session once those new beans are merged
        loadContracts();
    }

    /**
     * Loads all contracts from the database relevant to the logged-in user.
     */
    public void loadContracts() {
        try {
            if (auth.isUserInRole("ADMIN")) contractList = contractLogic.findAll();
            if (
                    auth.isUserInRole("USER")
                            || auth.isUserInRole("STAFF")
                            && auth.isUserInRole("ADMIN")) {

                PersonDTO person = personLogic.findByEmail(auth.getUserEmail());
                contractList = contractLogic.findContractsAssociatedWithPerson(person);
            }

        } catch (Exception e) {
            contractList = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractNoMatches"), e.getMessage()));
        }
    }

    public boolean canEdit(ContractDTO contract) {
        return contract.getStatus() == ContractStatus.PREPARED && (authorization.isSupervisor(contract) || authorization.isAssistant(contract));
    }

    public boolean canStart(ContractDTO contract) {
        return contract.getStatus() == ContractStatus.PREPARED && (authorization.isSupervisor(contract) || authorization.isAssistant(contract));
    }

    public void startContract(ContractDTO contract) {
        try {
            contractLogic.startContract(contract);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, locale.getString("contractStarted"), contract.getName()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractStartFailed"), e.getMessage()));
        }
    }

    public boolean canTerminate(ContractDTO contract) {
        return contract.getStatus() == ContractStatus.STARTED && (authorization.isSupervisor(contract) || authorization.isAssistant(contract));
    }

    public void terminateContract(ContractDTO contract) {
        try {
            contractLogic.terminate(contract.getUuid(), LocalDate.now());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, locale.getString("contractTerminated"), contract.getName()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractTerminationFailed"), e.getMessage()));
        }
    }

    public boolean canDelete(ContractDTO contract) {
        return contract.getStatus() == ContractStatus.PREPARED && (authorization.isSupervisor(contract) || authorization.isAssistant(contract));
    }

    public void deleteContract(ContractDTO contract) {
        try {
            contractLogic.deleteContract(contract.getUuid());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, locale.getString("contractDeleted"), contract.getName()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractDeletionFailed"), e.getMessage()));
        }
    }


    public void prepareDetails(ContractDTO contract) {
        contractDetailsBean.init(contract);
    }

    public void prepareEdit(ContractDTO contract) {
        contractEditBean.prepareEdit(contract);
    }

    public void prepareCreate() {
        contractEditBean.prepareCreate();
    }

    public String formatDate(LocalDate date) {
        if (date == null) return "Invalid";
        Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(currentLocale);
        return date.format(formatter);
    }

    public List<ContractDTO> getContractList() {
        if (contractList == null) loadContracts();
        return contractList;
    }

    public void setContractList(List<ContractDTO> contractList) {
        this.contractList = contractList;
    }
}
