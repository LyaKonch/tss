package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.dto.TimesheetDTO;
import bravo.dto.TimesheetEntryDTO;
import bravo.enums.ContractStatus;
import bravo.logic.ContractLogic;
import bravo.logic.PersonLogic;
import bravo.logic.TimesheetEntryLogic;
import bravo.logic.TimesheetLogic;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

@Named
@ViewScoped
public class DashboardBean implements Serializable {

    private static final Logger logger = Logger.getLogger(DashboardBean.class.getName());

    @Inject
    private ContractLogic contractLogic;

    @Inject
    private TimesheetLogic timesheetLogic;

    @Inject
    private TimesheetEntryLogic timesheetEntryLogic;

    @Inject
    private PersonLogic personLogic;

    @Inject
    private Authorization authorization;

    @Inject
    private ContractSelectBean contractSelectBean;

    @Inject
    private EntryBlockBean entryBlockBean;

    private List<ContractDTO> userContracts = new ArrayList<>();

    private ContractDTO selectedContract;

    private TimesheetDTO currentTimesheet;

    private TimesheetEntryDTO newEntry = new TimesheetEntryDTO();

    private boolean firstPrepareCall = true;


    @PostConstruct
    public void init() {
        PersonDTO person = authorization.getLoggedInPerson();
        userContracts = contractLogic.findContractsAssociatedWithPerson(person);
        if (userContracts == null) userContracts = new ArrayList<>();
        logger.info("DashboardBean.init: Found " + userContracts.size() + " contracts for this user.");
    }

    public void prepareContractSelect() {
        if (firstPrepareCall) contractSelectBean.init(getEntryContracts());
        firstPrepareCall = false;
    }

    /**
     * Returns a list of contracts that are currently started and have the logged-in user as the employee.
     */
    public List<ContractDTO> getEntryContracts() {
        if (userContracts == null) return new ArrayList<>();
        logger.info("DashboardBean.filter: Enter with " + userContracts.size() + " contracts.");
        List<ContractDTO> filteredContracts = userContracts.stream().filter(contract -> authorization.isEmployee(contract)).toList();
        logger.info("DashboardBean.filter: Found " + filteredContracts.size() + " employee contracts.");
        filteredContracts = filteredContracts.stream().filter(contract -> contract.getStatus() == ContractStatus.STARTED).toList();
        logger.info("DashboardBean.filter: Found " + filteredContracts.size() + " to add entries to.");
        return filteredContracts;
    }

    public void onContractSelect(@Observes ContractDTO selected) {
        this.selectedContract = selected;
//        logger.info("DashboardBean.onSelect: Chosen contract:\n" + selectedContract);
        entryBlockBean.init(this.selectedContract);
    }

    public void signAsEmployee() {
        if (currentTimesheet == null) {
            addError("No timesheet selected.");
            return;
        }
        try {
            timesheetLogic.signByEmployee(currentTimesheet, LocalDate.now());
            currentTimesheet = timesheetLogic.getCurrent(selectedContract);
            addInfo("Timesheet successfully signed by employee.");
        } catch (Exception e) {
            addError("Error signing timesheet: " + e.getMessage());
        }
    }

    public void signAsSupervisor() {
        if (currentTimesheet == null) {
            addError("No timesheet selected.");
            return;
        }
        try {
            timesheetLogic.signBySupervisor(currentTimesheet, LocalDate.now());
            currentTimesheet = timesheetLogic.getCurrent(selectedContract);
            addInfo("Timesheet successfully signed by supervisor.");
        } catch (Exception e) {
            addError("Error signing timesheet: " + e.getMessage());
        }
    }

    public List<ContractDTO> getUserContracts() {
        return userContracts == null ? java.util.List.of() : userContracts;
    }

    public ContractDTO getSelectedContract() {
        return selectedContract;
    }

    public TimesheetDTO getCurrentTimesheet() {
        return currentTimesheet;
    }

    public TimesheetEntryDTO getNewEntry() {
        return newEntry;
    }

    public void setNewEntry(TimesheetEntryDTO newEntry) {
        this.newEntry = newEntry;
    }


    private void addInfo(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, txt, ""));
    }

    private void addError(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, txt, ""));
    }

    public String roleLabel(ContractDTO contract) {
        if (contract == null) return "N/A";

        HashSet<String> roles = new HashSet<>();
        if (authorization.isEmployee(contract)) roles.add("Employee");
        if (authorization.isSupervisor(contract)) roles.add("Supervisor");
        if (authorization.isAssistant(contract)) roles.add("Assistant");
        if (authorization.isSecretary(contract)) roles.add("Secretary");

        logger.info(() -> "roleLabel for contract=" + contract.getName() + " → roles=" + roles);

        return roles.isEmpty() ? "N/A" : String.join(", ", roles);
    }
}
