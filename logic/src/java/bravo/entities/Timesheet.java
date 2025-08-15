package bravo.entities;

import bravo.enums.TimesheetStatus;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Entity
@Table(name = "timesheet")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "contract_id")
    private Contract contract;

    private LocalDate startDate;     
    private LocalDate endDate;      

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status = TimesheetStatus.IN_PROGRESS;

    private LocalDate signedByEmployee;
    private LocalDate signedBySupervisor;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();

    @Transient
    private static Predicate<LocalDate> holidayChecker;
    public static void setHolidayChecker(Predicate<LocalDate> checker) { holidayChecker = checker; }

    @Transient
    public double getHoursDue() {
        if (contract == null || startDate == null || endDate == null) return 0.0;
        int working = countWorkingDays(startDate, endDate);
        int holidays = countHolidaysOnWorkingDays(startDate, endDate);
        int effectiveDays = Math.max(0, working - holidays);
        return effectiveDays * (contract.getHoursPerWeek() / contract.getWorkingDaysPerWeek());
    }

    @Transient
    public double getReportedHours() {
        return entries.stream().mapToDouble(TimesheetEntry::getHours).sum();
    }

    public void signByEmployee(LocalDate date) {
        requireStatus(TimesheetStatus.IN_PROGRESS);
        this.signedByEmployee = (date != null) ? date : LocalDate.now();
        this.status = TimesheetStatus.SIGNED_BY_EMPLOYEE;
    }

    public void revokeEmployeeSignature() {
        requireStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        this.signedByEmployee = null;
        this.status = TimesheetStatus.IN_PROGRESS;
    }

    public void signBySupervisor(LocalDate date) {
        requireStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        this.signedBySupervisor = (date != null) ? date : LocalDate.now();
        this.status = TimesheetStatus.SIGNED_BY_SUPERVISOR;
    }

    public void archive() {
        requireStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        this.status = TimesheetStatus.ARCHIVED;
    }

    private static int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

    private static int countHolidaysOnWorkingDays(LocalDate start, LocalDate end) {
        if (holidayChecker == null) return 0;
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            boolean workday = (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY);
            if (workday && holidayChecker.test(dat)) count++;
        }
        return count;
    }

    private void requireStatus(TimesheetStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("Aktion nur im Status " + expected + " erlaubt.");
        }
    }

    private void requireEditable() {
        if (this.status != TimesheetStatus.IN_PROGRESS) {
            throw new IllegalStateException("Einträge nur im Status IN_PROGRESS änderbar.");
        }
    }

    //Getter&Setter
    public Long getId() { return id; }

    public Contract getContract() { 
        return contract; 
    }
    public void setContract(Contract contract) { 
        this.contract = contract; 
    }

    public LocalDate getStartDate() { 
        return startDate; 
    }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    public LocalDate getEndDate() { 
        return endDate; 
    }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

    public TimesheetStatus getStatus() { 
        return status; 
    }
    public void setStatus(TimesheetStatus status) { 
        this.status = status; 
    }

    public LocalDate getSignedByEmployee() { 
        return signedByEmployee; 
    }
    public void setSignedByEmployee(LocalDate signedByEmployee) { 
        this.signedByEmployee = signedByEmployee; 
    }

    public LocalDate getSignedBySupervisor() { 
        return signedBySupervisor; 
    }
    public void setSignedBySupervisor(LocalDate signedBySupervisor) { 
        this.signedBySupervisor = signedBySupervisor; 
    }

    public List<TimesheetEntry> getEntries() { 
        return Collections.unmodifiableList(entries); 
    }
    public void setEntries(List<TimesheetEntry> newEntries) {
        requireEditable();
        this.entries.clear();
        if (newEntries != null) newEntries.forEach(this::addEntry);
    }
    public void addEntry(TimesheetEntry e) {
        requireEditable();
        if (e != null) { e.setTimesheet(this); entries.add(e); }
    }
    public void removeEntry(TimesheetEntry e) {
        requireEditable();
        if (e != null) { entries.remove(e); e.setTimesheet(null); }
    }
}
