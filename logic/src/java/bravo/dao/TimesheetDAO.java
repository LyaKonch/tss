package bravo.dao;

import bravo.entities.Timesheet;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import java.time.LocalDate;
import java.util.UUID;

@LocalBean
@Stateless
public class TimesheetDAO extends AbstractDAO<Timesheet> {

    public TimesheetDAO() {
        super(Timesheet.class);
    }

    public Timesheet findCurrent(String contractUuid) {
        LocalDate today = LocalDate.now();
        return em.createQuery(
                        "SELECT t FROM Timesheet t " +
                                "WHERE t.contract.uuid = :contractUuid " +
                                "AND :today BETWEEN t.startDate AND t.endDate",
                        Timesheet.class
                )
                .setParameter("contractUuid", UUID.fromString(contractUuid))
                .setParameter("today", today)
                .getSingleResult();
    }

}

