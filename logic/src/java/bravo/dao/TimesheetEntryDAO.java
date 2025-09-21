package bravo.dao;

import bravo.dto.TimesheetEntryDTO;
import bravo.entities.TimesheetEntry;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import java.util.List;
import java.util.UUID;


@LocalBean
@Stateless
public class TimesheetEntryDAO extends AbstractDAO<TimesheetEntry> {

    public TimesheetEntryDAO() {
        super(TimesheetEntry.class);
    }

    public List<TimesheetEntry> findByTimesheetUUID(String timesheetUUID) {
        return em.createNamedQuery("TimesheetEntry.findByTimesheetUuid", TimesheetEntry.class)
                .setParameter("timesheetUuid", UUID.fromString(timesheetUUID))
                .getResultList();
    }

    public List<TimesheetEntryDTO> findProjectedByTimesheetUuid(String timesheetUuid) {
        return em.createQuery(
                        "SELECT new bravo.dto.TimesheetEntryDTO(e.id, e.uuid, e.type, e.description, e.startTime, e.endTime, e.entryDate, e.timesheet.uuid)" +
                                "FROM TimesheetEntry e " +
                                "WHERE e.timesheet.uuid = :uuid " +
                                "ORDER BY e.entryDate DESC",
                        TimesheetEntryDTO.class
                )
                .setParameter("uuid", UUID.fromString(timesheetUuid))
                .getResultList();
    }

}
