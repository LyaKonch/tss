package bravo.dao;

import bravo.entities.Contract;
import bravo.entities.Timesheet;
import bravo.enums.TimesheetStatus;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


/**
 * DAO for {@link Timesheet} entities.
 * <p>
 * Provides database operations for timesheets, including queries by contract,
 * status, date ranges, and retrieval of current or overdue timesheets.
 */

@LocalBean
@Stateless
public class TimesheetDAO extends AbstractDAO<Timesheet> {

    /**
     * Default constructor.
     * Initializes this DAO with the {@link Timesheet} entity class.
     */

    public TimesheetDAO() {
        super(Timesheet.class);
    }

    /**
     * Finds all timesheets associated with a specific {@link Contract}.
     *
     * @param contract the contract to search timesheets for
     * @return list of timesheets
     */
    public List<Timesheet> findByContract(Contract contract) {
        return em.createNamedQuery("Timesheet.findByContract", Timesheet.class).
                setParameter("contract", contract).
                getResultList();
    }

    /**
     * Finds all timesheets associated with a contract identified by its UUID.
     *
     * @param uuid the UUID of the contract
     * @return list of timesheets
     */
    public List<Timesheet> findByContractUuid(String uuid) {
        return em.createNamedQuery("Timesheet.findByContractUuid", Timesheet.class).
                setParameter("uuid", UUID.fromString(uuid)).
                getResultList();
    }


    /**
     * Finds timesheets with a specific {@link TimesheetStatus}.
     *
     * @param status the status to filter by
     * @return list of timesheets matching the status
     */
    public List<Timesheet> findbyStatus(TimesheetStatus status) {
        return em.createNamedQuery("Timesheet.findByStatus", Timesheet.class).
                setParameter("status", status).
                getResultList();
    }

    /**
     * Finds the current timesheet for a contract based on today's date.
     * <p>
     * A timesheet is considered current if today falls between its start and end dates.
     *
     * @param contractUuid the UUID of the contract
     * @return the current timesheet or {@code null} if none found
     */

    public Timesheet findCurrent(String contractUuid) {
        LocalDate today = LocalDate.now();
        return getSingleResultOrNull(em.createQuery(
                                "SELECT t FROM Timesheet t " +
                                        "WHERE t.contract.uuid = :contractUuid " +
                                        "AND :today BETWEEN t.startDate AND t.endDate",
                                Timesheet.class
                        ).setParameter("contractUuid", UUID.fromString(contractUuid))
                        .setParameter("today", today)
        );
    }

    /**
     * Finds timesheets that fall between the specified start and end dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of timesheets within the date range
     */
    public List<Timesheet> findTimesheetsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return em.createNamedQuery("Timesheet.findBetweenDates", Timesheet.class).
                setParameter("startDate", startDate).
                setParameter("endDate", endDate).getResultList();
    }

    /**
     * Finds overdue timesheets based on a reference date and status.
     * <p>
     * A timesheet is considered overdue if its end date is before the reference date.
     *
     * @param referenceDate the date to compare against
     * @param status        the status of the timesheet to filter by
     * @return list of overdue timesheets
     */
    public List<Timesheet> findOverdueTimesheets(LocalDate referenceDate, TimesheetStatus status) {
        return em.createQuery("SELECT t FROM Timesheet t WHERE t.status = :status AND t.endDate < :referenceDate", Timesheet.class)
                .setParameter("status", status)
                .setParameter("referenceDate", referenceDate)
                .getResultList();
    }

    /**
     * Finds archived timesheets that ended before the specified date.
     * <p>
     * Note: The end date of a timesheet does not exactly correspond
     * to the time it was archived, but this method retrieves timesheets
     * with status ARCHIVED that ended before the given date.
     *
     * @param date the cutoff date
     * @return list of archived timesheets before the specified date
     */
    public List<Timesheet> findArchivedByContractBeforeDate(Contract contract, LocalDate date) {
        return em.createQuery("SELECT t FROM Timesheet t WHERE t.contract = :contract " +
                        "AND t.status = :status AND t.signedBySupervisor < :date", Timesheet.class)
                .setParameter("contract", contract)
                .setParameter("status", TimesheetStatus.ARCHIVED)
                .setParameter("date", date)
                .getResultList();
    }
}
