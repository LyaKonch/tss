package bravo.entities;

import jakarta.persistence.*;


@Entity
@DiscriminatorValue("Assistant")
public class Assistant extends Role {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_contract_id", nullable = true, unique = true)
    private Contract contract;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

}

