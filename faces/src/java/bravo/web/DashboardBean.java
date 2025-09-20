package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.dto.TimesheetDTO;
import bravo.dto.TimesheetEntryDTO;
import bravo.enums.ContractStatus;
import bravo.enums.ReportType;
import bravo.enums.TimesheetStatus;
import bravo.logic.ContractLogic;
import bravo.logic.PersonLogic;
import bravo.logic.TimesheetEntryLogic;
import bravo.logic.TimesheetLogic;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.stream.Collectors;

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

    private List<ContractDTO> userContracts = new ArrayList<>();
    ;
    private String selectedContractUuid;
    private ContractDTO selectedContract;
    private TimesheetDTO currentTimesheet;

    private TimesheetEntryDTO newEntry = new TimesheetEntryDTO();

    @PostConstruct
    public void init() {
        String userEmail = authorization.getUserEmail();
        logger.info(() -> "DashboardBean.init: userEmail=" + userEmail);

        if (userEmail != null) {
            PersonDTO person = personLogic.findByEmail(userEmail);
            logger.info(() -> "DashboardBean.init: person=" + person);
            userContracts = contractLogic.findContractsAssociatedWithPerson(person);

            if (userContracts == null) userContracts = List.of();
            logger.info(() -> "DashboardBean.init: userContracts size=" + userContracts.size());
        }
    }

    public void onContractSelect() {
        doOnContractSelectInternal();
    }

    private void doOnContractSelectInternal() {
        logger.info(() -> "doOnContractSelectInternal: selectedContractUuid=" + selectedContractUuid);

        if (selectedContractUuid != null && !selectedContractUuid.isEmpty()) {
            selectedContract = contractLogic.findByUuid(selectedContractUuid);
            logger.info(() -> "Selected contract=" + selectedContract);

            currentTimesheet = timesheetLogic.getCurrent(selectedContract);
            if (currentTimesheet == null) {
                logger.warning("No current timesheet found for contract " + selectedContract.getName());
            } else {
                logger.info(() -> "Current timesheet=" + currentTimesheet);
            }

            resetNewEntry();
        } else {
            logger.info("No contract selected -> reset state");
            selectedContract = null;
            currentTimesheet = null;
        }
    }

    public void saveNewEntry() {
        if (newEntry.getEntryDate() == null || newEntry.getStartTime() == null || newEntry.getEndTime() == null) {
            addError("Date, Start Time, and End Time are required.");
            return;
        }
        if (currentTimesheet == null) {
            addError("No active timesheet available to add an entry.");
            return;
        }

        newEntry.setTimesheetUuid(currentTimesheet.getUuid());
        timesheetEntryLogic.create(newEntry);

        currentTimesheet = timesheetLogic.getCurrent(selectedContract);
        if (currentTimesheet == null) {
            logger.warning("After save, currentTimesheet is still null!");
        }

        addInfo("Entry added successfully!");
        resetNewEntry();
    }

    public void startContract() {
        try {
            contractLogic.startContract(selectedContract);
            selectedContract.setStatus(ContractStatus.STARTED);
            addInfo("Contract started successfully.");
        } catch (Exception e) {
            addError("Error starting contract: " + e.getMessage());
        }
    }


    public void terminateContract() {
        try {
            contractLogic.terminate(selectedContract.getUuid(), LocalDate.now());
            selectedContract.setStatus(ContractStatus.TERMINATED);
            addInfo("Contract terminated successfully.");
        } catch (Exception e) {
            addError("Error terminating contract: " + e.getMessage());
        }
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

    private void resetNewEntry() {
        newEntry = new TimesheetEntryDTO();
        newEntry.setEntryDate(LocalDate.now());
        newEntry.setStartTime(LocalTime.of(9, 0));
        newEntry.setEndTime(LocalTime.of(17, 0));
        newEntry.setType(ReportType.WORK); // Тип за замовчуванням
    }

    // --- Getters & Setters ---

    public List<ContractDTO> getUserContracts() {
        return userContracts == null ? java.util.List.of() : userContracts;
    }

    public String getSelectedContractUuid() {
        return selectedContractUuid;
    }

    public void setSelectedContractUuid(String selectedContractUuid) {
        this.selectedContractUuid = selectedContractUuid;
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

    // --- Helper Methods ---
    private void addInfo(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, txt, ""));
    }

    private void addError(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, txt, ""));
    }

    public String roleLabel(ContractDTO c) {
        if (c == null) return "N/A";

        HashSet<String> roles = new HashSet<>();
        if (authorization.isEmployee(c))  roles.add("Employee");
        if (authorization.isSupervisor(c)) roles.add("Supervisor");
        if (authorization.isAssistant(c)) roles.add("Assistant");
        if (authorization.isSecretary(c)) roles.add("Secretary");

        logger.info(() -> "roleLabel for contract=" + c.getName() + " → roles=" + roles);

        return roles.isEmpty() ? "N/A" : String.join(", ", roles);
    }
}
