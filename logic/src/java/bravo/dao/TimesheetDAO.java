/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bravo.dao;

import bravo.entities.Contract;
import bravo.entities.Timesheet;
import bravo.enums.TimesheetStatus;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.util.List;

@Stateless
public class TimesheetDAO extends AbstractDAO<Timesheet> {
    
    public TimesheetDAO() {
        super(Timesheet.class);
    }
    
    public List<Timesheet> findByContract(Contract contract) {
        return em.createNamedQuery("Timesheet.findByContract", Timesheet.class).
                setParameter("contract", contract).
                getResultList();
    }
    
    public List<Timesheet> findbyStatus(TimesheetStatus status){
        return em.createNamedQuery("Timesheet.findByStatus", Timesheet.class).
                setParameter("status",status).
                getResultList();
    }
    
    public List<Timesheet> findTimesheetsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return em.createNamedQuery("Timesheet.findBetweenDates", Timesheet.class).
        setParameter("startDate", startDate).
        setParameter("endDate", endDate).getResultList();
    }  
    
    public List<Timesheet> findOverdueTimesheets(LocalDate referenceDate, TimesheetStatus status) {
        return em.createQuery("SELECT t FROM Timesheet t WHERE t.status = :status AND t.endDate < :referenceDate", Timesheet.class)
            .setParameter("status", status)
            .setParameter("referenceDate", referenceDate)
            .getResultList();
    }
    
}
