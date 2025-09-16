package bravo.utitity;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.dao.TimesheetDAO;
import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.entities.*;
import bravo.enums.*;
import bravo.logic.ContractLogic;
import bravo.logic.PersonLogic;
import bravo.logic.impl.TimesheetLogicBean;
import bravo.logic.TimesheetLogic;
import bravo.logic.impl.TimesheetLogicBean;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Startup
@Singleton
public class TestCrudContractAndTimesheet {

    @EJB
    PersonLogic personLogic;

    @EJB
    private ContractLogic contractBean;

    @EJB
    private TimesheetLogic timesheetBean;

    // Added DAO injections used to inspect Entities created by the EJBs.
    @EJB
    private ContractDAO contractDAO;

    @EJB
    private TimesheetDAO timesheetDAO;

    private String employeeUuid;
    private String supervisorUuid;
    private String secretaryUuid;
    private String assistantUuid;

    public void runTests() {
        System.out.println("--- Starting Contract and Timesheet logic test suite ---");
        try {
            List<PersonDTO> persons = personLogic.findAll();
            employeeUuid = persons.stream()
                    .filter(person -> "Test".equals(person.getFirstName()) && "Employee".equals(person.getLastName()))
                    .findFirst().get().getUuid();

            supervisorUuid = persons.stream()
                    .filter(person -> "Test".equals(person.getFirstName()) && "Supervisor".equals(person.getLastName()))
                    .findFirst().get().getUuid();

            secretaryUuid = persons.stream()
                    .filter(person -> "Test".equals(person.getFirstName()) && "Secretary".equals(person.getLastName()))
                    .findFirst().get().getUuid();

            assistantUuid = persons.stream()
                    .filter(person -> "Test".equals(person.getFirstName()) && "Assistant".equals(person.getLastName()))
                    .findFirst().get().getUuid();
            ;

            // Contract logic tests
            testContractCrudOperations();
            testContractStatusTransitions();

            // Timesheet logic tests
            testTimesheetGeneration();
            testTimesheetEntryOperations();
            testTimesheetSigningProcess();
            testHoursDueCalculation();

        } catch (Exception e) {
            System.err.println("An unexpected error occurred during tests: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("--- Contract and Timesheet logic test suite Finished ---");
    }

    @PostConstruct
    public void shouldcreateTestPersons() {

        List<PersonDTO> persons = personLogic.findAll();

        if (persons.stream().noneMatch(person -> "Test".equals(person.getFirstName()) && "Employee".equals(person.getLastName()))) {

            PersonDTO employeePerson = new PersonDTO();
            employeePerson.setFirstName("Test");
            employeePerson.setLastName("Employee");
            employeePerson.setEmailAddress("employee@example.com");
            employeePerson.setState(FederalState.RHINELAND_PALATINATE);
            employeePerson.setDateOfBirth(LocalDate.of(1990, 1, 1));
            employeePerson.setConsent(true);
            personLogic.create(employeePerson);

            PersonDTO supervisorPerson = new PersonDTO();
            supervisorPerson.setFirstName("Test");
            supervisorPerson.setLastName("Supervisor");
            supervisorPerson.setEmailAddress("supervisor@example.com");
            supervisorPerson.setState(FederalState.RHINELAND_PALATINATE);
            supervisorPerson.setDateOfBirth(LocalDate.of(1980, 1, 1));
            supervisorPerson.setConsent(true);
            personLogic.create(supervisorPerson);

            PersonDTO secretaryPerson = new PersonDTO();
            secretaryPerson.setFirstName("Test");
            secretaryPerson.setLastName("Secretary");
            secretaryPerson.setEmailAddress("secretary@example.com");
            secretaryPerson.setState(FederalState.RHINELAND_PALATINATE);
            secretaryPerson.setDateOfBirth(LocalDate.of(1985, 1, 1));
            secretaryPerson.setConsent(true);
            personLogic.create(secretaryPerson);

            PersonDTO assistantPerson = new PersonDTO();
            assistantPerson.setFirstName("Test");
            assistantPerson.setLastName("Assistant");
            assistantPerson.setEmailAddress("assistant@example.com");
            assistantPerson.setState(FederalState.RHINELAND_PALATINATE);
            assistantPerson.setDateOfBirth(LocalDate.of(1992, 1, 1));
            assistantPerson.setConsent(true);
            personLogic.create(assistantPerson);
        }

        runTests();
    }

    /**
     * Create + Update tests.
     * <p>
     * Verifies: - contract created and set to PREPARED (CN5) - update allowed
     * in PREPARED (CN6)
     */
    private void testContractCrudOperations() {
        System.out.println("== Test Contract CRUD ==");
        ContractDTO dto = new ContractDTO();
        dto.setName("Test Contract");
        dto.setStatus(ContractStatus.PREPARED);
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 12, 31));
        dto.setFrequency(TimesheetFrequency.MONTHLY);
        dto.setHoursPerWeek(20.0);
        dto.setWorkingDaysPerWeek(5);
        dto.setVacationDaysPerYear(20);
        dto.setTerminationDate(null);
        dto.setEmployeeUuid(this.employeeUuid);
        dto.setSupervisorUuid(this.supervisorUuid);
        dto.setSecretaryUuids(Collections.singletonList(this.secretaryUuid));
        dto.setAssistantUuids(Collections.singletonList(this.assistantUuid));

        ContractDTO created = contractBean.create(dto);
        if (created == null) {
            throw new RuntimeException("createContract returned null");
        }
        System.out.println("Created contract UUID: " + created.getUuid() + " status: " + created.getStatus());
        if (created.getStatus() != ContractStatus.PREPARED) {
            throw new RuntimeException("Expected PREPARED after create (CN5).");
        }
        created.setName("Updated Name");
        created.setEmployeeUuid(supervisorUuid);
        ContractDTO updated = contractBean.update(created);
        System.out.println("Updated contract name: " + updated.getName());
        if (!"Updated Name".equals(updated.getName())) {
            throw new RuntimeException("Update did not persist name change (CN1/CN6).");
        }
        System.out.println("Updated contract employeeeUUID: " + updated.getEmployeeUuid());
        if (!supervisorUuid.equals(updated.getEmployeeUuid())) {
            throw new RuntimeException("Update did not persist employee change (CN1/CN6).");
        }
        contractBean.deleteContract(updated.getUuid());
        ContractDTO deletedContract = contractBean.findByUuid(updated.getUuid());
        if (deletedContract != null) {
            throw new RuntimeException("Contract was not deleted.");
        }
        System.out.println("testContractCrudOperations OK");
    }

    /**
     * Start -> Terminate -> Archive transitions.
     * <p>
     * Verifies: - startContract creates timesheets in IN_PROGRESS (CN7, TS1) -
     * terminateContract sets TERMINATED and removes IN_PROGRESS timesheets
     * (CN8, TS4) - archiveContract sets ARCHIVED when allowed (CN12, AR1)
     */
    private void testContractStatusTransitions() {
        System.out.println("== Test Contract Status Transitions ==");
        ContractDTO dto = new ContractDTO();
        dto.setName("Transition Contract");
        dto.setStatus(ContractStatus.PREPARED);
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 3, 31));
        dto.setFrequency(TimesheetFrequency.WEEKLY);
        dto.setHoursPerWeek(10.0);
        dto.setWorkingDaysPerWeek(5);
        dto.setVacationDaysPerYear(20);
        dto.setTerminationDate(null);
        dto.setEmployeeUuid(this.employeeUuid);
        dto.setSupervisorUuid(this.supervisorUuid);
        dto.setSecretaryUuids(Collections.singletonList(this.secretaryUuid));
        dto.setAssistantUuids(Collections.singletonList(this.assistantUuid));

        ContractDTO created = contractBean.create(dto);
        ContractDTO started = contractBean.startContract(created.getUuid()); // CN7 + TS1
        System.out.println("Started contract UUID: " + started.getUuid() + " status: " + started.getStatus());
        if (started.getStatus() != ContractStatus.STARTED) {
            throw new RuntimeException("Expected STARTED after startContract (CN7).");
        }

        // Inspect entity timesheets
        Contract contractEntity = contractDAO.findByUuid(started.getUuid());
        List<Timesheet> tsList = contractEntity.getTimesheets();
        System.out.println("Timesheets created: " + tsList.size());
        if (tsList.isEmpty()) {
            throw new RuntimeException("No timesheets created on start (TS1 MIN failed).");
        }
        boolean allInProgress = tsList.stream().allMatch(ts -> ts.getStatus() == TimesheetStatus.IN_PROGRESS);
        if (!allInProgress) {
            throw new RuntimeException("Not all timesheets are IN_PROGRESS after start.");
        }

        // Terminate
        ContractDTO terminated = contractBean.terminate(started.getUuid(), LocalDate.now()); // CN8 + TS4
        System.out.println("Terminated contract status: " + terminated.getStatus());
        if (terminated.getStatus() != ContractStatus.TERMINATED) {
            throw new RuntimeException("Expected TERMINATED after terminateContract (CN8).");
        }

        if (!terminated.getTimesheetUuids().isEmpty()) {
            throw new RuntimeException("Expected empty timesheets list after temrination because no timesheets was signed");
        }

        // Archive (allowed after TERMINATED)
        ContractDTO archived = contractBean.archiveContract(terminated.getUuid()); // CN12 + AR1
        System.out.println("Archived contract status: " + archived.getStatus());
        if (archived.getStatus() != ContractStatus.ARCHIVED) {
            throw new RuntimeException("Expected ARCHIVED after archiveContract (CN12).");
        }
    }

    /**
     * Ensure timesheet creation matches contract frequency & date range (TS1).
     */
    private void testTimesheetGeneration() {
        System.out.println("== testTimesheetGeneration ==");
        ContractDTO dto = new ContractDTO();
        dto.setName("TS Gen Contract");
        dto.setStatus(ContractStatus.PREPARED);
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 3, 31));
        dto.setFrequency(TimesheetFrequency.MONTHLY);
        dto.setHoursPerWeek(20.0);
        dto.setWorkingDaysPerWeek(5);
        dto.setVacationDaysPerYear(20);
        dto.setEmployeeUuid(this.employeeUuid);
        dto.setSupervisorUuid(this.supervisorUuid);
        dto.setSecretaryUuids(Collections.singletonList(this.secretaryUuid));

        ContractDTO created = contractBean.create(dto);
        ContractDTO startedDto = contractBean.startContract(created.getUuid());

        Contract contractEntity = contractDAO.findByUuid(startedDto.getUuid());
        List<Timesheet> tsList = contractEntity.getTimesheets();
        List<Timesheet> timesheets = timesheetDAO.findByContract(contractEntity);
        System.out.println("Timesheets created for monthly contract: " + tsList.size());
        if (tsList.size() < 3 && (timesheets.equals(tsList))) {
            throw new RuntimeException("Expected at least 3 monthly timesheets for Jan-Mar (TS1) and consistent creating of timesheet in correspoding table");
        }

        long inProgressCount = tsList.stream().filter(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS).count();
        if (inProgressCount != tsList.size()) {
            throw new RuntimeException("All timesheets should be IN_PROGRESS after start (TS1).");
        }
        System.out.println("testTimesheetGeneration OK");
    }

    /**
     * Add / Remove entries when timesheet IN_PROGRESS (TS2, TS9).
     */
    private void testTimesheetEntryOperations() {
        System.out.println("== Test Timesheet Entries ==");
        ContractDTO dto = new ContractDTO();
        dto.setName("Entry Contract");
        dto.setStatus(ContractStatus.PREPARED);
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 1, 31));
        dto.setFrequency(TimesheetFrequency.MONTHLY);
        dto.setHoursPerWeek(20.0);
        dto.setWorkingDaysPerWeek(5);
        dto.setVacationDaysPerYear(20);
        dto.setEmployeeUuid(this.employeeUuid);
        dto.setSupervisorUuid(this.supervisorUuid);
        dto.setSecretaryUuids(Collections.singletonList(this.secretaryUuid));

        ContractDTO created = contractBean.create(dto);
        ContractDTO started = contractBean.startContract(created.getUuid());
        Contract entity = contractDAO.findByUuid(started.getUuid());
        Timesheet ts = entity.getTimesheets().get(0);

        TimesheetEntry entry = new TimesheetEntry(true);
        entry.setEntryDate(LocalDate.of(2025, 1, 2));
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(13, 0));
        entry.setDescription("Worked on project");
        entry.setType(ReportType.WORK);

        // add
        timesheetBean.addEntry(ts, entry); // TS2, TS9
        double reported = timesheetBean.getReportedHours(ts);
        System.out.println("Entry added. Reported hours: " + reported);
        if (reported <= 0) {
            throw new RuntimeException("Reported hours should be > 0 after adding an entry.");
        }

        // remove
        timesheetBean.removeEntry(ts, entry);
        double reportedAfterRemove = timesheetBean.getReportedHours(ts);
        System.out.println("Entry removed. Reported hours now: " + reportedAfterRemove);
        if (reportedAfterRemove != 0.0) {
            throw new RuntimeException("Reported hours should be 0 after removing the only entry.");
        }
    }

    /**
     * Signing and archiving flow (SG1, SG1a, SG2, AR1, AR2).
     */
    private void testTimesheetSigningProcess() {
        System.out.println("== Test Timesheet Signing Process ==");
        ContractDTO dto = new ContractDTO();
        dto.setName("Signing Contract");
        dto.setStatus(ContractStatus.PREPARED);
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 1, 31));
        dto.setFrequency(TimesheetFrequency.MONTHLY);
        dto.setHoursPerWeek(20.0);
        dto.setWorkingDaysPerWeek(5);
        dto.setVacationDaysPerYear(20);
        dto.setEmployeeUuid(this.employeeUuid);
        dto.setSupervisorUuid(this.supervisorUuid);
        dto.setSecretaryUuids(Collections.singletonList(this.secretaryUuid));

        ContractDTO created = contractBean.create(dto);
        ContractDTO started = contractBean.startContract(created.getUuid());
        Contract entity = contractDAO.findByUuid(started.getUuid());
        Timesheet ts = entity.getTimesheets().get(0);

        // Add an entry so signing is meaningful
        TimesheetEntry entry = new TimesheetEntry(true);
        entry.setEntryDate(LocalDate.of(2025, 1, 2));
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(13, 0));
        entry.setType(ReportType.WORK);
        entry.setDescription("Work");
        timesheetBean.addEntry(ts, entry);

        // Employee signs
        timesheetBean.signByEmployee(ts, LocalDate.of(2025, 1, 31)); // SG1
        if (ts.getStatus() != TimesheetStatus.SIGNED_BY_EMPLOYEE) {
            throw new RuntimeException("Timesheet should be SIGNED_BY_EMPLOYEE after signByEmployee (SG1).");
        }

        // Revoke by employee
        timesheetBean.revokeEmployeeSignature(ts); // SG1a
        if (ts.getStatus() != TimesheetStatus.IN_PROGRESS) {
            throw new RuntimeException("Timesheet should be IN_PROGRESS after revokeEmployeeSignature (SG1a).");
        }

        // Sign again and then supervisor signs
        timesheetBean.signByEmployee(ts, LocalDate.of(2025, 1, 31));
        timesheetBean.signBySupervisor(ts, LocalDate.of(2025, 2, 1)); // SG2
        if (ts.getStatus() != TimesheetStatus.SIGNED_BY_SUPERVISOR) {
            throw new RuntimeException("Timesheet should be SIGNED_BY_SUPERVISOR after signBySupervisor (SG2).");
        }

        // Archive
        timesheetBean.archive(ts); // AR1, AR2
        if (ts.getStatus() != TimesheetStatus.ARCHIVED) {
            throw new RuntimeException("Timesheet should be ARCHIVED after archive() (AR1).");
        }
    }

    /**
     * getHoursDue aggregated on Contract (CN4, CN4b, CN4c).
     */
    private void testHoursDueCalculation() {
        System.out.println("== Test Hours Due Calculation ==");
        ContractDTO dto = new ContractDTO();
        dto.setName("HoursDue Contract");
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 1, 31));
        dto.setFrequency(TimesheetFrequency.MONTHLY);
        dto.setHoursPerWeek(20.0);
        dto.setWorkingDaysPerWeek(5);
        dto.setVacationDaysPerYear(20);
        dto.setEmployeeUuid(this.employeeUuid);
        dto.setSupervisorUuid(this.supervisorUuid);
        dto.setSecretaryUuids(Collections.singletonList(this.secretaryUuid));

        ContractDTO created = contractBean.create(dto);
        ContractDTO started = contractBean.startContract(created.getUuid());

        Double hoursDue = contractBean.calculateHoursDue(started);
        System.out.println("Hours due for contract: " + hoursDue);
        if (hoursDue == null || hoursDue <= 0.0) {
            throw new RuntimeException("Expected positive hours due (CN4).");
        }
    }
}
