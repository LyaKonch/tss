package bravo.dto;

import bravo.enums.ReportType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@XmlRootElement
public class TimesheetEntryDTO extends AbstractDTO {

    private ReportType type;

    private String description;

    private double hours;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate entryDate;

    private String timesheetUuid;


    public TimesheetEntryDTO() {
        super();
    }

    public TimesheetEntryDTO(ReportType type, String description, LocalTime startTime, LocalTime endTime, LocalDate entryDate, UUID timesheetUuid) {
        super();
        this.type = type;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entryDate = entryDate;
        this.timesheetUuid = timesheetUuid.toString();
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
