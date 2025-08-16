/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package bravo.dao;

import bravo.entities.TimesheetEntry;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 *
 * @author Женіч
 */
@LocalBean
@Stateless
public class TimesheetEntryDAO extends AbstractDAO<TimesheetEntry>{
    
    public TimesheetEntryDAO(){
        super(TimesheetEntry.class);
    }
    
    public List<TimesheetEntry> findByTimesheetUUID(String timesheetUUID){
        return em.createQuery("SELECT e FROM TimesheetEntry e WHERE e.timesheet.uuid = :uuid", TimesheetEntry.class)
               .setParameter("timesheetId", timesheetUUID)
                .getResultList(); 
    } 
     
}
