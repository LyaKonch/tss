package bravo.web;

import bravo.dto.ContractDTO;
import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
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
 * This bean provides functionality for viewing and searching contracts.
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
            contractList = contractLogic.getAllContracts();
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
                // Simple implementation: filter contracts by name containing the search text
                List<ContractDTO> allContracts = contractLogic.getAllContracts();
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

    /**
     * Creates test data for demonstration purposes.
     */
    public void createTestData() {
        try {
            // Create a few sample contracts
            ContractDTO contract1 = new ContractDTO();
            contract1.setName("Sample Contract 1");
            contract1.setStartDate(java.time.LocalDate.parse("2025-01-01"));
            contract1.setEndDate(java.time.LocalDate.parse("2025-01-31").plusMonths(6));
            contract1.setFrequency(bravo.enums.TimesheetFrequency.WEEKLY);
            contract1.setHoursPerWeek(40.0);
            contract1.setWorkingDaysPerWeek(5);
            contract1.setVacationDaysPerYear(20);
            contract1.setStatus(ContractStatus.STARTED);
            contractLogic.createContract(contract1);

            ContractDTO contract2 = new ContractDTO();
            contract2.setName("Sample Contract 2");
            contract2.setStartDate(java.time.LocalDate.parse("2025-01-01").minusMonths(1));
            contract2.setEndDate(java.time.LocalDate.parse("2025-01-31").plusMonths(12));
            contract2.setFrequency(bravo.enums.TimesheetFrequency.MONTHLY);
            contract2.setHoursPerWeek(35.0);
            contract2.setWorkingDaysPerWeek(5);
            contract2.setVacationDaysPerYear(25);
            contract2.setStatus(ContractStatus.STARTED);
            contractLogic.createContract(contract2);

            ContractDTO contract3 = new ContractDTO();
            contract3.setName("Sample Contract 3");
            contract3.setStartDate(java.time.LocalDate.parse("2025-01-01").minusMonths(3));
            contract3.setEndDate(java.time.LocalDate.parse("2025-01-31").plusMonths(3));
            contract3.setFrequency(TimesheetFrequency.WEEKLY);
            contract3.setHoursPerWeek(20.0);
            contract3.setWorkingDaysPerWeek(3);
            contract3.setVacationDaysPerYear(15);
            contract3.setStatus(ContractStatus.PREPARED);
            contractLogic.createContract(contract3);

            // Reload the contract list
            loadContracts();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Test data created successfully", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating test data: " + e.getMessage(), null));
        }
    }

    // Getters and setters

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
