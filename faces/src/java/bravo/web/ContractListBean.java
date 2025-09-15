package bravo.web;

import bravo.dto.ContractDTO;
import bravo.logic.ContractLogic;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for contract list view.
 * This bean provides functionality for viewing and searching lists of contracts.
 */
@Named
@ViewScoped
public class ContractListBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

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
            contractList = contractLogic.findAll();
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
