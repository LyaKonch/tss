package bravo.entities;

import jakarta.persistence.*;


@Entity
@DiscriminatorValue("Assistant")
public class Assistant extends Role {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_contract_id")
    private Contract contract;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

}

