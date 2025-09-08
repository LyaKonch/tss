package bravo.dao;

import bravo.entities.TimesheetEntry;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import java.util.List;


@LocalBean
@Stateless
public class TimesheetEntryDAO extends AbstractDAO<TimesheetEntry> {

    public TimesheetEntryDAO() {
        super(TimesheetEntry.class);
    }

    public List<TimesheetEntry> findByTimesheetUUID(String timesheetUUID) {
        return em.createNamedQuery("TimesheetEntry.findByTimesheetUuid", TimesheetEntry.class)
                .setParameter("timesheetUuid", timesheetUUID)
                .getResultList();
    }

}
