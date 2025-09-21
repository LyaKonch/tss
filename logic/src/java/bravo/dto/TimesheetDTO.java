package bravo.dto;

import bravo.enums.TimesheetStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Transfer Object (DTO) for {@link bravo.entities.Timesheet} information.
 * <p>
 * Extends {@link AbstractDTO} to inherit common DTO properties such as {@code id}, {@code uuid}, and {@code version}.
 * Contains timesheet details, associated contract, entries, and signing information.
 */
public class TimesheetDTO extends AbstractDTO {

    /** Status of the timesheet (e.g., OPEN, SUBMITTED, APPROVED, ARCHIVED). */
    private TimesheetStatus status;

    /** Start date of the timesheet period. */
    private LocalDate startDate;

    /** End date of the timesheet period. */
    private LocalDate endDate;

    /** Total hours due for the timesheet period. */
    private double hoursDue;

    /** Date when the employee signed the timesheet, if applicable. */
    private LocalDate signedByEmployee;

    /** Date when the supervisor signed the timesheet, if applicable. */
    private LocalDate signedBySupervisor;

    /** UUID of the associated contract. */
    private String contractUuid;

    /** List of timesheet entries associated with this timesheet. */
    private List<TimesheetEntryDTO> entries;

    /** List of UUIDs for the timesheet entries. */
    private List<String> timesheetEntryUuids = new ArrayList<>();

    /** Default constructor. */
    public TimesheetDTO() {
        super();
    }

    /**
     * Constructor with full initialization.
     *
     * @param uuid                UUID of the timesheet
     * @param jpaVersion          JPA version number
     * @param contractUuid        UUID of the associated contract
     * @param startDate           start date of the timesheet period
     * @param endDate             end date of the timesheet period
     * @param status              timesheet status
     * @param signedByEmployee    date signed by employee
     * @param signedBySupervisor  date signed by supervisor
     * @param entries             list of associated timesheet entries
     */
    public TimesheetDTO(String uuid, int jpaVersion, String contractUuid, LocalDate startDate, LocalDate endDate, TimesheetStatus status, LocalDate signedByEmployee, LocalDate signedBySupervisor, List<TimesheetEntryDTO> entries) {
        super(uuid, jpaVersion);
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.signedByEmployee = signedByEmployee;
        this.signedBySupervisor = signedBySupervisor;
        this.contractUuid = contractUuid;
        this.entries = entries;
        this.timesheetEntryUuids = entries.stream().map(TimesheetEntryDTO::getUuid).toList();
    }


    // --- Getters and Setters ---

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

    public double getHoursDue() {
        return hoursDue;
    }

    public void setHoursDue(double hoursDue) {
        this.hoursDue = hoursDue;
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

    public String getContractUuid() {
        return contractUuid;
    }

    public void setContractUuid(String contractUuid) {
        this.contractUuid = contractUuid;
    }

    public List<TimesheetEntryDTO> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntryDTO> entries) {
        this.entries = entries;
    }

    public List<String> getTimesheetEntryUuids() {
        return timesheetEntryUuids;
    }

    public void setTimesheetEntryUuids(List<String> timesheetEntryUuids) {
        this.timesheetEntryUuids = timesheetEntryUuids;
    }
}
