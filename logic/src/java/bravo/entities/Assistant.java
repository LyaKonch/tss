package bravo.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Assistant")
@NamedQuery(name = "Assistant.findByContract",
        query = "SELECT s FROM Assistant s WHERE s.contract = :contract")
public class Assistant extends Role {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

}
