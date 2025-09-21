package bravo.logic;

import bravo.dto.ContractDTO;
import bravo.dto.TimesheetDTO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;

import java.time.LocalDate;
import java.util.List;

public interface TimesheetLogic {

    /**
     * Calculates the total reported hours for the given timesheet.
     *
     * @param timesheet the timesheet DTO
     * @return the total reported hours
     */
    double getReportedHours(TimesheetDTO timesheet);

    /**
     * Signs the timesheet by the employee on the specified date.
     *
     * @param timesheetdto the timesheet DTO
     * @param date the date of signing
     */
    void signByEmployee(TimesheetDTO timesheetdto, LocalDate date);

    /**
     * Revokes the employee's signature from the timesheet.
     *
     * @param timesheet the timesheet DTO
     */
    void revokeEmployeeSignature(TimesheetDTO timesheet);

    /**
     * Signs the timesheet by the supervisor on the specified date.
     *
     * @param timesheetdto the timesheet DTO
     * @param date the date of signing
     */
    void signBySupervisor(TimesheetDTO timesheetdto, LocalDate date);

    /**
     * Archives the given timesheet.
     *
     * @param timesheet the timesheet DTO to archive
     */
    void archive(TimesheetDTO timesheet);

    /**
     * Adds an entry to the given timesheet entity.
     *
     * @param timesheet the timesheet entity
     * @param e the timesheet entry to add
     */
    void addEntry(Timesheet timesheet, TimesheetEntry e);

    /**
     * Removes a timesheet entry from the given timesheet DTO.
     *
     * @param timesheetdto the timesheet DTO
     * @param entryDTO the entry DTO to remove
     */
    void removeEntry(TimesheetDTO timesheetdto, TimesheetEntryDTO entryDTO);

    /**
     * Replaces all entries in the given timesheet with a new list of entries.
     *
     * @param timesheet the timesheet entity
     * @param newEntries the new list of entries
     */
    void setEntries(Timesheet timesheet, List<TimesheetEntry> newEntries);

    /**
     * Retrieves all entries for the given timesheet entity.
     *
     * @param timesheet the timesheet entity
     * @return list of timesheet entries
     */
    List<TimesheetEntry> getEntries(Timesheet timesheet);

    /**
     * Calculates the hours due for a timesheet identified by its UUID.
     *
     * @param uuid the UUID of the timesheet
     * @return the hours due
     */
    double hoursDue(String uuid);

    /**
     * Retrieves the current timesheet for the given contract.
     *
     * @param contract the contract DTO
     * @return the current timesheet DTO
     */
    TimesheetDTO getCurrent(ContractDTO contract);

    /**
     * Finds all timesheets associated with a specific contract UUID.
     *
     * @param contractUuid the contract UUID
     * @return list of timesheet DTOs
     */
    List<TimesheetDTO> findByContractUuid(String contractUuid);
}