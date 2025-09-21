package bravo.entities;

import bravo.enums.TimesheetStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity representing a Timesheet for a specific Contract.
 * Contains entries and tracks status and signatures.
 */
@Entity
@Table(name = "timesheet")
@NamedQueries({
        @NamedQuery(name = "Timesheet.findByUuid",
                query = "SELECT e FROM Timesheet e WHERE e.uuid = :uuid"),
        @NamedQuery(name = "Timesheet.findAll",
                query = "SELECT e FROM Timesheet e"),
        @NamedQuery(name = "Timesheet.findByContract",
                query = "SELECT e FROM Timesheet e WHERE e.contract = :contract"),
        @NamedQuery(name = "Timesheet.findByContractUuid",
                query = "SELECT e FROM Timesheet e WHERE e.contract.uuid = :uuid"),
        @NamedQuery(name = "Timesheet.findByStatus",
                query = "SELECT e FROM Timesheet e WHERE e.status = :status"),
        @NamedQuery(name = "Timesheet.findBetweenDates",
                query = "SELECT e FROM Timesheet e WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
})
public class Timesheet extends AbstractEntity {

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

    public Timesheet() {
        super();
    }

    /**
     * Returns the status of the timesheet.
     *
     * @return the TimesheetStatus
     */
    public TimesheetStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the timesheet.
     *
     * @param status the TimesheetStatus to set
     */
    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    /**
     * Returns the start date of the timesheet.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the timesheet.
     *
     * @param startDate the start date to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date of the timesheet.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the timesheet.
     *
     * @param endDate the end date to set
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the date the timesheet was signed by the employee.
     *
     * @return the date signed by employee
     */
    public LocalDate getSignedByEmployee() {
        return signedByEmployee;
    }

    /**
     * Sets the date the timesheet was signed by the employee.
     *
     * @param signedByEmployee the date to set
     */
    public void setSignedByEmployee(LocalDate signedByEmployee) {
        this.signedByEmployee = signedByEmployee;
    }

    /**
     * Returns the date the timesheet was signed by the supervisor.
     *
     * @return the date signed by supervisor
     */
    public LocalDate getSignedBySupervisor() {
        return signedBySupervisor;
    }

    /**
     * Sets the date the timesheet was signed by the supervisor.
     *
     * @param signedBySupervisor the date to set
     */
    public void setSignedBySupervisor(LocalDate signedBySupervisor) {
        this.signedBySupervisor = signedBySupervisor;
    }

    /**
     * Returns the contract associated with this timesheet.
     *
     * @return the Contract entity
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Sets the contract associated with this timesheet.
     *
     * @param contract the Contract entity to associate
     */
    public void setContract(Contract contract) {
        this.contract = contract;
    }

    /**
     * Returns an unmodifiable list of timesheet entries.
     *
     * @return list of TimesheetEntry
     */
    public List<TimesheetEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Sets the entries of the timesheet.
     * Clears existing entries and updates the timesheet reference in each entry.
     *
     * @param entries list of TimesheetEntry to set
     */
    public void setEntries(List<TimesheetEntry> entries) {
        this.entries.forEach(e -> e.setTimesheet(null));
        this.entries.clear();
        if (entries == null) return;
        this.entries.addAll(entries);
        entries.forEach(e -> e.setTimesheet(this));
    }

    /**
     * Adds a single entry to the timesheet.
     * Updates the timesheet reference in the entry.
     *
     * @param entry the TimesheetEntry to add
     */
    public void addEntry(TimesheetEntry entry) {
        if (entry == null) return;
        entries.add(entry);
        entry.setTimesheet(this);
    }
}