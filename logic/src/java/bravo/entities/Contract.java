package bravo.entities;

import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private int version;

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.PREPARED;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TimesheetFrequency frequency = TimesheetFrequency.MONTHLY;

    private LocalDate terminationDate;

    @Positive
    private double hoursPerWeek;

    @Column(nullable = false)
    @Min(1)
    @Max(7)
    private int workingDaysPerWeek = 5;

    @Column(nullable = false)
    @Min(0)
    @Max(365)
    private int vacationDaysPerYear = 20;

//    // Contract 1 < --- > 1 Employee
//    @OneToOne(optional = false)
//    @JoinColumn(name = "employee_id")
//    private Employee employee;
//
//    // Contract 1 < --- > 1 Supervisor
//    @OneToOne(optional = false)
//    @JoinColumn(name = "supervisor_id")
//    private Supervisor supervisor;
//
//    // Contract 1 < --- > 1..* Secretary
//    @NotNull
//    @Size(min = 1, message = "Mindestens eine Secretary erforderlich")
//    @OneToMany
//    @JoinTable(name = "contract_secretary", joinColumns = @JoinColumn(name = "contract_id"), inverseJoinColumns = @JoinColumn(name = "secretary_id"))
//    private List<Secretary> secretaries = new ArrayList<>();
//
//    // Contract 1 < --- > 0..* Assistant
//    @OneToMany
//    @JoinTable(name = "contract_assistant", joinColumns = @JoinColumn(name = "contract_id"), inverseJoinColumns = @JoinColumn(name = "assistant_id"))
//    private List<Assistant> assistants = new ArrayList<>();
//
//    // Contract 1 < --- > * Timesheet
//    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Timesheet> timesheets = new ArrayList<>();

    @Transient
    private double getVacationHours() {
        int months = getDurationInWholeMonths();
        if (months <= 0 || hoursPerWeek <= 0) return 0.0;
        return vacationDaysPerYear * (months / 12.0) * (hoursPerWeek / workingDaysPerWeek);
    }

//    @Transient
//    private double getHoursDue() {
//        return timesheets.stream().mapToDouble(Timesheet::getHoursDue).sum();
//    }


    public Contract() {
    }


    private int getDurationInWholeMonths() {
        if (startDate == null || endDate == null) return 0;
        return (int) ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1)) + 1;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

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

    public int getWorkingDaysPerWeek() {
        return workingDaysPerWeek;
    }

    public void setWorkingDaysPerWeek(int workingDaysPerWeek) {
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    public int getVacationDaysPerYear() {
        return vacationDaysPerYear;
    }

    public void setVacationDaysPerYear(int vacationDaysPerYear) {
        this.vacationDaysPerYear = vacationDaysPerYear;
    }

//    public Employee getEmployee() {
//        return employee;
//    }
//
//    public void setEmployee(Employee employee) {
//        this.employee = employee;
//    }
//
//    public Supervisor getSupervisor() {
//        return supervisor;
//    }
//
//    public void setSupervisor(Supervisor supervisor) {
//        this.supervisor = supervisor;
//    }
//
//    public List<Secretary> getSecretaries() {
//        return java.util.Collections.unmodifiableList(secretaries);
//    }
//
//    public void setSecretaries(List<Secretary> secretaries) {
//        this.secretaries.clear();
//        if (secretaries != null) this.secretaries.addAll(secretaries);
//    }
//
//    public void addSecretary(Secretary s) {
//        if (s != null) secretaries.add(s);
//    }
//
//    public void removeSecretary(Secretary s) {
//        secretaries.remove(s);
//    }
//
//    public List<Assistant> getAssistants() {
//        return java.util.Collections.unmodifiableList(assistants);
//    }
//
//    public void setAssistants(List<Assistant> assistants) {
//        this.assistants.clear();
//        if (assistants != null) this.assistants.addAll(assistants);
//    }
//
//    public void addAssistant(Assistant a) {
//        if (a != null) assistants.add(a);
//    }
//
//    public void removeAssistant(Assistant a) {
//        assistants.remove(a);
//    }
//
//    public List<Timesheet> getTimesheets() {
//        return timesheets;
//    }
//
//    public void setTimesheets(List<Timesheet> timesheets) {
//        this.timesheets = timesheets;
//    }
}
