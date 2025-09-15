package bravo.web;

import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import bravo.dto.TimesheetEntryDTO;
import bravo.enums.ReportType;

@Named
@ViewScoped
public class EntryBlockBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TimesheetStatus { IN_PROGRESS, SIGNED_BY_EMPLOYEE, SIGNED_BY_SUPERVISOR, ARCHIVED }
    public enum ContractStatus  { PREPARED, STARTED, TERMINATED, ARCHIVED }

    public static class TimesheetDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        public String uuid;
        public Long contractId;
        public LocalDate startDate, endDate;
        public TimesheetStatus status;
        public ContractStatus contractStatus;
        public double hoursDue, hoursActual, contractVacationHours;

        public TimesheetDTO(String uuid, Long contractId, LocalDate start, LocalDate end,
                            TimesheetStatus status, ContractStatus cStatus,
                            double due, double actual, double vacHours) {
            this.uuid = uuid; this.contractId = contractId; this.startDate = start; this.endDate = end;
            this.status = status; this.contractStatus = cStatus; this.hoursDue = due; this.hoursActual = actual;
            this.contractVacationHours = vacHours;
        }
    }

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static LocalDate toLocalDate(Date d) {
        return d == null ? null : d.toInstant().atZone(ZONE).toLocalDate();
    }
    private static LocalTime toLocalTime(Date d) {
        return d == null ? null : d.toInstant().atZone(ZONE).toLocalTime();
    }
    private static Date toDate(LocalDate d) {
        return d == null ? null : Date.from(d.atStartOfDay(ZONE).toInstant());
    }
    private static Date toDate(LocalTime t) {
        return t == null ? null : Date.from(t.atDate(LocalDate.now()).atZone(ZONE).toInstant());
    }

    private TimesheetDTO timesheet;
    private List<TimesheetEntryDTO> entries = new ArrayList<>();
    private TimesheetEntryDTO selected;
    private EntryForm form = new EntryForm();

    public EntryBlockBean() { }

    @PostConstruct
    public void init() {
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        String tsUuid = UUID.randomUUID().toString();
        this.timesheet = new TimesheetDTO(
                tsUuid, 99L, start, end,
                TimesheetStatus.IN_PROGRESS, ContractStatus.STARTED,
                40.0, 0.0, 80.0
        );
        recalcActual();
    }

    public void recalcFormHours() {
        LocalTime s = toLocalTime(form.getStartTime());
        LocalTime e = toLocalTime(form.getEndTime());
        double h = 0.0;
        if (s != null && e != null && e.isAfter(s)) {
            h = Duration.between(s, e).toMinutes() / 60.0;
        }
        form.setHours(h);
    }

    public String save() {
        if (!isEditable()) {
            addError("Timesheet nicht editierbar.");
            return null;
        }
        try {
            validateForm();

            LocalTime s = toLocalTime(form.getStartTime());
            LocalTime e = toLocalTime(form.getEndTime());
            form.setHours(Duration.between(s, e).toMinutes() / 60.0);

            LocalDate d = toLocalDate(form.getEntryDate());

            if (form.getUuid() == null) {
                TimesheetEntryDTO dto = new TimesheetEntryDTO(
                        UUID.randomUUID().toString(),
                        0,
                        timesheet.uuid,
                        d,
                        s,
                        e,
                        form.getType(),
                        form.getDescription()
                );
                entries.add(dto);
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    if (Objects.equals(entries.get(i).getUuid(), form.getUuid())) {
                        TimesheetEntryDTO dto = new TimesheetEntryDTO(
                                form.getUuid(),
                                form.getJpaVersion(),
                                timesheet.uuid,
                                d,
                                s,
                                e,
                                form.getType(),
                                form.getDescription()
                        );
                        entries.set(i, dto);
                        break;
                    }
                }
            }

            recalcActual();
            form = new EntryForm(); 
            addInfo("Gespeichert.");
        } catch (IllegalArgumentException ex) {
            addError(ex.getMessage());
        }
        return null;
    }

    public void prepareEdit() {
        if (selected == null) return;
        form.from(selected);
    }

    public void deleteSelected() {
        if (!isEditable()) { addError("Timesheet nicht editierbar."); 
        return; 
        }
        if (selected == null) return;
        entries.removeIf(x -> Objects.equals(x.getUuid(), selected.getUuid()));
        recalcActual();
        selected = null;
        addInfo("Eintrag gelöscht.");
    }

    public void signAsEmployee() {
        if (!isEditable()) { addError("Timesheet nicht editierbar."); return; }
        timesheet.status = TimesheetStatus.SIGNED_BY_EMPLOYEE;
        addInfo("Signiert (Mitarbeiter).");
    }

    public void revokeSignature() {
        if (timesheet.status == TimesheetStatus.SIGNED_BY_EMPLOYEE) {
            timesheet.status = TimesheetStatus.IN_PROGRESS;
            addInfo("Signatur widerrufen.");
        }
    }

    private void validateForm() {
        LocalDate d = toLocalDate(form.getEntryDate());
        if (d == null) throw new IllegalArgumentException("Datum fehlt.");
        if (d.isBefore(timesheet.startDate) || d.isAfter(timesheet.endDate))
            throw new IllegalArgumentException("Datum außerhalb des Timesheet-Zeitraums.");

        LocalTime s = toLocalTime(form.getStartTime());
        LocalTime e = toLocalTime(form.getEndTime());
        if (s == null || e == null) throw new IllegalArgumentException("Zeit fehlt.");
        if (!e.isAfter(s)) throw new IllegalArgumentException("Ende muss nach Start liegen.");

        ensureNoOverlap(d, s, e);

        double newHours = Duration.between(s, e).toMinutes() / 60.0;
        if (form.getType() == ReportType.VACATION) {
            double used = entries.stream()
                    .filter(x -> x.getType() == ReportType.VACATION)
                    .mapToDouble(this::calcHours)
                    .sum();
            if (used + newHours > timesheet.contractVacationHours) {
                throw new IllegalArgumentException("Urlaubsstunden überschreiten das Vertragsbudget.");
            }
        }
    }

    private void ensureNoOverlap(LocalDate d, LocalTime s, LocalTime e) {
        for (TimesheetEntryDTO x : entries) {
            if (form.getUuid() != null && Objects.equals(x.getUuid(), form.getUuid())) continue;
            if (!d.equals(x.getEntryDate())) continue;

            LocalTime os = x.getStartTime();
            LocalTime oe = x.getEndTime();
            if (os != null && oe != null && s.isBefore(oe) && e.isAfter(os)) {
                throw new IllegalArgumentException(
                    "Zeiten überschneiden sich mit Eintrag am " + d + " (" + os + "–" + oe + ")."
                );
            }
        }
    }

    public double calcHours(TimesheetEntryDTO e) {
        if (e.getStartTime() == null || e.getEndTime() == null) return 0.0;
        return Duration.between(e.getStartTime(), e.getEndTime()).toMinutes() / 60.0;
    }

    private void recalcActual() {
        timesheet.hoursActual = entries.stream().mapToDouble(this::calcHours).sum();
    }

    private void addInfo(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, txt, null));
    }
    private void addError(String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, txt, null));
    }

    public boolean isEditable() {
        return timesheet != null
            && timesheet.status == TimesheetStatus.IN_PROGRESS
            && timesheet.contractStatus == ContractStatus.STARTED;
    }
    public boolean getCanSign()   { return timesheet != null && timesheet.status == TimesheetStatus.IN_PROGRESS; }
    public boolean getCanRevoke() { return timesheet != null && timesheet.status == TimesheetStatus.SIGNED_BY_EMPLOYEE; }

    public String getStatusLabel() { return timesheet == null ? "" : timesheet.status.name(); }
    public double getHoursActual() { return timesheet == null ? 0.0 : timesheet.hoursActual; }

    public List<TimesheetEntryDTO> getEntries() { return entries; }
    public EntryForm getForm() { return form; }
    public TimesheetEntryDTO getSelected() { return selected; }
    public void setSelected(TimesheetEntryDTO selected) { this.selected = selected; }

  
    public static class EntryForm implements Serializable {
        private static final long serialVersionUID = 1L;

        private String uuid;
        private int jpaVersion;

        private Date entryDate;
        private Date startTime;
        private Date endTime;

        private ReportType type = ReportType.WORK;
        private String description;
        private double hours;

        public void from(TimesheetEntryDTO e) {
            setUuid(e.getUuid());
            setJpaVersion(e.getJpaVersion());
            setEntryDate(EntryBlockBean.toDate(e.getEntryDate()));
            setStartTime(EntryBlockBean.toDate(e.getStartTime()));
            setEndTime(EntryBlockBean.toDate(e.getEndTime()));
            setType(e.getType());
            setDescription(e.getDescription());
            setHours((e.getStartTime()!=null && e.getEndTime()!=null)
                    ? Duration.between(e.getStartTime(), e.getEndTime()).toMinutes()/60.0 : 0.0);
        }

        public String getUuid() { 
            return uuid; 
        }
        public void setUuid(String uuid) { 
            this.uuid = uuid; 
        }
        public int getJpaVersion() { 
            return jpaVersion; 
        }
        public void setJpaVersion(int v) { 
            this.jpaVersion = v; 
        }

        public Date getEntryDate() { 
            return entryDate; 
        }
        public void setEntryDate(Date entryDate) { 
            this.entryDate = entryDate; 
        }

        public Date getStartTime() { 
            return startTime;
        } 
        public void setStartTime(Date startTime) { 
            this.startTime = startTime;
        }

        public Date getEndTime() { 
            return endTime; 
        }
        public void setEndTime(Date endTime) { 
            this.endTime = endTime; 
        }

        public ReportType getType() { 
            return type; 
        }
        public void setType(ReportType type) { 
            this.type = type; 
        }

        public String getDescription() { 
            return description; 
        }
        public void setDescription(String description) { 
            this.description = description; 
        }

        public double getHours() { 
            return hours; 
        }
        public void setHours(double hours) { 
            this.hours = hours; 
        }
    }
}
