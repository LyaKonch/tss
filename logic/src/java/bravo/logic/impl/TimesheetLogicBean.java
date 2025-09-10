package bravo.logic.impl;

//import bravo.dao.PersonDAO;
import bravo.dao.TimesheetDAO;
import bravo.dao.TimesheetEntryDAO;
import bravo.dto.TimesheetEntryDTO;
import bravo.entities.Contract;
import bravo.entities.Timesheet;
import bravo.entities.TimesheetEntry;
import bravo.enums.FederalState;
import static bravo.enums.FederalState.RHINELAND_PALATINATE;

import bravo.enums.TimesheetStatus;
import bravo.logic.TimesheetLogic;
import bravo.utitity.PublicHolidayService;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Stateless
public class TimesheetLogicBean implements TimesheetLogic {

    //@EJB
    //private PersonDAO personDAO;
    
    @EJB
    private TimesheetEntryDAO entryDAO;

    @EJB
    private TimesheetDAO timesheetDAO;
    
    
    public void createNewEntry(TimesheetEntryDTO entryDto) {
        Timesheet timesheet = timesheetDAO.findByUUID(entryDto.getUuid());
        if (timesheet == null) {
            throw new IllegalArgumentException("Timesheet with" + entryDto.getUuid() + "is not found");
        }
        // we need timesheetDAO to link entry to timesheet
        TimesheetEntry newEntry = new TimesheetEntry(true);// true means new object, it's supposed to generate new UUID for new objects;
        newEntry.setEntryDate(entryDto.getEntryDate());
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
    public void addEntry(Timesheet timesheet, TimesheetEntry e) {
        requireEditable(timesheet);
        if (e != null) {
            e.setTimesheet(timesheet);
            timesheet.getEntries().add(e);
        }
        entryDAO.save(e);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void removeEntry(Timesheet timesheet, TimesheetEntry e) {
        requireEditable(timesheet);
        if (e != null) {
            timesheet.getEntries().remove(e);
            e.setTimesheet(null);
        }
        entryDAO.save(e);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void signByEmployee(Timesheet timesheet, LocalDate date) {
        requireStatus(timesheet, TimesheetStatus.IN_PROGRESS);
        timesheet.setSignedByEmployee((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void revokeEmployeeSignature(Timesheet timesheet) {
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void signBySupervisor(Timesheet timesheet, LocalDate date) {
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheet.setSignedBySupervisor((date != null) ? date : LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        timesheetDAO.save(timesheet);
    }

    @Override
    public void archive(Timesheet timesheet) {
        requireStatus(timesheet, TimesheetStatus.SIGNED_BY_SUPERVISOR);
        // maybe some actions here
        timesheet.setStatus(TimesheetStatus.ARCHIVED);
        timesheetDAO.save(timesheet);
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
        if (timesheet.getStatus() != TimesheetStatus.IN_PROGRESS) {
            throw new IllegalStateException("Einträge nur im Status IN_PROGRESS änderbar.");
        }
    }

    private void deleteTimesheet(Timesheet timesheet) {
        timesheetDAO.delete(timesheet);
        // does entity manager automatically delete link in contract to this timesheet?
        // does CascadeType.ALL take care of that?
    }
    
    
    
}
