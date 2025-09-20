package bravo.logic;


import bravo.dto.PersonDTO;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface PersonLogic {

    PersonDTO create(PersonDTO person);

    PersonDTO update(PersonDTO person);

    PersonDTO findByUuid(String uuid);

    PersonDTO findByEmail(String email);

    List<PersonDTO> findAll();
}
