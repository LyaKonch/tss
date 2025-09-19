package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Backing bean for contract list view.
 * This bean provides functionality for viewing and searching lists of contracts.
 */
@Named
@ViewScoped
public class ContractListBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Logger logger = Logger.getLogger(ContractListBean.class.getName());

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    @Inject
    private Authorization auth;

    @Inject
    private ContractDetailsBean contractDetailsBean;

    private String searchText;

    private List<ContractDTO> contractList;

    /**
     * Initializes the bean.
     */
    @PostConstruct
    public void init() {
        try {
            // Initialize the contract list
            loadContracts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error initializing bean: " + e.getMessage(), null));
        }
    }

    /**
     * Loads all contracts from the database.
     */
    private void loadContracts() {
        try {
            if(auth.isUserInRole("ADMIN")) {contractList = contractLogic.findAll();}
            if(
                    auth.isUserInRole("USER")
                    || auth.isUserInRole("STAFF")
                    && auth.isUserInRole("ADMIN"))
            {

                PersonDTO person = personLogic.findByEmail(auth.getEmail());
                contractList = contractLogic.findContractsAssociatedWithPerson(person);
            }

        } catch (Exception e) {
            contractList = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error loading contracts: " + e.getMessage(), null));
        }
    }

    /**
     * Searches for contracts based on the search text.
     * If the search text is empty, all contracts are loaded.
     */
    public void search() {
        try {
            if (searchText == null || searchText.trim().isEmpty()) {
                loadContracts();
            } else {
                // Filter contracts by string
                List<ContractDTO> allContracts = contractLogic.findAll();
                contractList = new ArrayList<>();

                for (ContractDTO c : allContracts) {
                    if (c.getName() != null && c.getName().toLowerCase().contains(searchText.toLowerCase())) {
                        contractList.add(c);
                    }
                }
            }
        } catch (Exception e) {
            contractList = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error searching contracts: " + e.getMessage(), null));
        }
    }

    public void prepareDetails(ContractDTO contract) {
        contractDetailsBean.init(contract);
    }

    public String formatDate(LocalDate date) {
        return (date != null ? date.format(formatter) : "Invalid");  // TODO: Use locale and maybe localized date format
    }


    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<ContractDTO> getContractList() {
        if (contractList == null) {
            loadContracts();
        }
        return contractList;
    }

    public void setContractList(List<ContractDTO> contractList) {
        this.contractList = contractList;
    }
}
