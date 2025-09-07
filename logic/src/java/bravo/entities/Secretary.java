package bravo.entities;

import jakarta.persistence.*;


@Entity
@DiscriminatorValue("Secretary")
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


