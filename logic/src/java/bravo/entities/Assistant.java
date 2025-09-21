package bravo.entities;

import jakarta.persistence.*;

/**
 * JPA Entity representing an Assistant role.
 * <p>
 * Extends {@link Role} and associates with a {@link Contract}.
 * Annotated with {@code @DiscriminatorValue("Assistant")} for single-table inheritance.
 */
@Entity
@DiscriminatorValue("Assistant")
@NamedQuery(name = "Assistant.findByContract",
        query = "SELECT s FROM Assistant s WHERE s.contract = :contract")
public class Assistant extends Role {

    /**
     * The contract associated with this assistant role.
     * Lazy-loaded to avoid fetching the contract unless explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    /**
     * Getter for the associated contract.
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Setter for the associated contract.
     */
    public void setContract(Contract contract) {
        this.contract = contract;
    }

}
