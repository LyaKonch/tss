package bravo.web;

import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped; 
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@Named("entryBlockBean")
@ViewScoped
public class EntryBlockBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private TimesheetDTO timesheet;
    private List<TimesheetEntryDTO> entries = new ArrayList<>();
    private TimesheetEntryDTO selected;       
    private EntryForm form = new EntryForm();

    public EntryBlockBean() {
        java.time.YearMonth ym = java.time.YearMonth.now();
LocalDate start = ym.atDay(1);
LocalDate end   = ym.atEndOfMonth();
        this.timesheet = new TimesheetDTO(1L, 99L, start, end,
                "IN_PROGRESS", "STARTED",
                40.0, 0.0, 80.0);
        recalcActual();
    }

    public void save() {
        if (!isEditable()) { addMsg("Timesheet nicht editierbar."); return; }
        try {
            validateForm(); 

            if (form.getId() == null) {
                long newId = entries.stream()
                        .map(TimesheetEntryDTO::getId)
                        .filter(Objects::nonNull)
                        .max(Long::compareTo)
                        .orElse(0L) + 1;

                TimesheetEntryDTO dto = form.toDTO();
                dto.setId(newId);
                dto.setTimesheetId(timesheet.id); 
                entries.add(dto);

            } else { 
                for (int i = 0; i < entries.size(); i++) {
                    if (Objects.equals(entries.get(i).getId(), form.getId())) {
                        TimesheetEntryDTO dto = form.toDTO();
                        dto.setTimesheetId(timesheet.id);
                        entries.set(i, dto);
                        break;
                    }
                }
            }

            recalcActual();
            form = new EntryForm();
            addMsg("Gespeichert.");
        } catch (IllegalArgumentException ex) {
            addMsg(ex.getMessage());
        }
    }

    public void prepareEdit() {
        if (selected == null) return;
        form.from(selected);
    }

    public void deleteSelected() {
        if (!isEditable()) { addMsg("Timesheet nicht editierbar."); return; }
        if (selected == null) return;
        entries.removeIf(x -> Objects.equals(x.getId(), selected.getId()));
        recalcActual();
        selected = null;
        addMsg("Eintrag gelöscht.");
    }

    public void signAsEmployee() {
        if (!isEditable()) { addMsg("Timesheet nicht editierbar."); return; }
        timesheet.status = "SIGNED_BY_EMPLOYEE";
        addMsg("Signiert (Mitarbeiter).");
    }

    public void revokeSignature() {
        if ("SIGNED_BY_EMPLOYEE".equals(timesheet.status)) {
            timesheet.status = "IN_PROGRESS";
            addMsg("Signatur widerrufen.");
        }
    }

    private void validateForm() {
        LocalDate d = parseDate(form.getEntryDateStr());
        if (d == null) throw new IllegalArgumentException("Datum-Format: yyyy-MM-dd");
        if (d.isBefore(timesheet.startDate) || d.isAfter(timesheet.endDate))
            throw new IllegalArgumentException("Datum außerhalb des Timesheet-Zeitraums.");
        form.setEntryDate(d);

        LocalTime s = parseTime(form.getStartTimeStr());
        LocalTime e = parseTime(form.getEndTimeStr());
        if (s == null || e == null) throw new IllegalArgumentException("Zeit-Format: HH:mm");
        if (!e.isAfter(s)) throw new IllegalArgumentException("Ende muss nach Start liegen.");
        form.setStartTime(s);
        form.setEndTime(e);
        ensureNoOverlap(d, s, e);

        form.setHours(Duration.between(s, e).toMinutes() / 60.0);

        if ("VACATION".equals(form.getType())) {
            double used = entries.stream()
                    .filter(x -> "VACATION".equals(x.getType()))
                    .mapToDouble(TimesheetEntryDTO::getHours)
                    .sum();
            if (used + form.getHours() > timesheet.contractVacationHours) {
                throw new IllegalArgumentException("Urlaubsstunden überschreiten das Vertragsbudget.");
            }
        }
    }

    private void ensureNoOverlap(LocalDate d, LocalTime s, LocalTime e) {
        for (TimesheetEntryDTO x : entries) {
            if (form.getId() != null && Objects.equals(x.getId(), form.getId())) continue; 
            if (!d.equals(x.getEntryDate())) continue;

            LocalTime os = x.getStartTime();
            LocalTime oe = x.getEndTime();
            if (s.isBefore(oe) && e.isAfter(os)) {
                throw new IllegalArgumentException(
                    "Zeiten überschneiden sich mit Eintrag am " + d + " (" + os + "–" + oe + ")."
                );
            }
        }
    }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
        catch (Exception ex) { return null; }
    }
    private LocalTime parseTime(String s) {
        try { return LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm")); }
        catch (Exception ex) { return null; }
    }
    private void recalcActual() {
        timesheet.hoursActual = entries.stream().mapToDouble(TimesheetEntryDTO::getHours).sum();
    }
    private void addMsg(String txt) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(txt));
    }

   
    public boolean isEditable() {
        return "IN_PROGRESS".equals(timesheet.status) && "STARTED".equals(timesheet.contractStatus);
    }
    public boolean getCanSign()   { return "IN_PROGRESS".equals(timesheet.status); }
    public boolean getCanRevoke() { return "SIGNED_BY_EMPLOYEE".equals(timesheet.status); }

    public String getPeriodLabel() { return timesheet.startDate + " – " + timesheet.endDate; }
    public String getStatusLabel() { return timesheet.status; }
    public double getHoursDue()    { return timesheet.hoursDue; }
    public double getHoursActual() { return timesheet.hoursActual; }

    public List<TimesheetEntryDTO> getEntries() { return entries; }
    public EntryForm getForm() { return form; }
    public TimesheetEntryDTO getSelected() { return selected; }
    public void setSelected(TimesheetEntryDTO selected) { this.selected = selected; }

    
    public static class TimesheetDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        public Long id, contractId;
        public LocalDate startDate, endDate;
        public String status, contractStatus;
        public double hoursDue, hoursActual, contractVacationHours;
        public TimesheetDTO(Long id, Long contractId, LocalDate start, LocalDate end,
                            String status, String cStatus, double due, double actual, double vacHours) {
            this.id=id; this.contractId=contractId; this.startDate=start; this.endDate=end;
            this.status=status; this.contractStatus=cStatus; this.hoursDue=due; this.hoursActual=actual;
            this.contractVacationHours = vacHours;
        }
    }

    public static class TimesheetEntryDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id, timesheetId;
        private LocalDate entryDate;
        private LocalTime startTime, endTime;
        private String type;       
        private String description;
        private double hours;

        public TimesheetEntryDTO() {}

        public TimesheetEntryDTO(Long id, Long tsId, LocalDate date, LocalTime s, LocalTime e,
                                 String type, String desc, double hours) {
            this.id = id; this.timesheetId = tsId; this.entryDate = date;
            this.startTime = s; this.endTime = e; this.type = type;
            this.description = desc; this.hours = hours;
        }

        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTimesheetId() { return timesheetId; }
        public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

        public LocalDate getEntryDate() { return entryDate; }
        public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getHours() { return hours; }
        public void setHours(double hours) { this.hours = hours; }
    }

    public static class EntryForm implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;

        // Strings fürs XHTML-Binding
        private String entryDateStr = LocalDate.now().toString();
        private String startTimeStr;
        private String endTimeStr;

        // geparste Werte
        private LocalDate entryDate;
        private LocalTime startTime, endTime;

        private String type = "WORK";
        private String description;
        private double hours;

        public void from(TimesheetEntryDTO e) {
            setId(e.getId());
            setEntryDate(e.getEntryDate());
            setEntryDateStr(e.getEntryDate() != null ? e.getEntryDate().toString() : "");
            setStartTime(e.getStartTime());
            setEndTime(e.getEndTime());
            setStartTimeStr(e.getStartTime() != null ? e.getStartTime().toString() : "");
            setEndTimeStr(e.getEndTime() != null ? e.getEndTime().toString() : "");
            setType(e.getType());
            setDescription(e.getDescription());
            setHours(e.getHours());
        }

        public TimesheetEntryDTO toDTO() {
            return new TimesheetEntryDTO(id, null, entryDate, startTime, endTime, type, description, hours);
        }

        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEntryDateStr() { return entryDateStr; }
        public void setEntryDateStr(String entryDateStr) { this.entryDateStr = entryDateStr; }

        public String getStartTimeStr() { return startTimeStr; }
        public void setStartTimeStr(String startTimeStr) { this.startTimeStr = startTimeStr; }

        public String getEndTimeStr() { return endTimeStr; }
        public void setEndTimeStr(String endTimeStr) { this.endTimeStr = endTimeStr; }

        public LocalDate getEntryDate() { return entryDate; }
        public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getHours() { return hours; }
        public void setHours(double hours) { this.hours = hours; }
    }
}