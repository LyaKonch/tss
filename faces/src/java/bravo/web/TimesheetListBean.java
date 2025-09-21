package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.dto.TimesheetDTO;
import bravo.enums.TimesheetStatus;
import bravo.logic.ContractLogic;
import bravo.logic.TimesheetLogic;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.event.Observes;
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
import java.util.logging.Logger;

/**
 * Backing bean for timesheet list view.
 * This bean provides functionality for viewing, searching and interacting with timesheets.
 */
@Named
@ViewScoped
public class TimesheetListBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(TimesheetListBean.class.getName());

    private final ResourceBundle locale = FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");

    @EJB
    private ContractLogic contractLogic;

    @Inject
    private TimesheetLogic timesheetLogic;

    @Inject
    private ContractSelectBean contractSelectBean;

    @Inject
    private Authorization authorization;

    private ContractDTO selectedContract;

    private List<ContractDTO> contractList = new ArrayList<>();

    private List<TimesheetDTO> timesheetList = new ArrayList<>();

    private boolean firstPrepareCall = true;

    /**
     * Initializes the bean by loading contracts.
     */
    @PostConstruct
    public void init() {
        PersonDTO person = authorization.getLoggedInPerson();
        contractList = contractLogic.findContractsAssociatedWithPerson(person);
        if (contractList == null) contractList = new ArrayList<>();
        logger.info("TimesheetListBean.init: Found " + contractList.size() + " contracts for this user.");
    }

    public void prepareContractSelect() {
        if (firstPrepareCall) contractSelectBean.init(loadContracts());
        firstPrepareCall = false;
    }

    /**
     * Loads all timesheets from the database relevant to the logged-in user.
     */
    public List<ContractDTO> loadContracts() {
        try {
            if (authorization.isUserInRole("ADMIN")) contractList = contractLogic.findAll();
            if (authorization.isUserInRole("USER")
                    || authorization.isUserInRole("STAFF") && authorization.isUserInRole("ADMIN")) {
                contractList = contractLogic.findContractsAssociatedWithPerson(authorization.getLoggedInPerson());
            }
        } catch (Exception e) {
            contractList = new ArrayList<>();
            addError("contractNoMatches");
        }
        return contractList;
    }

    /**
     * Loads all timesheets from the database relevant to the logged-in user.
     */
    public void loadTimesheets() {
        try {
            timesheetList = timesheetLogic.findByContractUuid(selectedContract.getUuid());
        } catch (Exception e) {
            timesheetList = new ArrayList<>();
            addError(locale.getString("timesheetNoMatches"));
        }
    }

    public void onContractSelect(@Observes ContractDTO selected) {
        this.timesheetList.clear();
        this.selectedContract = selected;
        logger.info("DashboardBean.onSelect: Chosen contract:\n" + selectedContract);
        loadTimesheets();
    }

    public boolean canEmployeeSign(TimesheetDTO timesheet) {
        if (timesheet == null) return false;
        return timesheet.getStatus() == TimesheetStatus.IN_PROGRESS && authorization.isEmployee(selectedContract);
    }

    public void signAsEmployee(TimesheetDTO currentTimesheet) {
        if (currentTimesheet == null) {
            addError(locale.getString("entry.noCurrentTimesheetFound"));
            return;
        }
        try {
            timesheetLogic.signByEmployee(currentTimesheet, LocalDate.now());
            addInfo("timesheetSignEmployeeSuccess");
        } catch (Exception e) {
            addError("timesheetSignEmployeeFail", e.getMessage());
            return;
        }
        loadTimesheets();
    }

    public boolean canRevokeEmployeeSign(TimesheetDTO timesheet) {
        if (timesheet == null) return false;
        return timesheet.getStatus() == TimesheetStatus.SIGNED_BY_EMPLOYEE && authorization.isEmployee(selectedContract);
    }

    public void revokeSignAsEmployee(TimesheetDTO currentTimesheet) {
        if (currentTimesheet == null) {
            addError("entry.noCurrentTimesheetFound");
            return;
        }
        try {
            timesheetLogic.revokeEmployeeSignature(currentTimesheet);
            addInfo("timesheetRevokeEmployeeSuccess");
        } catch (Exception e) {
            addError("timesheetRevokeEmployeeFail", e.getMessage());
            return;
        }
        loadTimesheets();
    }

    public boolean canSupervisorSign(TimesheetDTO timesheet) {
        return timesheet.getStatus() == TimesheetStatus.SIGNED_BY_EMPLOYEE && authorization.isSupervisor(selectedContract);
    }

    public void signAsSupervisor(TimesheetDTO currentTimesheet) {
        if (currentTimesheet == null) {
            addError("entry.noCurrentTimesheetFound");
            return;
        }
        try {
            timesheetLogic.signBySupervisor(currentTimesheet, LocalDate.now());
            addInfo("timesheetSignSupervisorSuccess");
        } catch (Exception e) {
            addError("timesheetSignSupervisorFail", e.getMessage());
            return;
        }
        loadTimesheets();
    }

    public boolean canArchive(TimesheetDTO timesheet) {
        return timesheet.getStatus() == TimesheetStatus.SIGNED_BY_SUPERVISOR && authorization.isSecretary(selectedContract);
    }

    public void archiveTimesheet(TimesheetDTO currentTimesheet) {
        if (currentTimesheet == null) {
            addError("entry.noCurrentTimesheetFound");
            return;
        }
        try {
            timesheetLogic.archive(currentTimesheet);
            addInfo("timesheetArchiveSuccess");
        } catch (Exception e) {
            addError("timesheetArchiveFail" + e.getMessage());
            return;
        }
        loadTimesheets();
    }

    public String getWeekOfYear(LocalDate date) {
        return date != null ? String.valueOf(date.get(java.time.temporal.WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())) : "Invalid";
    }

    public String formatDate(LocalDate date) {
        if (date == null) return "Invalid";
        Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(currentLocale);
        return date.format(formatter);
    }

    private void addInfo(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, locale.getString(txt), ""));
    }

    private void addError(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString(txt), ""));
    }

    private void addError(String txt, String details) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString(txt), details != null ? details : ""));
    }


    public List<ContractDTO> getContractList() {
        if (contractList == null) loadContracts();
        return contractList;
    }

    public void setContractList(List<ContractDTO> contractList) {
        if (contractList == null) return;
        this.contractList.clear();
        this.contractList.addAll(contractList);
    }

    public List<TimesheetDTO> getTimesheetList() {
        return timesheetList;
    }

    public void setTimesheetList(List<TimesheetDTO> timesheetList) {
        if (timesheetList == null) return;
        this.timesheetList.clear();
        this.timesheetList.addAll(timesheetList);
    }
}
