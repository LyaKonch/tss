package bravo.dto;

import bravo.enums.ReportType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for {@link bravo.entities.TimesheetEntry} information.
 * <p>
 * Extends {@link AbstractDTO} to inherit common DTO properties such as {@code id}, {@code uuid}, and {@code version}.
 * Represents a single entry in a timesheet, including reporting type, hours, times, and associated timesheet.
 */
@XmlRootElement
public class TimesheetEntryDTO extends AbstractDTO {

    /**
     * Type of report for this entry (e.g., WORK, VACATION, SICK).
     */
    private ReportType type;

    /**
     * Description of the timesheet entry.
     */
    private String description;

    /**
     * Number of hours recorded for this entry.
     */
    private double hours;

    /**
     * Start time of the entry.
     */
    private LocalTime startTime;

    /**
     * End time of the entry.
     */
    private LocalTime endTime;

    /**
     * Date when this entry was recorded.
     */
    private LocalDate entryDate;

    /**
     * UUID of the associated timesheet.
     */
    private String timesheetUuid;


    /**
     * Default constructor.
     */
    public TimesheetEntryDTO() {
        super();
    }

    /**
     * Constructor with full initialization.
     *
     * @param id            database ID of the entry
     * @param uuid          UUID of the entry
     * @param type          type of report
     * @param description   description of the entry
     * @param startTime     start time
     * @param endTime       end time
     * @param entryDate     date of the entry
     * @param timesheetUuid UUID of the associated timesheet
     */
    public TimesheetEntryDTO(Long id, UUID uuid, ReportType type, String description, LocalTime startTime, LocalTime endTime, LocalDate entryDate, UUID timesheetUuid) {
        super();
        this.setId(id);
        this.setUuid(uuid.toString());
        this.type = type;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entryDate = entryDate;
        this.timesheetUuid = timesheetUuid.toString();
    }

    // --- Getters and Setters ---
    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getTimesheetUuid() {
        return timesheetUuid;
    }

    public void setTimesheetUuid(String timesheetUuid) {
        this.timesheetUuid = timesheetUuid;
    }
}
