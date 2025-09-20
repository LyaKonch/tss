package bravo.web;

import bravo.dto.ContractDTO;
import bravo.logic.ContractLogic;
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

    @Inject
    private ContractDetailsBean contractDetailsBean;

    @Inject
    private ContractEditBean contractEditBean;

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
    private void loadContracts() {
        try {
            contractList = contractLogic.findAll();
        } catch (Exception e) {
            contractList = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractNoMatches"), e.getMessage()));
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
