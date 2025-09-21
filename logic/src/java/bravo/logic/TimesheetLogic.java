package bravo.logic;

import bravo.dto.ContractDTO;
import bravo.dto.TimesheetDTO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;

import java.time.LocalDate;
import java.util.List;

public interface TimesheetLogic {

    double getReportedHours(TimesheetDTO timesheet);

    void signByEmployee(TimesheetDTO timesheetdto, LocalDate date);

    void revokeEmployeeSignature(TimesheetDTO timesheet);

    void signBySupervisor(TimesheetDTO timesheetdto, LocalDate date);

    void archive(TimesheetDTO timesheet);

    void removeEntry(TimesheetDTO timesheetdto, TimesheetEntryDTO entryDTO);

    void setEntries(Timesheet timesheet, List<TimesheetEntry> newEntries);

    List<TimesheetEntry> getEntries(Timesheet timesheet);

    double hoursDue(String uuid);

    TimesheetDTO getCurrent(ContractDTO contract);

    List<TimesheetDTO> findByContractUuid(String contractUuid);
}
