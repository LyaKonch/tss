package bravo.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Supervisor")
@NamedQuery(name = "Supervisor.findByContract",
        query = "SELECT s FROM Supervisor s WHERE s.contract = :contract")
public class Supervisor extends Role {

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
