package bravo.entities;

import bravo.enums.ReportType;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity representing a single entry in a timesheet.
 * Tracks the type of work, start/end times, date, and associated timesheet.
 */
@Entity
@Table(name = "timesheet_entry")
@NamedQueries({
        @NamedQuery(name = "TimesheetEntry.findByUuid",
                query = "SELECT e FROM TimesheetEntry e WHERE e.uuid = :uuid"),
        @NamedQuery(name = "TimesheetEntry.findByTimesheetUuid",
                query = "SELECT e FROM TimesheetEntry e WHERE e.timesheet.uuid = :timesheetUuid ORDER BY e.entryDate DESC"),
        @NamedQuery(name = "TimesheetEntry.findAll",
                query = "SELECT e FROM TimesheetEntry e ORDER BY e.entryDate DESC")
})
public class TimesheetEntry extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    private ReportType type = ReportType.WORK;

    @Column(length = 500)
    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate entryDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "timesheet_id")
    private Timesheet timesheet;

    public TimesheetEntry() {
        super();
    }

    /**
     * Calculates the duration in hours between startTime and endTime.
     *
     * @return the number of hours worked; 0 if times are null or endTime is before startTime
     */
    @Transient
    private double hours() {
        if (startTime == null || endTime == null || endTime.isBefore(startTime)) return 0.0;
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return Math.max(0, minutes) / 60.0;
    }

    /**
     * Returns the report type of the entry.
     *
     * @return the ReportType
     */
    public ReportType getType() {
        return type;
    }

    /**
     * Sets the report type of the entry.
     *
     * @param type the ReportType to set
     */
    public void setType(ReportType type) {
        this.type = type;
    }

    /**
     * Returns the description of the entry.
     *
     * @return the description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the entry.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the start time of the entry.
     *
     * @return the startTime
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the entry.
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the end time of the entry.
     *
     * @return the endTime
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the entry.
     *
     * @param endTime the endTime to set
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the date of the entry.
     *
     * @return the entryDate
     */
    public LocalDate getEntryDate() {
        return entryDate;
    }

    /**
     * Sets the date of the entry.
     *
     * @param entryDate the entryDate to set
     */
    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    /**
     * Returns the timesheet associated with this entry.
     *
     * @return the Timesheet entity
     */
    public Timesheet getTimesheet() {
        return timesheet;
    }

    /**
     * Sets the timesheet associated with this entry.
     *
     * @param timesheet the Timesheet to set
     */
    public void setTimesheet(Timesheet timesheet) {
        this.timesheet = timesheet;
    }

    /**
     * Returns the calculated hours of the entry.
     *
     * @return hours worked as a double
     */
    public double getHours() {
        return hours();
    }
}