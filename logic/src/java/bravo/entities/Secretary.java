package bravo.entities;

import jakarta.persistence.*;


@Entity
@DiscriminatorValue("Secretary")
@NamedQuery(name = "Secretary.findByContract",
        query = "SELECT s FROM Secretary s WHERE s.contract = :contract")
public class Secretary extends Role {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

}


