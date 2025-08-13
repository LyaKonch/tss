package bravo.entities;

import jakarta.persistence.*;

@Entity
@Table(name= "roles")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role_type")
public abstract class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
 
     //@ManyToOne(optional = false, fetch = FetchType.LAZY)
//@JoinColumn(name = "contract_id", nullable = false)
//private Contract contract;


    // Get  Set

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    //public Contract getContract() {
       // return contract;
    //}

    //public void setContract(Contract contract) {
      //  this.contract = contract;
    //}
    
}

