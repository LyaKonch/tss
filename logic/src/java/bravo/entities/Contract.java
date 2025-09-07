package bravo.entities;

import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import bravo.logic.TimesheetLogic;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
@NamedQueries({
        @NamedQuery(name = "Contract.findByName",
                query = "SELECT c FROM Contract c WHERE c.name = :name"),
        @NamedQuery(name = "Contract.findAll",
                query = "SELECT c FROM Contract c ORDER BY c.startDate DESC")
})
public class Contract extends AbstractEntity {

    // Assistants 0..*
    @OneToMany(mappedBy = "contract", cascade = CascadeType.PERSIST)
    private List<Assistant> assistants = new ArrayList<>();

    private int WORKINGDAYSPERWEEK = 5;

    private int VACATIONDAYSPERYEAR = 20;

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.PREPARED;

    @NotBlank
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private TimesheetFrequency frequency = TimesheetFrequency.MONTHLY;

    private LocalDate terminationDate;

    @Positive
    private Double hoursPerWeek;

    // Genau 1
    @OneToOne(mappedBy = "contract", optional = false)
    private Employee employee;

    @OneToOne(mappedBy = "contract", optional = false)
    private Supervisor supervisor;

    // mindestens 1
    @NotNull
    @Size(min = 1, message = "Every Contract needs at least one Secretary.")
    @OneToMany(mappedBy = "contract")
    private List<Secretary> secretaries = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Timesheet> timesheets = new ArrayList<>();

    public Contract() {
        super();
    }

    public Contract(boolean newEntity) {
        super(newEntity);
    }

    public int getWorkingDaysPerWeek() {
        return WORKINGDAYSPERWEEK;
    }

    public int getVacationDaysPerYear() {
        return VACATIONDAYSPERYEAR;
    }

    // Berechnet
    @Transient
    public double getVacationHours() {
        int months = getDurationInWholeMonths();
        if (months <= 0 || hoursPerWeek <= 0) return 0.0;
        return VACATIONDAYSPERYEAR * (months / 12.0) * (hoursPerWeek / WORKINGDAYSPERWEEK);
    }

    @Transient
    public double getHoursDue(TimesheetLogic timesheetLogic) {
        return timesheets.stream().mapToDouble(timesheet -> timesheetLogic.getHoursDue(timesheet)).sum();
    }

    //Hilfsmethode
    private int getDurationInWholeMonths() {
        if (startDate == null || endDate == null) return 0;
        return (int) ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1),
                endDate.withDayOfMonth(1)
        ) + 1;
    }

    // Getter&Setter

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    //
    public void setStartDate(LocalDate d) {
        this.startDate = d;
        if (d != null && d.getDayOfMonth() != 1)
            throw new IllegalArgumentException("startDate muss der 1. des Monats sein");
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate d) {
        this.endDate = d;
        if (d != null && d.plusDays(1).getDayOfMonth() != 1)
            throw new IllegalArgumentException("endDate muss letzter Tag des Monats sein");
        if (startDate != null && d != null && d.isBefore(startDate))
            throw new IllegalArgumentException("endDate darf nicht vor startDate liegen");
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

    public double getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(double hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public List<Secretary> getSecretaries() {
        return java.util.Collections.unmodifiableList(secretaries);
    }

    public void setSecretaries(List<Secretary> secretaries) {
        this.secretaries.clear();
        if (secretaries != null) this.secretaries.addAll(secretaries);
    }

    public void addSecretary(Secretary s) {
        if (s != null) secretaries.add(s);
    }

    public void removeSecretary(Secretary s) {
        secretaries.remove(s);
    }

    public List<Assistant> getAssistants() {
        return java.util.Collections.unmodifiableList(assistants);
    }

    public void setAssistants(List<Assistant> assistants) {
        this.assistants.clear();
        if (assistants != null) this.assistants.addAll(assistants);
    }

    public void addAssistant(Assistant a) {
        if (a != null) assistants.add(a);
    }

    public void removeAssistant(Assistant a) {
        assistants.remove(a);
    }

    public List<Timesheet> getTimesheets() {
        return timesheets;
    }

    public void setTimesheets(List<Timesheet> timesheets) {
        this.timesheets = timesheets;
    }
}
