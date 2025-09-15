package bravo.logic.impl;

import bravo.dao.TimesheetDAO;
import bravo.dao.TimesheetEntryDAO;
import bravo.dto.ContractDTO;
import bravo.dto.TimesheetDTO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;
import bravo.enums.TimesheetStatus;
import bravo.logic.TimesheetLogic;
import bravo.mapper.TimesheetMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Stateless
public class TimesheetLogicBean implements TimesheetLogic {

    private static Predicate<LocalDate> holidayChecker;

    @EJB
    private TimesheetEntryDAO entryDAO;

    @Inject
    private TimesheetDAO timesheetDAO;

    @Inject
    private TimesheetMapper timesheetMapper;

    public void createNewEntry(TimesheetEntryDTO entryDto) {
        //Timesheet timesheet = timesheetDAO.findByUUID(entryDto.gettimesheetID());
        // if(timesheet==null) {throw new IllegalArgumentException("Timesheet with" + entryDto.gettimesheetID()+ "is not found")};
        // we need timesheetDAO to link entry to timesheet
        TimesheetEntry newEntry = new TimesheetEntry(true);// true means new object, it's supposed to generate new UUID for new objects;
        newEntry.setEntryDate(entryDto.getEntryDate());
        newEntry.setStartTime(entryDto.getStartTime());
        newEntry.setEndTime(entryDto.getEndTime());
        newEntry.setType(entryDto.getType());
        newEntry.setDescription(entryDto.getDescription());
        //addEntry(timesheet, newEntry);

    }

    @Override
    public void addEntry(Timesheet timesheet, TimesheetEntry e) {
        requireEditable(timesheet);
        if (e != null) {
            e.setTimesheet(timesheet);
            timesheet.getEntries().add(e);
        }
        entryDAO.save(e);
        //timesheetDAO.save(timesheet);
    }

    @Override
    public void removeEntry(Timesheet timesheet, TimesheetEntry e) {
        requireEditable(timesheet);
        if (e != null) {
            timesheet.getEntries().remove(e);
            e.setTimesheet(null);
        }
        entryDAO.save(e);
        //timesheetDAO.save(timesheet);
    }

    ;

    @Override
    public void signByEmployee(Timesheet timesheet, LocalDate date) {
        requireStatus(timesheet, TimesheetStatus.IN_PROGRESS);
        timesheet.setSignedByEmployee((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        //timesheetDAO.save(timesheet);
    }

    @Override
    public void revokeEmployeeSignature(Timesheet timesheet) {
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        //timesheetDAO.save(timesheet);
    }

    @Override
    public void signBySupervisor(Timesheet timesheet, LocalDate date) {
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedBySupervisor((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        //timesheetDAO.save(timesheet);
    }

    @Override
    public void archive(Timesheet timesheet) {
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_SUPERVISOR);
        // maybe some actions here
        timesheet.setStatus(TimesheetStatus.ARCHIVED);
        //timesheetDAO.save(timesheet);
    }

    @Override
    public double getHoursDue(Timesheet timesheet) {
        if (timesheet.getContract() == null || timesheet.getStartDate() == null || timesheet.getEndDate() == null)
            return 0.0;
        int working = countWorkingDays(timesheet.getStartDate(), timesheet.getEndDate());
        int holidays = countHolidaysOnWorkingDays(timesheet.getStartDate(), timesheet.getEndDate());
        int effectiveDays = Math.max(0, working - holidays);
        return effectiveDays * (timesheet.getContract().getHoursPerWeek() / timesheet.getContract().getWorkingDaysPerWeek());
    }


    @Override
    public double getReportedHours(Timesheet timesheet) {
        return timesheet.getEntries().stream().mapToDouble(TimesheetEntry::getHours).sum();
    }

    @Override
    public List<TimesheetEntry> getEntries(Timesheet timesheet) {
        return Collections.unmodifiableList(timesheet.getEntries());
    }

    @Override
    public TimesheetDTO getCurrent(ContractDTO contract) {
        Timesheet entity = timesheetDAO.findCurrent(contract.getUuid());
        TimesheetDTO dto = timesheetMapper.toDto(entity);
        dto.setHoursDue(getHoursDue(entity));
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
        //timesheetDAO.save(timesheet);
    }

    private int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

    private int countHolidaysOnWorkingDays(LocalDate start, LocalDate end) {
        if (holidayChecker == null) return 0;
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            boolean workday = (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY);
            if (workday && holidayChecker.test(dat)) count++;
        }
        return count;
    }

    private void requireStatus(Timesheet timesheet, TimesheetStatus expected) {
        if (timesheet.getStatus() != expected) {
            throw new IllegalStateException("Aktion nur im Status " + expected + " erlaubt.");
        }
    }

    private void requireEditable(Timesheet timesheet) {
        if (timesheet.getStatus() != TimesheetStatus.IN_PROGRESS) {
            throw new IllegalStateException("Einträge nur im Status IN_PROGRESS änderbar.");
        }
    }
}
