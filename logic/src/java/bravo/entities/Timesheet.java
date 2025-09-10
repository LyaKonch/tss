package bravo.entities;

import bravo.enums.TimesheetStatus;
import bravo.utitity.PublicHolidayService;
import jakarta.persistence.*;
import java.time.DayOfWeek;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "timesheet")
@NamedQueries({
    @NamedQuery(name = "Timesheet.findByUuid",
            query = "SELECT e FROM Timesheet e WHERE e.uuid = :uuid"),
    @NamedQuery(name = "Timesheet.findAll",
            query = "SELECT e FROM Timesheet e"),
    @NamedQuery(name = "Timesheet.findByContract",
            query = "SELECT e FROM Timesheet e WHERE e.contract = :contract"),
    @NamedQuery(name = "Timesheet.findByStatus",
            query = "SELECT e FROM Timesheet e WHERE e.status = :status"),
    @NamedQuery(name = "Timesheet.findBetweenDates",
            query = "SELECT e FROM Timesheet e WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
})
public class Timesheet extends AbstractEntity {
    
    
    
    // it's a questionable choice, but i need to access holidays checker somehow and it creates static reference to it
    // it needs to be initialized by ejb exemplar of PublicHolidayService at startup
    // it was previously in timesheetLogicBean, but the issue was the Contract entity transient field used method to calculate it's hoursDue using method in TimesheetLogicBean
    // static fields are persisted by definition by JPA as far as i understand
    private static PublicHolidayService holidayChecker;
    
    @Enumerated(EnumType.STRING)
    private TimesheetStatus status = TimesheetStatus.IN_PROGRESS;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate signedByEmployee;

    private LocalDate signedBySupervisor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();
    
    @Transient
    private double hoursDue() {
        if (this.contract == null || this.startDate == null || this.endDate == null) {
            return 0.0;
        }
        int working = countWorkingDays(this.getStartDate(), this.getEndDate());
        int holidays = countHolidaysOnWorkingDays(this.getStartDate(), this.getEndDate());
        int effectiveDays = Math.max(0, working - holidays);
        return effectiveDays * (contract.getHoursPerWeek() / contract.getWorkingDaysPerWeek());
    }
    
    public Timesheet() {
        super();
    }

    public Timesheet(boolean NewEntity) {
        super(NewEntity);
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
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

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public List<TimesheetEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void setEntries(List<TimesheetEntry> entries) {
        this.entries.forEach(e -> e.setTimesheet(null));
        this.entries.clear();
        if (entries == null) {
            return;
        }
        this.entries.addAll(entries);
        entries.forEach(e -> e.setTimesheet(this));
    }

    public void addEntry(TimesheetEntry entry) {
        if (entry == null) {
            return;
        }
        entries.add(entry);
        entry.setTimesheet(this);
    }
    
    public int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    public int countHolidaysOnWorkingDays(LocalDate start, LocalDate end) {
        if (holidayChecker == null) {
            return 0;
        }
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            boolean workday = (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY);
            if (workday && holidayChecker.isHoliday(dat, this.contract.state)) {
                count++;
            }
        }
        return count;
    }
}
