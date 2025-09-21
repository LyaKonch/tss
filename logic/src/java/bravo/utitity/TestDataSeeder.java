package bravo.utitity;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.entities.*;
import bravo.enums.ContractStatus;
import bravo.enums.ReportType;
import bravo.enums.TimesheetFrequency;
import bravo.enums.TimesheetStatus;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * TestDataSeeder initializes the database with sample data at application startup.
 * <p>
 * This includes:
 * <ul>
 *     <li>Sample persons with roles (Employee, Supervisor, Secretary)</li>
 *     <li>Contracts in various states (STARTED, PREPARED)</li>
 *     <li>Timesheets and timesheet entries across different scenarios</li>
 * </ul>
 *
 * The seeding is only performed if the database has no existing persons
 * (see {@link #preventSeeding()}).
 * <p>
 * This is intended for development and testing purposes, not for production use.
 */
@Singleton
@Startup
public class TestDataSeeder {

    @Inject
    private PersonDAO personDAO;

    @Inject
    private ContractDAO contractDAO;


    public TestDataSeeder() {
    }


    /**
     * Determine if seeding is necessary by counting the number of persons.
     *
     * @return true if persons are present, false otherwise
     */
    private boolean preventSeeding() {
        return personDAO.count() != 0;
    }

    @PostConstruct
    private void seed() {
        if (preventSeeding()) return;

        // Persons
        Person chef = createPerson("Chef", "Zero", "chef@example.com", LocalDate.of(1995, 9, 5), true);
        Person alice = createPerson("Alice", "Anderson", "alice@example.com", LocalDate.of(2001, 1, 1), true);
        Person bob = createPerson("Bob", "Baker", "bob@example.com", LocalDate.of(2005, 5, 5), true);

        // Contracts
        Contract contractA = createContract(ContractStatus.STARTED, "Alice Employee Contract", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), TimesheetFrequency.WEEKLY, null, 40.0);
        Contract contractB = createContract(ContractStatus.PREPARED, "Bob Employee Contract", LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31), TimesheetFrequency.MONTHLY, null, 38.0);

        // Roles (and links)
        Secretary secC = new Secretary();
        chef.addRole(secC);
        Supervisor supCA = new Supervisor();
        chef.addRole(supCA);
        Employee empA = new Employee();
        alice.addRole(empA);

        contractA.addSecretary(secC);
        contractA.setSupervisor(supCA);
        contractA.setEmployee(empA);


        Secretary secA = new Secretary();
        alice.addRole(secA);
        Supervisor supCB = new Supervisor();
        chef.addRole(supCB);
        Employee empB = new Employee();
        bob.addRole(empB);

        contractB.addSecretary(secA);
        contractB.setSupervisor(supCB);
        contractB.setEmployee(empB);


        personDAO.save(chef);
        personDAO.save(alice);
        personDAO.save(bob);

        // Timesheets
        Timesheet timesheetAA = createTimesheet(contractA, TimesheetStatus.ARCHIVED, LocalDate.of(2025, 9, 8), LocalDate.of(2025, 9, 14), true, true);
        Timesheet timesheetAB = createTimesheet(contractA, TimesheetStatus.IN_PROGRESS, LocalDate.of(2025, 9, 15), LocalDate.of(2025, 9, 21), false, false);
        Timesheet timesheetAC = createTimesheet(contractA, TimesheetStatus.IN_PROGRESS, LocalDate.of(2025, 9, 22), LocalDate.of(2025, 9, 28), false, false);

        Timesheet timesheetBA = createTimesheet(contractB, TimesheetStatus.ARCHIVED, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31), true, true);
        Timesheet timesheetBB = createTimesheet(contractB, TimesheetStatus.SIGNED_BY_SUPERVISOR, LocalDate.of(2021, 2, 1), LocalDate.of(2021, 2, 28), true, true);
        Timesheet timesheetBC = createTimesheet(contractB, TimesheetStatus.IN_PROGRESS, LocalDate.of(2021, 3, 1), LocalDate.of(2021, 3, 31), false, false);

        // Timesheet Entries
        createTimesheetEntry(timesheetAA, ReportType.WORK, "Worked on project XYZ", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2020, 1, 1));
        createTimesheetEntry(timesheetAA, ReportType.WORK, "Worked later on project XYZ", LocalTime.of(10, 0), LocalTime.of(18, 0), LocalDate.of(2020, 1, 5));
        createTimesheetEntry(timesheetAA, ReportType.WORK, "Worked early on project XYZ", LocalTime.of(7, 0), LocalTime.of(12, 0), LocalDate.of(2020, 1, 15));

        createTimesheetEntry(timesheetAB, ReportType.SICK_LEAVE, "Sick", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2020, 2, 1));
        createTimesheetEntry(timesheetAB, ReportType.SICK_LEAVE, "More sick", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2020, 2, 2));
        createTimesheetEntry(timesheetAB, ReportType.WORK, "Team meeting about project XYZ", LocalTime.of(7, 30), LocalTime.of(12, 0), LocalDate.of(2020, 2, 3));

        createTimesheetEntry(timesheetAC, ReportType.VACATION, "2 week vacation", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2020, 3, 1));
        createTimesheetEntry(timesheetAC, ReportType.VACATION, "2 week vacation", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2020, 3, 14));
        createTimesheetEntry(timesheetAC, ReportType.WORK, "Back to coffee", LocalTime.of(7, 0), LocalTime.of(12, 0), LocalDate.of(2020, 3, 15));

        createTimesheetEntry(timesheetBA, ReportType.WORK, "Starting new job", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2021, 1, 1));
        createTimesheetEntry(timesheetBA, ReportType.WORK, "Getting used to the job", LocalTime.of(10, 0), LocalTime.of(18, 0), LocalDate.of(2021, 1, 5));
        createTimesheetEntry(timesheetBA, ReportType.SICK_LEAVE, "Sick (of) the new job", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2021, 1, 15));

        createTimesheetEntry(timesheetBB, ReportType.WORK, "Worked on project XYZ", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2021, 2, 1));
        createTimesheetEntry(timesheetBB, ReportType.WORK, "Worked later on project XYZ", LocalTime.of(10, 0), LocalTime.of(18, 0), LocalDate.of(2021, 2, 5));
        createTimesheetEntry(timesheetBB, ReportType.WORK, "Worked early on project XYZ", LocalTime.of(7, 0), LocalTime.of(12, 0), LocalDate.of(2021, 2, 15));

        createTimesheetEntry(timesheetBC, ReportType.VACATION, "Long sleep week", LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDate.of(2021, 3, 1));
        createTimesheetEntry(timesheetBC, ReportType.VACATION, "Last day of long sleep week", LocalTime.of(10, 0), LocalTime.of(18, 0), LocalDate.of(2021, 3, 5));
        createTimesheetEntry(timesheetBC, ReportType.WORK, "Finishing up project XYZ", LocalTime.of(7, 0), LocalTime.of(16, 0), LocalDate.of(2021, 3, 25));

        // Persist contracts (cascades to roles, timesheets and entries)
        contractDAO.save(contractA);
        contractDAO.save(contractB);
    }

    /**
     * Helper for one-line creation of a person.
     */
    private Person createPerson(String first, String last, String email, LocalDate dob, boolean consent) {
        Person person = new Person();
        person.setFirstName(first);
        person.setLastName(last);
        person.setEmailAddress(email);
        person.setDateOfBirth(dob);
        person.setConsent(consent);
        return person;
    }

    /**
     * Helper for one-line creation of a contract.
     */
    private Contract createContract(ContractStatus status, String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency frequency, LocalDate terminationDate, double hoursPerWeek) {
        Contract contract = new Contract();
        contract.setStatus(status);
        contract.setName(name);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setFrequency(frequency);
        contract.setTerminationDate(terminationDate);
        contract.setHoursPerWeek(hoursPerWeek);
        return contract;
    }

    /**
     * Helper for one-line creation of a timesheet.
     * Links contract and timesheet.
     */
    private Timesheet createTimesheet(Contract contract, TimesheetStatus status, LocalDate startDate, LocalDate endDate, boolean signedByEmp, boolean signedBySup) {
        Timesheet sheet = new Timesheet();
        sheet.setContract(contract);
        sheet.setStatus(status);
        sheet.setStartDate(startDate);
        sheet.setEndDate(endDate);
        sheet.setSignedByEmployee(signedByEmp ? endDate : null);
        sheet.setSignedBySupervisor(signedBySup ? endDate.plusDays(3) : null);

        contract.addTimesheet(sheet);
        return sheet;
    }

    /**
     * Helper for one-line creation of a timesheet entry.
     * Links timesheet and entry.
     */
    private TimesheetEntry createTimesheetEntry(Timesheet timesheet, ReportType type, String description, LocalTime startTime, LocalTime endTime, LocalDate entryDate) {
        TimesheetEntry entry = new TimesheetEntry();
        entry.setTimesheet(timesheet);
        entry.setType(type);
        entry.setDescription(description);
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        entry.setEntryDate(entryDate);

        timesheet.addEntry(entry);

        return entry;
    }
}
