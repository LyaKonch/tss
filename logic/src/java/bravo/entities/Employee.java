package bravo.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Employee")
@NamedQuery(name = "Employee.findByContract",
        query = "SELECT s FROM Employee s WHERE s.contract = :contract")
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
