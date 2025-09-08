package bravo.entities;

import bravo.enums.TimesheetStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "timesheet")
public class Timesheet extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status = TimesheetStatus.IN_PROGRESS;

    private LocalDate signedByEmployee;

    private LocalDate signedBySupervisor;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();

    //Getter&Setter
    public Timesheet() {
        super();
    }

    public Timesheet(boolean NewEntity) {
        super(NewEntity);
    }

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

    public void setEntries(List<TimesheetEntry> entries) {
        this.entries.forEach(e -> e.setTimesheet(null));
        this.entries.clear();
        if (entries == null) return;
        this.entries.addAll(entries);
        entries.forEach(e -> e.setTimesheet(this));
    }

    public void addEntry(TimesheetEntry entry) {
        if (entry == null) return;
        entries.add(entry);
        entry.setTimesheet(this);
    }
}
