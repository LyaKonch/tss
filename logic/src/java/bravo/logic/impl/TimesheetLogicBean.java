package bravo.logic.impl;

import bravo.dao.TimesheetDAO;
import bravo.dao.TimesheetEntryDAO;
import bravo.dto.ContractDTO;
import bravo.dto.TimesheetDTO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;
import bravo.enums.ContractStatus;
import bravo.enums.FederalState;
import bravo.enums.TimesheetStatus;
import bravo.logic.ContractLogic;
import bravo.logic.TimesheetLogic;
import bravo.mapper.TimesheetEntryMapper;
import bravo.mapper.TimesheetMapper;
import bravo.utitity.AuthorizationService;
import bravo.utitity.PublicHolidayService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Stateless EJB implementing {@link TimesheetLogic}.
 * <p>
 * Provides business logic for managing timesheets and timesheet entries,
 * including creation, modification, signing, archiving, and calculation of reported hours.
 * Handles validation of editable states and enforces business rules for timesheet lifecycle.
 */
@Stateless
public class TimesheetLogicBean implements TimesheetLogic {

    private static final Logger logger = Logger.getLogger(TimesheetLogicBean.class.getName());

    @Inject
    TimesheetEntryMapper timesheetEntryMapper;

    @Inject
    private AuthorizationService authService;

    @EJB
    private PublicHolidayService holidayChecker;

    @Inject
    private ContractLogic contractLogic;

    @EJB
    private TimesheetEntryDAO entryDAO;

    @EJB
    private TimesheetDAO timesheetDAO;

    @Inject
    private TimesheetMapper timesheetMapper;

    /**
     * Creates a new timesheet entry for the corresponding timesheet.
     * Validates that the entry date is within the timesheet period.
     *
     * @param entryDto DTO containing entry data
     */
    public void createNewEntry(TimesheetEntryDTO entryDto) {
        Timesheet timesheet = timesheetDAO.findByUuid(entryDto.getUuid());
        if (!authService.canEditTimesheetEntry(timesheet)) {
            throw new SecurityException("User is not authorized to add entries to this timesheet.");
        }
        if (timesheet == null) {
            throw new IllegalArgumentException("Timesheet with" + entryDto.getUuid() + "is not found");
        }

        // true means a new object, it's supposed to generate new UUID for new objects;
        TimesheetEntry newEntry = new TimesheetEntry();
        if (entryDto.getEntryDate().isBefore(timesheet.getEndDate())
                || entryDto.getEntryDate().equals(timesheet.getEndDate())
                || entryDto.getEntryDate().isAfter(timesheet.getStartDate())
                || entryDto.getEntryDate().equals(timesheet.getStartDate())) {
            newEntry.setEntryDate(entryDto.getEntryDate());
        } else {
            throw new IllegalArgumentException("Entry Date for the Timesheet with" + entryDto.getUuid() + " should be in within the start and end time of the timesheet");
        }
        newEntry.setStartTime(entryDto.getStartTime());
        newEntry.setEndTime(entryDto.getEndTime());
        newEntry.setType(entryDto.getType());
        newEntry.setDescription(entryDto.getDescription());
        addEntry(timesheet, newEntry);

    }

    /**
     * Checks whether the timesheet has been signed by the employee.
     *
     * @param timesheet the timesheet entity
     * @return true if signed by employee, false otherwise
     */
    public boolean isSignedByEmployee(Timesheet timesheet) {
        return timesheet.getSignedByEmployee() != null;
    }

    /**
     * Adds a timesheet entry to a timesheet.
     *
     * @param timesheet the timesheet entity
     * @param e         the timesheet entry to add
     */
    public void addEntry(Timesheet timesheet, TimesheetEntry e) {

        requireEditable(timesheet);
        if (e == null) {
            logger.warning("Attempted to add null entry to timesheet " + timesheet.getUuid());
            return;
        }
        e.setTimesheet(timesheet);
        timesheet.getEntries().add(e);
        entryDAO.save(e);
        timesheetDAO.save(timesheet);
    }

    /**
     * Removes a timesheet entry from a timesheet.
     *
     * @param timesheetdto the timesheet DTO
     * @param entryDTO     the entry DTO to remove
     */
    @Override
    public void removeEntry(TimesheetDTO timesheetdto, TimesheetEntryDTO entryDTO) {
        Timesheet timesheet = timesheetDAO.findByUuid(timesheetdto.getUuid());
        if (!authService.canEditTimesheetEntry(timesheet)) {
            throw new SecurityException("User is not authorized to remove entries from this timesheet.");
        }
        TimesheetEntry e = timesheetEntryMapper.toEntity(entryDTO);
        requireEditable(timesheet);
        if (e == null) {
            logger.warning("Attempted to add null entry to timesheet " + timesheet.getUuid());
            return;
        }
        timesheet.getEntries().remove(e);
        e.setTimesheet(null);
        entryDAO.save(e);
        timesheetDAO.save(timesheet);
    }
    /**
     * Signs a timesheet by the employee.
     *
     * @param timesheetdto the timesheet DTO
     * @param date         the signing date; if null, uses current date
     */
    @Override
    public void signByEmployee(TimesheetDTO timesheetdto, LocalDate date) {
        Timesheet timesheet = timesheetDAO.findByUuid(timesheetdto.getUuid());
        if (!authService.canSignTimesheetAsEmployee(timesheet.getContract())) {
            throw new SecurityException("User not authorized to sign as Employee for this contract.");
        }
        requireStatus(timesheet, TimesheetStatus.IN_PROGRESS);
        timesheet.setSignedByEmployee((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheetDAO.save(timesheet);
    }

    /**
     * Revokes an employee's signature on a timesheet.
     *
     * @param timesheetdto the timesheet DTO
     */
    @Override
    public void revokeEmployeeSignature(TimesheetDTO timesheetdto) {
        Timesheet timesheet = timesheetDAO.findByUuid(timesheetdto.getUuid());
        if (!authService.canSignTimesheetAsEmployee(timesheet.getContract())) {
            throw new SecurityException("User not authorized to revoke as Employee for this contract.");
        }
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        timesheetDAO.save(timesheet);
    }

    /**
     * Signs a timesheet by the supervisor.
     *
     * @param timesheetdto the timesheet DTO
     * @param date         the signing date; if null, uses current date
     */
    @Override
    public void signBySupervisor(TimesheetDTO timesheetdto, LocalDate date) {
        Timesheet timesheet = timesheetDAO.findByUuid(timesheetdto.getUuid());
        if (!authService.canSignTimesheetAsSupervisor(timesheet.getContract())) {
            throw new SecurityException("User not authorized to sign as Supervisor for this contract.");
        }
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedBySupervisor((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        timesheetDAO.save(timesheet);
    }

    /**
     * Archives a timesheet after supervisor approval.
     *
     * @param timesheetdto the timesheet DTO
     */
    @Override
    public void archive(TimesheetDTO timesheetdto) {
        Timesheet timesheet = timesheetDAO.findByUuid(timesheetdto.getUuid());
        // if the person is secretary to the contract than he can archive all timesheets.
        if (!authService.canArchiveContract(timesheet.getContract())) {
            throw new SecurityException("User not authorized to archive this timesheet.");
        }
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_SUPERVISOR);
        timesheet.setStatus(TimesheetStatus.ARCHIVED);
        timesheetDAO.save(timesheet);
        if (timesheet.getContract().getTimesheets().stream().allMatch(ts -> ts.getStatus() == TimesheetStatus.ARCHIVED)) {
            contractLogic.archiveContract(timesheet.getContract().getUuid().toString());
        }
    }

    /**
     * Returns the total reported hours of a timesheet.
     *
     * @param timesheetdto the timesheet DTO
     * @return the sum of all timesheet entry hours
     */
    @Override
    public double getReportedHours(Timesheet timesheet) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        return timesheet.getEntries().stream().mapToDouble(TimesheetEntry::getHours).sum();
    }


    /**
     * Returns the list of entries for a timesheet.
     *
     * @param timesheet the timesheet entity
     * @return an unmodifiable list of timesheet entries
     */
    @Override
    public List<TimesheetEntry> getEntries(Timesheet timesheet) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        return Collections.unmodifiableList(timesheet.getEntries());
    }

    /**
     * Retrieves the current timesheet for a contract and calculates hours due.
     *
     * @param contract the contract DTO
     * @return current timesheet DTO with hours due set
     */
    @Override
    public TimesheetDTO getCurrent(ContractDTO contract) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        if (timesheetDAO.findCurrent(contract.getUuid()) == null) return null;
        Timesheet entity = timesheetDAO.findCurrent(contract.getUuid());
        TimesheetDTO dto = timesheetMapper.toDto(entity);
        dto.setHoursDue(hoursDue(dto.getUuid()));
        return dto;
    }

    /**
     * Finds all timesheets for a given contract UUID.
     *
     * @param contractUuid the contract UUID
     * @return list of timesheet DTOs
     */
    @Override
    public List<TimesheetDTO> findByContractUuid(String contractUuid) {
        return timesheetDAO.findByContractUuid(contractUuid).stream().map(timesheetMapper::toDto).toList();
    }

    /**
     * Replaces all entries in a timesheet with a new list of entries.
     *
     * @param timesheet  the timesheet entity
     * @param newEntries the new list of timesheet entries
     */
    @Override
    public void setEntries(Timesheet timesheet, List<TimesheetEntry> newEntries) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        requireEditable(timesheet);
        timesheet.getEntries().clear();
        if (newEntries != null) {
            newEntries.forEach(e -> {
                e.setTimesheet(timesheet);
                timesheet.getEntries().add(e);
            });
        }
        timesheetDAO.save(timesheet);
    }

    /**
     * Calculates the hours due for a timesheet based on working days and holidays.
     *
     * @param uuid timesheet UUID
     * @return hours due
     */
    public double hoursDue(String uuid) {
        Timesheet timesheet = timesheetDAO.findByUuid(uuid);
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        FederalState state = timesheet.getContract().getEmployee().getPerson().getState();
        if (timesheet.getContract() == null || timesheet.getStartDate() == null || timesheet.getEndDate() == null) {
            return 0.0;
        }
        int working = countWorkingDays(timesheet.getStartDate(), timesheet.getEndDate());
        int holidays = countHolidaysOnWorkingDays(timesheet.getStartDate(), timesheet.getEndDate(), state);
        int effectiveDays = Math.max(0, working - holidays);
        return effectiveDays * (timesheet.getContract().getHoursPerWeek() / timesheet.getContract().getWorkingDaysPerWeek());
    }

    /**
     * Counts working days (Mon-Fri) between two dates.
     *
     * @param start start date
     * @param end   end date
     * @return number of working days
     */
    public int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

    /**
     * Counts holidays that fall on working days for a given federal state.
     *
     * @param start start date
     * @param end   end date
     * @param state federal state
     * @return number of holidays on working days
     */
    public int countHolidaysOnWorkingDays(LocalDate start, LocalDate end, FederalState state) {
        if (holidayChecker == null) return 0;
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            boolean workday = (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY);
            if (workday && holidayChecker.isHoliday(dat, state)) count++;
        }
        return count;
    }

    // --- Private helper methods ---

    private void requireStatus(Timesheet timesheet, TimesheetStatus expected) {
        if (timesheet.getStatus() != expected) {
            throw new IllegalStateException("Aktion nur im Status " + expected + " erlaubt.");
        }
    }

    private void requireEditable(Timesheet timesheet) {
        // TS2 MIN
        if (timesheet.getStatus() == TimesheetStatus.ARCHIVED) {
            throw new IllegalStateException("Archived timesheets cannot be changed.");
        }
        if (timesheet.getStatus() != TimesheetStatus.IN_PROGRESS && timesheet.getContract().getStatus() != ContractStatus.STARTED) {
            throw new IllegalStateException("Einträge nur im Status IN_PROGRESS änderbar.");
        }
    }

    private void deleteTimesheet(Timesheet timesheet) {
        timesheetDAO.delete(timesheet);
        timesheet.getContract().removeTimesheet(timesheet);
    }
}
