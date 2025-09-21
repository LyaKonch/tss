package bravo.dao;

import bravo.dto.TimesheetEntryDTO;
import bravo.entities.TimesheetEntry;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import java.util.List;
import java.util.UUID;


/**
 * DAO for {@link TimesheetEntry} entities.
 * <p>
 * Provides database operations for timesheet entries, including retrieval
 * by timesheet UUID and projected DTO queries.
 */
@LocalBean
@Stateless
public class TimesheetEntryDAO extends AbstractDAO<TimesheetEntry> {

    /**
     * Default constructor.
     * Initializes this DAO with the {@link TimesheetEntry} entity class.
     */
    public TimesheetEntryDAO() {
        super(TimesheetEntry.class);
    }

    /**
     * Finds all {@link TimesheetEntry} entities associated with a specific timesheet UUID.
     *
     * @param timesheetUUID the UUID of the timesheet
     * @return list of timesheet entries
     */
    public List<TimesheetEntry> findByTimesheetUUID(String timesheetUUID) {
        return em.createNamedQuery("TimesheetEntry.findByTimesheetUuid", TimesheetEntry.class)
                .setParameter("timesheetUuid", UUID.fromString(timesheetUUID))
                .getResultList();
    }

    /**
     * Finds projected {@link TimesheetEntryDTO} objects for a specific timesheet UUID.
     * <p>
     * Uses a constructor expression to map {@link TimesheetEntry} fields into
     * {@link TimesheetEntryDTO} instances.
     *
     * @param timesheetUuid the UUID of the timesheet
     * @return list of projected TimesheetEntryDTOs ordered by entry date descending
     */
    public List<TimesheetEntryDTO> findProjectedByTimesheetUuid(String timesheetUuid) {
        return em.createQuery(
                        "SELECT new bravo.dto.TimesheetEntryDTO(e.type, e.description, e.startTime, e.endTime, e.entryDate, e.timesheet.uuid)" +
                                "FROM TimesheetEntry e " +
                                "WHERE e.timesheet.uuid = :uuid " +
                                "ORDER BY e.entryDate DESC",
                        TimesheetEntryDTO.class
                )
                .setParameter("uuid", UUID.fromString(timesheetUuid))
                .getResultList();
    }

}
