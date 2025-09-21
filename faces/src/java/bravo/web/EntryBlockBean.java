package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.TimesheetDTO;
import bravo.dto.TimesheetEntryDTO;
import bravo.enums.ContractStatus;
import bravo.enums.ReportType;
import bravo.enums.TimesheetStatus;
import bravo.logic.TimesheetEntryLogic;
import bravo.logic.TimesheetLogic;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

@Named
@ViewScoped
public class EntryBlockBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(EntryBlockBean.class.getName());

    ResourceBundle msg = FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");

    @Inject
    private TimesheetLogic timesheetLogic;

    @Inject
    private TimesheetEntryLogic timesheetEntryLogic;

    private ContractDTO contract;

    private TimesheetDTO timesheet;

    private TimesheetEntryDTO currentEntry;

    private List<TimesheetEntryDTO> entries = new ArrayList<>();

    private boolean editMode = false;


    public void init(ContractDTO contract) {
        if (contract == null || contract.getUuid() == null) return;
        if (contract.getTimesheetUuids().isEmpty()) {
            logger.info("No timesheets found for contract " + contract.getName());
            return;
        }

        this.contract = contract;
        try {
            timesheet = timesheetLogic.getCurrent(contract);
            if (timesheet == null) return;
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.getString("entry.noCurrentTimesheetFound"), e.getMessage()));
            return;
        }

        reloadEntryList();

        logger.info("Initialized EntryBlockBean for contract " + contract.getName());
    }

    /**
     * Creates a new entry with sensible default values.
     */
    public void newEntryWithDefaults() {
        currentEntry = new TimesheetEntryDTO();
        currentEntry.setType(ReportType.WORK);
        currentEntry.setStartTime(LocalTime.of(8, 0));
        currentEntry.setEndTime(LocalTime.of(17, 0));
        currentEntry.setHours(calcHours(currentEntry));
        currentEntry.setEntryDate(LocalDate.now());
        currentEntry.setTimesheetUuid(timesheet.getUuid());
    }

    public void reloadEntryList() {
        entries = timesheetEntryLogic.getByTimesheetUuid(timesheet.getUuid());
        newEntryWithDefaults();
        recalcTimesheetHoursDue();
    }

    public void recalcFormHours() {
        LocalTime start = currentEntry.getStartTime();
        LocalTime end = currentEntry.getEndTime();
        double hours = 0.0;
        if (start != null && end != null && end.isAfter(start)) {
            hours = Duration.between(start, end).toMinutes() / 60.0;
        }
        currentEntry.setHours(hours);
    }

    public void edit(TimesheetEntryDTO entry) {
        reloadEntryList();
        currentEntry = entry;
        editMode = true;
        addInfo(msg.getString("entry.preparedEdit"));
    }

    public String save() {
        if (!isEditable()) {
            if (editMode) {
                addError(msg.getString("entry.notEditable"));
            } else {
                addError(msg.getString("entry.cannotSave"));
            }
            return null;
        }
        try {
            validateForm();

            if (currentEntry.getId() == null) timesheetEntryLogic.create(currentEntry);
            else {
                timesheetEntryLogic.update(currentEntry);
                editMode = false;
            }

            reloadEntryList();

            addInfo(msg.getString("label.saved"));
            logger.info("Saved entry.");

        } catch (IllegalArgumentException ex) {
            addError(ex.getMessage());
        }
        return null;
    }

    public void deleteSelected(TimesheetEntryDTO entry) {
        if (!isEditable()) {
            addError(msg.getString("entry.cannotDelete"));
            return;
        }
        if (entry == null || entry.getId() == null) return;
        try {
            timesheetEntryLogic.delete(entry);
            addInfo(msg.getString("entry.deleted"));
        } catch (Exception e) {
            addError(msg.getString("entry.deletionFail"));
            return;
        }

        reloadEntryList();

        addInfo(msg.getString("entry.deleted"));
    }

    private void validateForm() {
        LocalDate entryDate = currentEntry.getEntryDate();
        if (entryDate == null) throw new IllegalArgumentException("Datum fehlt.");
        if (entryDate.isBefore(timesheet.getStartDate()) || entryDate.isAfter(timesheet.getEndDate()))
            throw new IllegalArgumentException("Datum außerhalb des Timesheet-Zeitraums.");

        LocalTime startTime = currentEntry.getStartTime();
        LocalTime endTime = currentEntry.getEndTime();
        if (startTime == null || endTime == null) throw new IllegalArgumentException("Zeit fehlt.");
        if (!endTime.isAfter(startTime)) throw new IllegalArgumentException("Ende muss nach Start liegen.");

        ensureNoOverlap(entryDate, startTime, endTime);

        double newHours = Duration.between(startTime, endTime).toMinutes() / 60.0;
        if (currentEntry.getType() == ReportType.VACATION) {
            double used = entries.stream()
                    .filter(x -> x.getType() == ReportType.VACATION)
                    .mapToDouble(this::calcHours)
                    .sum();
            if (used + newHours > contract.getVacationHours()) {
                throw new IllegalArgumentException("Urlaubsstunden überschreiten das Vertragsbudget.");
            }
        }
        logger.info("Validated new TimesheetEntry!");
    }

    private void ensureNoOverlap(LocalDate entryDate, LocalTime startDate, LocalTime endDate) {
        for (TimesheetEntryDTO entry : entries) {
            if (currentEntry.getUuid() != null && entry.equals(currentEntry)) continue;
            if (!entryDate.equals(entry.getEntryDate())) continue;

            LocalTime os = entry.getStartTime();
            LocalTime oe = entry.getEndTime();
            if (os != null && oe != null && startDate.isBefore(oe) && endDate.isAfter(os)) {
                throw new IllegalArgumentException(
                        "Zeiten überschneiden sich mit Eintrag am " + entryDate + " (" + os + "–" + oe + ")."
                );
            }
        }
    }

    public String formatDate(LocalDate date) {
        if (date == null) return "Invalid";
        Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(currentLocale);
        return date.format(formatter);
    }

    public double calcHours(TimesheetEntryDTO e) {
        if (e.getStartTime() == null || e.getEndTime() == null) return 0.0;
        return Duration.between(e.getStartTime(), e.getEndTime()).toMinutes() / 60.0;
    }

    private void recalcTimesheetHoursDue() {
        timesheet.setHoursDue(entries.stream().mapToDouble(this::calcHours).sum());
    }

    private void addInfo(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, txt, ""));
    }

    private void addError(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, txt, ""));
    }

    public boolean isEditable() {
        if (timesheet == null) return false;
        return timesheet.getStatus() == TimesheetStatus.IN_PROGRESS && contract.getStatus() == ContractStatus.STARTED;
    }

    public boolean canSign() {
        return timesheet != null && timesheet.getStatus() == TimesheetStatus.IN_PROGRESS;
    }

    public boolean canRevoke() {
        return timesheet != null && timesheet.getStatus() == TimesheetStatus.SIGNED_BY_EMPLOYEE;
    }

    public List<TimesheetEntryDTO> getEntries() {
        return entries;
    }

    public TimesheetEntryDTO getCurrentEntry() {
        return currentEntry;
    }

    public void setCurrentEntry(TimesheetEntryDTO currentEntry) {
        this.currentEntry = currentEntry;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}
