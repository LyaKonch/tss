package bravo.utitity;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.dao.RoleDAO;
import bravo.entities.*;
import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.time.LocalDate;

@Singleton
@Startup
public class TestDataSeeder {

    @Inject
    private PersonDAO personDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private ContractDAO contractDAO;

    public TestDataSeeder() {
    }

    @PostConstruct
    public void seedAll() {
        if (!shouldSeed()) {
            return;
        }

        seed();
    }

    /**
     * Determine if seeding is necessary by counting the number of persons.
     *
     * @return true if no persons are present, false otherwise
     */
    private boolean shouldSeed() {
        return personDAO.count() == 0;
    }

    private void seed() {
        // Persons
        Person chef = createPerson("Chef", "Zero", "chef@example.com", LocalDate.of(1995, 9, 5), true);
        Person alice = createPerson("Alice", "Anderson", "alice@example.com", LocalDate.of(2001, 1, 1), true);
        Person bob = createPerson("Bob", "Baker", "bob@example.com", LocalDate.of(2005, 5, 5), true);

        // Contracts
        Contract contractA = createContract(ContractStatus.STARTED, "Alice Employee Contract", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31), TimesheetFrequency.WEEKLY, null, 40.0);
        Contract contractB = createContract(ContractStatus.PREPARED, "Bob Employee Contract", LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31), TimesheetFrequency.MONTHLY, null, 38.0);

        // Link Roles to Persons
        Secretary secC = new Secretary();
        linkPersonAndRole(chef, secC);
        Supervisor supC = new Supervisor();
        linkPersonAndRole(chef, supC);

        Employee empA = new Employee();
        linkPersonAndRole(alice, empA);
        Secretary secA = new Secretary();
        linkPersonAndRole(alice, secA);

        Employee empB = new Employee();
        linkPersonAndRole(bob, empB);

        personDAO.save(chef);
        personDAO.save(alice);
        personDAO.save(bob);

        // Link Contracts to Roles
        // - Contracts need at least one Secretary
        // - Contracts have exactly one Employee and Supervisor
        linkContractAndRoles(contractA, supC, secC, empA, null);
        linkContractAndRoles(contractB, supC, secA, empB, null);

        contractDAO.save(contractA);
        contractDAO.save(contractB);
    }

    /**
     * Helper for one-line creation of a person.
     */
    private Person createPerson(String first, String last, String email, LocalDate dob, boolean consent) {
        Person p = new Person(true);
        p.setFirstName(first);
        p.setLastName(last);
        p.setEmailAddress(email);
        p.setDateOfBirth(dob);
        p.setConsent(consent);
        return p;
    }

    /**
     * Helper for one-line creation of a contract.
     */
    private Contract createContract(ContractStatus status, String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency frequency, LocalDate terminationDate, double hoursPerWeek) {
        Contract c = new Contract(true);
        c.setStatus(status);
        c.setName(name);
        c.setStartDate(startDate);
        c.setEndDate(endDate);
        c.setFrequency(frequency);
        c.setTerminationDate(terminationDate);
        c.setHoursPerWeek(hoursPerWeek);
        return c;
    }

    /**
     * Links a role to a person by setting both sides of the relationship.
     *
     * @param person the person to be added to the role
     * @param role   the role to be added to the person
     */
    private void linkPersonAndRole(Person person, Role role) {
        role.setPerson(person);
        person.getRoles().add(role);
    }

    /**
     * Links a contract to a role by setting both sides of the relationship.
     * Currently only supports adding one secretary and assistant at once.
     *
     * @param contract   the contract to be added to the role
     * @param supervisor the supervisor to be added to the contract (can be null)
     * @param secretary  the secretary to be added to the contract (can be null)
     * @param employee   the employee to be added to the contract (can be null)
     * @param assistant  the assistant to be added to the contract (can be null)
     */
    private void linkContractAndRoles(Contract contract, Supervisor supervisor, Secretary secretary, Employee employee, Assistant assistant) {
        if (supervisor != null) {
            supervisor.setContract(contract);
            contract.setSupervisor(supervisor);
        }
        if (secretary != null) {
            secretary.setContract(contract);
            contract.addSecretary(secretary);
        }
        if (employee != null) {
            employee.setContract(contract);
            contract.setEmployee(employee);
        }
        if (assistant != null) {
            assistant.setContract(contract);
            contract.addAssistant(assistant);
        }
    }
}
