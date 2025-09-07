package bravo.utitity;

import bravo.dao.ContractDAO;
import bravo.dao.PersonDAO;
import bravo.dao.RoleDAO;
import bravo.entities.*;
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
        // Person C
        Person chef = new Person(true);
        chef.setFirstName("Chef");
        chef.setLastName("Zero");
        chef.setEmailAddress("chef@example.com");
        chef.setDateOfBirth(LocalDate.of(1995, 9, 5));
        chef.setConsent(true);

        // Person A
        Person alice = new Person(true);
        alice.setFirstName("Alice");
        alice.setLastName("Anderson");
        alice.setEmailAddress("alice@example.com");
        alice.setDateOfBirth(LocalDate.of(2001, 1, 1));
        alice.setConsent(true);

        // Person B
        Person bob = new Person(true);
        bob.setFirstName("Bob");
        bob.setLastName("Baker");
        bob.setEmailAddress("bob@example.com");
        bob.setDateOfBirth(LocalDate.of(2005, 5, 5));
        bob.setConsent(true);

        // Link Roles to Persons
        Secretary secC = new Secretary();
        linkRoleToPerson(chef, secC);
        Supervisor supC = new Supervisor();
        linkRoleToPerson(chef, supC);

        Employee empA = new Employee();
        linkRoleToPerson(alice, empA);
        Secretary secA = new Secretary();
        linkRoleToPerson(alice, secA);

        Employee empB = new Employee();
        linkRoleToPerson(bob, empB);


        // Contracts
        Contract ContractA = createExampleContract("Alice Employee Contract");
        Contract ContractB = createExampleContract("Bob Employee Contract");

        // Link Contracts to Roles
        // - Contracts need at least one Secretary
        // - Contracts have exactly one Employee and Supervisor
        linkContractToRoles(ContractA, supC, secC, empA, null);
        linkContractToRoles(ContractB, supC, secA, empB, null);
    }

    private Contract createExampleContract(String name) {
        Contract c = new Contract();
        c.setName(name);
        return c;
    }

    /**
     * Links a role to a person by setting both sides of the relationship.
     *
     * @param person the person to be added to the role
     * @param role   the role to be added to the person
     */
    private void linkRoleToPerson(Person person, Role role) {
        role.setPerson(person);
        person.getRoles().add(role);
        personDAO.save(person);
        // Cannot persist the role yet - roles require contracts to be set
    }

    /**
     * Links a contract to a role by setting both sides of the relationship.
     *
     * @param contract   the contract to be added to the role
     * @param supervisor the supervisor to be added to the contract (can be null)
     * @param secretary  the secretary to be added to the contract (can be null)
     * @param employee   the employee to be added to the contract (can be null)
     * @param assistant  the assistant to be added to the contract (can be null)
     */
    private void linkContractToRoles(Contract contract, Supervisor supervisor, Secretary secretary, Employee employee, Assistant assistant) {
        if (supervisor != null) {
            supervisor.setContract(contract);
            contract.setSupervisor(supervisor);
            roleDAO.save(supervisor);
        }
        if (secretary != null) {
            secretary.setContract(contract);
            contract.addSecretary(secretary);
            roleDAO.save(secretary);
        }
        if (employee != null) {
            employee.setContract(contract);
            contract.setEmployee(employee);
            roleDAO.save(employee);
        }
        if (assistant != null) {
            assistant.setContract(contract);
            contract.addAssistant(assistant);
            roleDAO.save(assistant);
        }

        contractDAO.save(contract);

    }
}
