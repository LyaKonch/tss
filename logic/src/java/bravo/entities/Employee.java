package bravo.entities;

import jakarta.persistence.*;

/**
 * JPA Entity representing an Employee role.
 * <p>
 * An Employee is associated with exactly one Contract.
 * This class extends Role and is a concrete discriminator value in the Role hierarchy.
 */
@Entity
@DiscriminatorValue("Employee")
@NamedQuery(name = "Employee.findByContract",
        query = "SELECT s FROM Employee s WHERE s.contract = :contract")
public class Employee extends Role {

    /**
     * The contract associated with this Employee.
     * Each Employee is linked to exactly one Contract.
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    /**
     * Gets the contract associated with this employee.
     *
     * @return the contract
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Sets the contract associated with this employee.
     * <p>
     * Note: This does not automatically update the bidirectional reference in Contract.
     *
     * @param contract the contract to set
     */
    public void setContract(Contract contract) {
        this.contract = contract;
    }

}
