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
import bravo.logic.TimesheetLogic;
import bravo.mapper.TimesheetEntryMapper;
import bravo.mapper.TimesheetMapper;
import bravo.utitity.PublicHolidayService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class TimesheetLogicBean implements TimesheetLogic {

    @EJB
    private PublicHolidayService holidayChecker;

    @EJB
    private TimesheetEntryDAO entryDAO;

    @EJB
    private TimesheetDAO timesheetDAO;

    @Inject
    private TimesheetMapper timesheetMapper;

    @Inject
    TimesheetEntryMapper timesheetEntryMapper;

    private static final Logger logger = Logger.getLogger(TimesheetLogicBean.class.getName());


    public void createNewEntry(TimesheetEntryDTO entryDto) {
        Timesheet timesheet = timesheetDAO.findByUuid(entryDto.getUuid());
        if (timesheet == null) {
            throw new IllegalArgumentException("Timesheet with" + entryDto.getUuid() + "is not found");
        }

        // true means a new object, it's supposed to generate new UUID for new objects;
        TimesheetEntry newEntry = new TimesheetEntry(true);
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

    public boolean isSignedByEmployee(Timesheet timesheet) {
        return timesheet.getSignedByEmployee() != null;
    }

    @Override
    public void addEntry(Timesheet timesheet, TimesheetEntry e ) {

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

    @Override
    public void removeEntry(TimesheetDTO timesheetdto, TimesheetEntryDTO entryDTO) {
        Timesheet timesheet = timesheetMapper.toEntity(timesheetdto);
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

    @Override
    public void signByEmployee(TimesheetDTO timesheetdto, LocalDate date) {
        Timesheet timesheet = timesheetMapper.toEntity(timesheetdto);
        requireStatus(timesheet, TimesheetStatus.IN_PROGRESS);
        timesheet.setSignedByEmployee((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void revokeEmployeeSignature(TimesheetDTO timesheetdto) {
        Timesheet timesheet = timesheetMapper.toEntity(timesheetdto);
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void signBySupervisor(TimesheetDTO timesheetdto, LocalDate date) {
        Timesheet timesheet = timesheetMapper.toEntity(timesheetdto);
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedBySupervisor((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void archive(TimesheetDTO timesheetdto) {
        Timesheet timesheet = timesheetMapper.toEntity(timesheetdto);
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_SUPERVISOR);
        // maybe some actions here
        timesheet.setStatus(TimesheetStatus.ARCHIVED);
        timesheetDAO.save(timesheet);
    }

    @Override
    public double getReportedHours(TimesheetDTO timesheetdto) {
        Timesheet timesheet = timesheetMapper.toEntity(timesheetdto);
        return timesheet.getEntries().stream().mapToDouble(TimesheetEntry::getHours).sum();
    }

    @Override
    public List<TimesheetEntry> getEntries(Timesheet timesheet) {
        return Collections.unmodifiableList(timesheet.getEntries());
    }

    @Override
    public TimesheetDTO getCurrent(ContractDTO contract) {
        if(timesheetDAO.findCurrent(contract.getUuid())== null) { return null; }
        Timesheet entity = timesheetDAO.findCurrent(contract.getUuid());
        TimesheetDTO dto = timesheetMapper.toDto(entity);
        dto.setHoursDue(hoursDue(dto.getUuid()));
        return dto;
    }

    @Override
    public void setEntries(Timesheet timesheet, List<TimesheetEntry> newEntries) {
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

    private void requireStatus(Timesheet timesheet, TimesheetStatus expected) {
        if (timesheet.getStatus() != expected) {
            throw new IllegalStateException("Aktion nur im Status " + expected + " erlaubt.");
        }
    }

    private void requireEditable(Timesheet timesheet) {
        // TS2 MIN
        if (timesheet.getStatus() != TimesheetStatus.IN_PROGRESS && timesheet.getContract().getStatus() != ContractStatus.STARTED) {
            throw new IllegalStateException("Einträge nur im Status IN_PROGRESS änderbar.");
        }
    }

    private void deleteTimesheet(Timesheet timesheet) {
        timesheetDAO.delete(timesheet);
        timesheet.getContract().removeTimesheet(timesheet);
    }

    public double hoursDue(String uuid) {
        Timesheet timesheet = timesheetDAO.findByUuid(uuid);
        FederalState state = timesheet.getContract().getEmployee().getPerson().getState();
        if (timesheet.getContract() == null || timesheet.getStartDate() == null || timesheet.getEndDate() == null) {
            return 0.0;
        }
        int working = countWorkingDays(timesheet.getStartDate(), timesheet.getEndDate());
        int holidays = countHolidaysOnWorkingDays(timesheet.getStartDate(), timesheet.getEndDate(), state);
        int effectiveDays = Math.max(0, working - holidays);
        return effectiveDays * (timesheet.getContract().getHoursPerWeek() / timesheet.getContract().getWorkingDaysPerWeek());
    }

    public int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

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
}
