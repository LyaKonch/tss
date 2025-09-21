package bravo.dto;

import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for {@link bravo.entities.Contract} information.
 * <p>
 * Extends {@link AbstractDTO} to inherit common DTO properties such as {@code id}, {@code uuid}, and {@code version}.
 * Includes fields for contract details, associated roles, and related timesheets.
 */
public class ContractDTO extends AbstractDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Status of the contract (e.g., ACTIVE, TERMINATED).
     */
    private ContractStatus status;

    /**
     * Name of the contract.
     */
    private String name;

    /**
     * Contract start date.
     */
    private LocalDate startDate;

    /**
     * Contract end date.
     */
    private LocalDate endDate;

    /**
     * Timesheet frequency (e.g., WEEKLY, MONTHLY).
     */
    private TimesheetFrequency frequency;

    /**
     * Optional termination date of the contract.
     */
    private LocalDate terminationDate;

    private Integer archivationDuration;
    
    /**
     * Number of hours worked per week.
     */
    private Double hoursPerWeek;

    /**
     * Number of working days per week.
     */
    private Integer workingDaysPerWeek;

    /**
     * Number of vacation days per year.
     */
    private Integer vacationDaysPerYear;

    /**
     * UUID of the employee associated with this contract.
     */
    @NotBlank
    private String employeeUuid;

    /**
     * UUID of the supervisor associated with this contract.
     */
    @NotBlank
    private String supervisorUuid;

    /**
     * UUIDs of secretaries associated with this contract. Must contain at least one.
     */
    @Size(min = 1)
    private List<String> secretaryUuids = new ArrayList<>();

    /**
     * UUIDs of assistants associated with this contract.
     */
    private List<String> assistantUuids = new ArrayList<>();

    /**
     * UUIDs of timesheets associated with this contract.
     */
    private List<String> timesheetUuids = new ArrayList<>();


    /**
     * Default constructor.
     */
    public ContractDTO() {
        super();
    }

    /**
     * Constructor with UUID and version.
     *
     * @param uuid    the UUID of the contract
     * @param version the version number
     */
    public ContractDTO(String uuid, int version) {
        super(uuid, version);

    }

    @Override
    public String toString() {
        return "ContractDTO {\n" +
                "  status=" + status + ",\n" +
                "  name=" + name + ",\n" +
                "  startDate=" + startDate + ",\n" +
                "  endDate=" + endDate + ",\n" +
                "  frequency=" + frequency + ",\n" +
                "  terminationDate=" + terminationDate + ",\n" +
                "  hoursPerWeek=" + hoursPerWeek + ",\n" +
                "  workingDaysPerWeek=" + workingDaysPerWeek + ",\n" +
                "  vacationDaysPerYear=" + vacationDaysPerYear + ",\n" +
                "  employeeUuid=" + employeeUuid + ",\n" +
                "  supervisorUuid=" + supervisorUuid + ",\n" +
                "  secretaryUuids=" + secretaryUuids + ",\n" +
                "  assistantUuids=" + assistantUuids + ",\n" +
                "  timesheetUuids=" + timesheetUuids + "\n" +
                "}";
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

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public void setHoursPerWeek(Double hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public Integer getWorkingDaysPerWeek() {
        return workingDaysPerWeek;
    }

    public void setWorkingDaysPerWeek(Integer workingDaysPerWeek) {
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    public Integer getVacationDaysPerYear() {
        return vacationDaysPerYear;
    }

    public void setVacationDaysPerYear(Integer vacationDaysPerYear) {
        this.vacationDaysPerYear = vacationDaysPerYear;
    }

    /**
     * Calculates the weekly hours due based on working days and hours per week.
     *
     * @return calculated hours due
     */
    public double getHoursDue() {
        // TODO: Use calculation as described in requirements
        return workingDaysPerWeek * (hoursPerWeek / 5.0);
    }

    /**
     * Calculates vacation hours based on vacation days per year and hours per week.
     *
     * @return calculated vacation hours
     */
    public double getVacationHours() {
        return vacationDaysPerYear * (hoursPerWeek / 5.0);
    }

    public String getEmployeeUuid() {
        return employeeUuid;
    }

    public void setEmployeeUuid(String employeeUuid) {
        this.employeeUuid = employeeUuid;
    }

    public String getSupervisorUuid() {
        return supervisorUuid;
    }

    public void setSupervisorUuid(String supervisorUuid) {
        this.supervisorUuid = supervisorUuid;
    }

    public List<String> getSecretaryUuids() {
        return secretaryUuids;
    }

    public void setSecretaryUuids(List<String> secretaryUuids) {
        this.secretaryUuids.clear();
        if (secretaryUuids == null) return;
        this.secretaryUuids.addAll(secretaryUuids);
    }

    public List<String> getAssistantUuids() {
        return assistantUuids;
    }

    public void setAssistantUuids(List<String> assistantUuids) {
        this.assistantUuids.clear();
        if (assistantUuids == null) return;
        this.assistantUuids.addAll(assistantUuids);
    }

    public List<String> getTimesheetUuids() {
        return timesheetUuids;
    }

    public void setTimesheetUuids(List<String> timesheetUuids) {
        this.timesheetUuids.clear();
        if (timesheetUuids == null) return;
        this.timesheetUuids.addAll(timesheetUuids);
    }

    public Integer getArchivationDuration() {
        return archivationDuration;
    }

    public void setArchivationDuration(Integer archivationDuration) {
        this.archivationDuration = archivationDuration;
    }
}
