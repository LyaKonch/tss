package bravo.entities;

import bravo.enums.ReportType;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "timesheet_entry")
@NamedQueries({
    @NamedQuery(name = "TimesheetEntry.findByUuid", query = "SELECT e FROM TimesheetEntry e WHERE e.uuid = :uuid"),
    @NamedQuery(name = "TimesheetEntry.findByTimesheetUuid", query = "SELECT e FROM TimesheetEntry e WHERE e.timesheet.uuid = :timesheetUuid"),
    @NamedQuery(name = "TimesheetEntry.findAll", query = "SELECT e FROM TimesheetEntry e ORDER BY e.entryDate DESC")})
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

    public TimesheetEntry(boolean NewEntity) {
        super(NewEntity);
    }

    @Transient
    private double hours() {
        if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
            return 0.0;
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return Math.max(0, minutes) / 60.0;
    }

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

    public Timesheet getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(Timesheet timesheet) {
        this.timesheet = timesheet;
    }

    public double getHours() {
        return hours();
    }

}
