package bravo.entities;

import jakarta.persistence.*;

/**
 * Entity representing a Secretary role in the system.
 * A Secretary is associated with exactly one Contract.
 */
@Entity
@DiscriminatorValue("Secretary")
@NamedQuery(name = "Secretary.findByContract",
        query = "SELECT s FROM Secretary s WHERE s.contract = :contract")
public class Secretary extends Role {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    /**
     * Returns the contract associated with this Secretary.
     *
     * @return the Contract entity linked to this Secretary
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Sets the contract associated with this Secretary.
     *
     * @param contract the Contract entity to associate
     */
    public void setContract(Contract contract) {
        this.contract = contract;
    }

}


