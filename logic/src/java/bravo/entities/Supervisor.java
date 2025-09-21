package bravo.entities;

import jakarta.persistence.*;

/**
 * Entity representing a Supervisor role in the system.
 * A Supervisor is associated with exactly one Contract.
 */
@Entity
@DiscriminatorValue("Supervisor")
@NamedQuery(name = "Supervisor.findByContract",
        query = "SELECT s FROM Supervisor s WHERE s.contract = :contract")
public class Supervisor extends Role {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    /**
     * Returns the contract associated with this Supervisor.
     *
     * @return the Contract entity linked to this Supervisor
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Sets the contract associated with this Supervisor.
     *
     * @param contract the Contract entity to associate
     */
    public void setContract(Contract contract) {
        this.contract = contract;
    }

}


