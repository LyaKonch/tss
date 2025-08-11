package bravo.entities;

import bravo.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contract")
public class Contract {

    // Fixwerte
    public static final int WORKINGDAYS = 5;
    public static final int VACATIONDAYS = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private double hoursPerWeek;

    // Personen-Beziehungen
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supervisor_id")
    private Supervisor supervisor;

    // Secretaries: mindestens 1
    @NotNull
    @Size(min = 1, message = "Mindestens eine Secretary erforderlich")
    @ManyToMany
    @JoinTable(name = "contract_secretary",
            joinColumns = @JoinColumn(name = "contract_id"),
            inverseJoinColumns = @JoinColumn(name = "secretary_id"))
    private final List<Secretary> secretaries = new ArrayList<>();///ich weiss nicht ob richtig final ist

    // Assistants: 0..*
    @NotNull
    @ManyToMany
    @JoinTable(name = "contract_assistant",
            joinColumns = @JoinColumn(name = "contract_id"),
            inverseJoinColumns = @JoinColumn(name = "assistant_id"))
    private final List<Assistant> assistants = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Timesheet> timesheets = new ArrayList<>();

    // ===== Fixwerte als nicht-persistente Getter =====
    @Transient
    public int getWorkingDaysPerWeek() {
        return WORKINGDAYS;
    }

    @Transient
    public int getVacationDaysPerYear() {
        return VACATIONDAYS;
    }

    // ===== Berechnete Felder ("/") =====
    @Transient
    public double getVacationHours() {
        int months = getDurationInWholeMonths();
        if (months <= 0 || hoursPerWeek <= 0) return 0.0;
        // Urlaubstage/Jahr * (Monate/12) * (Stunden/Woche / Arbeitstage/Woche)
        return VACATIONDAYS * (months / 12.0) * (hoursPerWeek / WORKINGDAYS);
    }

    @Transient
    public double getHoursDue() {
        return timesheets.stream().mapToDouble(Timesheet::getHoursDue).sum();
    }

    // ===== Hilfsmethoden =====
    private int getDurationInWholeMonths() {
        if (startDate == null || endDate == null) return 0;
        return (int) ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1),
                endDate.withDayOfMonth(1)
        ) + 1; // Endmonat inklusiv
    }

    // ===== Monatsgrenzen in Settern prüfen =====
    public void setStartDate(LocalDate d) {
        this.startDate = d;
        if (d != null && d.getDayOfMonth() != 1)
            throw new IllegalArgumentException("startDate muss der 1. des Monats sein");
    }

    public void setEndDate(LocalDate d) {
        this.endDate = d;
        if (d != null && d.plusDays(1).getDayOfMonth() != 1)
            throw new IllegalArgumentException("endDate muss letzter Tag des Monats sein");
        if (startDate != null && d != null && d.isBefore(startDate))
            throw new IllegalArgumentException("endDate darf nicht vor startDate liegen");
    }

    // ===== Getter/Setter trivial =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public TimesheetFrequency getFrequency() { return frequency; }
    public void setFrequency(TimesheetFrequency frequency) { this.frequency = frequency; }

    public LocalDate getTerminationDate() { return terminationDate; }
    public void setTerminationDate(LocalDate terminationDate) { this.terminationDate = terminationDate; }

    public double getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(double hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Supervisor getSupervisor() { return supervisor; }
    public void setSupervisor(Supervisor supervisor) { this.supervisor = supervisor; }

    public List<Secretary> getSecretaries() {
        return java.util.Collections.unmodifiableList(secretaries);
    }
    public void setSecretaries(List<Secretary> secretaries) {
        this.secretaries.clear();
        if (secretaries != null) this.secretaries.addAll(secretaries);
    }
    public void addSecretary(Secretary s) { if (s != null) secretaries.add(s); }
    public void removeSecretary(Secretary s) { secretaries.remove(s); }

    public List<Assistant> getAssistants() {
        return java.util.Collections.unmodifiableList(assistants);
    }
    public void setAssistants(List<Assistant> assistants) {
        this.assistants.clear();
        if (assistants != null) this.assistants.addAll(assistants);
    }
    public void addAssistant(Assistant a) { if (a != null) assistants.add(a); }
    public void removeAssistant(Assistant a) { assistants.remove(a); }

    public List<Timesheet> getTimesheets() { return timesheets; }
    public void setTimesheets(List<Timesheet> timesheets) { this.timesheets = timesheets; }
}
