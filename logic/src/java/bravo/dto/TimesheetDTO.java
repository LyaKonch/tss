package bravo.dto;

import bravo.enums.TimesheetStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TimesheetDTO extends AbstractDTO {

    private TimesheetStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private double hoursDue;

    private LocalDate signedByEmployee;

    private LocalDate signedBySupervisor;

    private List<String> timesheetEntryUuids = new ArrayList<>();

    public TimesheetDTO() {
        super();
    }

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

    public List<String> getTimesheetEntryUuids() {
        return timesheetEntryUuids;
    }

    public void setTimesheetEntryUuids(List<String> timesheetEntryUuids) {
        this.timesheetEntryUuids = timesheetEntryUuids;
    }
}
