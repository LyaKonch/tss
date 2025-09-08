package bravo.dao;

import bravo.entities.Contract;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

@LocalBean
@Stateless
public class ContractDAO extends AbstractDAO<Contract> {

    public ContractDAO() {
        super(Contract.class);
    }

    public Contract findByEmail(String name) {
        return em.createNamedQuery("Contract.findByName", Contract.class)
                .setParameter("name", name)
                .getSingleResult();
    }
}
