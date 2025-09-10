package bravo.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Employee")
public class Employee extends Role {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

}
