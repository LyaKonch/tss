package bravo.entities;

import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JPA Entity representing a Contract.
 * <p>
 * A Contract has a status, associated employee and supervisor, secretaries, assistants, and timesheets.
 * Provides calculated fields such as vacation hours.
 */
@Entity
@Table(name = "contract")
@NamedQueries({
        @NamedQuery(name = "Contract.findByName",
                query = "SELECT c FROM Contract c WHERE c.name = :name"),
        @NamedQuery(name = "Contract.findAll",
                query = "SELECT c FROM Contract c ORDER BY c.startDate DESC"),
        @NamedQuery(name = "Contract.findByUuid",
                query = "SELECT c FROM Contract c WHERE c.uuid = :uuid")
})
public class Contract extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.PREPARED;

    @NotBlank
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TimesheetFrequency frequency = TimesheetFrequency.MONTHLY;

    @Nullable
    private LocalDate terminationDate = null;

    @Positive
    private Double hoursPerWeek;

    @Column(nullable = false)
    @Min(1)
    @Max(7)
    private int workingDaysPerWeek = 5;

    @Column(nullable = false)
    @Min(0)
    @Max(365)
    private int vacationDaysPerYear = 20;

    // --- Role Associations ---

    /** Contract is associated with exactly one Employee. */
    @OneToOne(mappedBy = "contract", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    private Employee employee;

    /** Contract is associated with exactly one Supervisor. */
    @OneToOne(mappedBy = "contract", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    private Supervisor supervisor;

   /** Contract is associated with one or more Secretaries. */
    @NotNull
    @Size(min = 1, message = "Every Contract needs at least one Secretary.")
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Secretary> secretaries = new ArrayList<>();

    /** Contract may have zero or more Assistants. */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assistant> assistants = new ArrayList<>();

    /** Contract may have zero or more Timesheets. */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Timesheet> timesheets = new ArrayList<>();


    public Contract() {
        super();
    }

    // --- Transient Calculated Fields ---
    
    @Transient
    private double vacationHours() {
        int months = getDurationInWholeMonths();
        if (months <= 0 || hoursPerWeek <= 0) return 0.0;
        return vacationDaysPerYear * (months / 12.0) * (hoursPerWeek / workingDaysPerWeek);
    }

    private int getDurationInWholeMonths() {
        if (startDate == null || endDate == null) return 0;
        return (int) ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1)) + 1;
    }


    // --- Getters and Setters ---
    
    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        if (status == null) throw new IllegalArgumentException("Status must not be null");
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be null or blank");
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate d) {
        this.startDate = d;
        if (d != null && d.getDayOfMonth() != 1)
            throw new IllegalArgumentException("startDate has to be first of a month");
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate d) {
        this.endDate = d;
        if (d != null && d.plusDays(1).getDayOfMonth() != 1)
            throw new IllegalArgumentException("endDate has to be last day of a month");
        if (startDate != null && d != null && d.isBefore(startDate))
            throw new IllegalArgumentException("endDate has to be after startDate");
    }

    public TimesheetFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(TimesheetFrequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Double getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(double hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public int getWorkingDaysPerWeek() {
        return workingDaysPerWeek;
    }

    public void setWorkingDaysPerWeek(int workingDaysPerWeek) {
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    public Integer getVacationDaysPerYear() {
        return vacationDaysPerYear;
    }

    public void setVacationDaysPerYear(int vacationDaysPerYear) {
        this.vacationDaysPerYear = vacationDaysPerYear;
    }

    public Employee getEmployee() {
        return employee;
    }

    /**
     * Bidirectional update for setting a new employee.
     * Updates the employee's contract reference.
     */
    public void setEmployee(Employee employee) {
        if (employee == null) throw new IllegalArgumentException("Employee must not be null");
        if (employee.equals(this.employee)) return;
        this.employee = employee;
        employee.setContract(this);
    }

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        if (supervisor == null) throw new IllegalArgumentException("Supervisor must not be null");
        if (supervisor.equals(this.supervisor)) return;
        this.supervisor = supervisor;
        supervisor.setContract(this);
    }

    public List<Secretary> getSecretaries() {
        return Collections.unmodifiableList(secretaries);
    }

    public void setSecretaries(List<Secretary> secretaries) {
        if (secretaries.size() <= 1) throw new IllegalArgumentException("Need at least one Secretary");
        this.secretaries.clear();
        this.secretaries.addAll(secretaries);
        secretaries.forEach(secretary -> secretary.setContract(this));
    }

    public void addSecretary(Secretary secretary) {
        if (secretary == null) return;
        if (secretaries.contains(secretary)) return;
        secretaries.add(secretary);
        secretary.setContract(this);
    }

    public void removeSecretary(Secretary secretary) {
        if (secretary == null) return;
        secretaries.remove(secretary);
    }

    public List<Assistant> getAssistants() {
        return Collections.unmodifiableList(assistants);
    }

    public void setAssistants(List<Assistant> assistants) {
        if (assistants == null) return;
        this.assistants.clear();
        this.assistants.addAll(assistants);
        assistants.forEach(assistant -> assistant.setContract(this));
    }

    public void addAssistant(Assistant assistant) {
        if (assistant == null) return;
        if (assistants.contains(assistant)) return;
        assistants.add(assistant);
        assistant.setContract(this);
    }

    public void removeAssistant(Assistant assistant) {
        if (assistant == null) return;
        assistants.remove(assistant);
    }

    public List<Timesheet> getTimesheets() {
        return Collections.unmodifiableList(timesheets);
    }

    public void setTimesheets(List<Timesheet> timesheets) {
        if (timesheets == null) return;
        this.timesheets.clear();
        this.timesheets.addAll(timesheets);
        timesheets.forEach(timesheet -> timesheet.setContract(this));
    }

    public void addTimesheet(Timesheet timesheet) {
        if (timesheet != null) {
            timesheets.add(timesheet);
            timesheet.setContract(this);
        }
    }

    /**
     * Calculates the total vacation hours for this contract based on working days and hours per week.
     * 
     * @return calculated vacation hours
     */
    public double getVacationHours() {
        return vacationHours();
    }


    public void removeTimesheet(Timesheet timesheet) {
        if (timesheet != null) {
            timesheets.remove(timesheet);
            timesheet.setContract(null); // this removes the reference  
        }
    }
}
