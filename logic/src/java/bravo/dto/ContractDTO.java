package bravo.dto;

import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for Contract information.
 * This class extends AbstractDTO to inherit common DTO functionality.
 */
public class ContractDTO extends AbstractDTO{

    @Serial
    private static final long serialVersionUID = 1L;

    private ContractStatus status;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private TimesheetFrequency frequency;
    private LocalDate terminationDate;
    private double hoursPerWeek;
    private int workingDaysPerWeek;
    private int vacationDaysPerYear;
    
    private String employeeUuid;

    private String supervisorUuid;

    private List<String> secretaryUuids = new ArrayList<>();

    private List<String> assistantUuids = new ArrayList<>();

    private List<String> timesheetUuids = new ArrayList<>();

    
    public ContractDTO() {
        super();
    }

    public ContractDTO(String uuid, int version) {
        super(uuid,version);
        
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
    
    public double getHoursDue() {
        // TODO: Use calculation as described in requirements
        return workingDaysPerWeek * (hoursPerWeek / 5.0);
    }

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
        this.secretaryUuids = secretaryUuids;
    }

    public List<String> getAssistantUuids() {
        return assistantUuids;
    }

    public void setAssistantUuids(List<String> assistantUuids) {
        this.assistantUuids = assistantUuids;
    }

    public List<String> getTimesheetUuids() {
        return timesheetUuids;
    }

    public void setTimesheetUuids(List<String> timesheetUuids) {
        this.timesheetUuids = timesheetUuids;
    }

    
}
