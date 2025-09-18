package bravo.logic.impl;

import bravo.dao.PersonDAO;
import bravo.dto.PersonDTO;
import bravo.entities.Person;
import bravo.logic.PersonLogic;
import bravo.mapper.PersonMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class PersonLogicBean implements PersonLogic {

    @EJB
    private PersonDAO personDAO;

    @Inject
    private PersonMapper personMapper;

    @Override
    public PersonDTO create(PersonDTO person) {
        Person entity = personMapper.toEntity(person);
        personDAO.save(entity);
        return personMapper.toDto(entity);
    }

    @Override
    public PersonDTO update(PersonDTO person) {
        Person entity = personDAO.findByUuid(person.getUuid());
        personMapper.updateExistingPersonFromDto(person, entity);
        personDAO.save(entity);
        return personMapper.toDto(entity);
    }

    @Override
    public PersonDTO findByUuid(String uuid) {
        Person entity = personDAO.findByUuid(uuid);
        return personMapper.toDto(entity);
    }

    @Override
    public List<PersonDTO> findAll() {
        List<Person> entities = personDAO.findAll();
        List<PersonDTO> dtos = new java.util.ArrayList<>(entities.size());
        for (Person entity : entities) dtos.add(personMapper.toDto(entity));
        return dtos;
    }
}
