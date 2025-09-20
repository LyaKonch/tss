package bravo.logic.impl;

import bravo.dao.PersonDAO;
import bravo.dao.RoleDAO;
import bravo.dto.PersonDTO;
import bravo.entities.Person;
import bravo.entities.Role;
import bravo.logic.PersonLogic;
import bravo.mapper.PersonMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class PersonLogicBean implements PersonLogic {

    @EJB
    private PersonDAO personDAO;

    @Inject
    private PersonMapper personMapper;

    @Inject
    private RoleDAO roleDao;

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
    public PersonDTO findByEmail(String email) {
        Person entity = personDAO.findByEmailAddress(email).orElseThrow(() -> new IllegalArgumentException("Person with email " + email + " not found."));
        return personMapper.toDto(entity);
    }

    @Override
    public List<PersonDTO> findAll() {
        List<Person> entities = personDAO.findAll();
        List<PersonDTO> dtos = new java.util.ArrayList<>(entities.size());
        for (Person entity : entities) dtos.add(personMapper.toDto(entity));
        return dtos;
    }

    @Override
    public PersonDTO findByEmail(String email){
        Optional<Person> entity = personDAO.findByEmailAddress(email);
        return entity.map(person -> personMapper.toDto(person)).orElse(null);
    }

    // returns roles uuid for person
    @Override
    public List<String> findPersonRoles(PersonDTO person) {
        List<Role> personRoles = roleDao.findByPerson(personMapper.toEntity(person));
        return personRoles.stream().map(role -> role.getUuid().toString()).collect(Collectors.toList());
    }

    // returns person by role uuid
    @Override
    public PersonDTO findByRole(String roleUuid) {
        if (roleUuid == null) {
            return null;
        }
        Role role = roleDao.findByUuid(roleUuid);
        if (role == null) {
            return null;
        }
        Person person = role.getPerson();
        if (person == null) {
            return null;
        }

        return personMapper.toDto(person);
    }

    // returns persons by role uuids
    @Override
    public List<PersonDTO> findByRole(List<String> roleUuids) {
        if (roleUuids == null || roleUuids.isEmpty()) {
            return List.of();
        }

        return roleUuids.stream()
                .map(uuid -> {
                    if (uuid == null) {
                        return null;
                    }

                    Role role = roleDao.findByUuid(uuid);
                    if (role == null) {
                        return null;
                    }

                    Person person = role.getPerson();
                    if (person == null) {
                        return null;
                    }

                    return personMapper.toDto(person);
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
