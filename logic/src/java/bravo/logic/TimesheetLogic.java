package bravo.logic;

import bravo.entities.Contract;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;
import java.time.LocalDate;
import java.util.List;

public interface TimesheetLogic {

    double getReportedHours(Timesheet timesheet);

    void signByEmployee(Timesheet timesheet, LocalDate date);

    void revokeEmployeeSignature(Timesheet timesheet);

    void signBySupervisor(Timesheet timesheet, LocalDate date);

    void archive(Timesheet timesheet);

    void addEntry(Timesheet timesheet, TimesheetEntry e);

    void removeEntry(Timesheet timesheet, TimesheetEntry e);

    void setEntries(Timesheet timesheet, List<TimesheetEntry> newEntries);

    List<TimesheetEntry> getEntries(Timesheet timesheet);

}
