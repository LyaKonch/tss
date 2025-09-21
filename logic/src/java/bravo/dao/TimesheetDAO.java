package bravo.dao;

import bravo.entities.Contract;
import bravo.entities.Timesheet;
import bravo.enums.TimesheetStatus;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@LocalBean
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

    public List<Timesheet> findByContractUuid(String uuid) {
        return em.createNamedQuery("Timesheet.findByContractUuid", Timesheet.class).
                setParameter("uuid", UUID.fromString(uuid)).
                getResultList();
    }

    public List<Timesheet> findbyStatus(TimesheetStatus status) {
        return em.createNamedQuery("Timesheet.findByStatus", Timesheet.class).
                setParameter("status", status).
                getResultList();
    }

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

    // end date of the timesheet doesn't exactly point at the time it was archived.
    public List<Timesheet> findArchivedBefore(LocalDate date) {
        return em.createQuery("SELECT t FROM Timesheet t WHERE t.status = :archived AND t.endDate < :date", Timesheet.class)
                .setParameter("archived", TimesheetStatus.ARCHIVED)
                .setParameter("date", date)
                .getResultList();

    }
}
