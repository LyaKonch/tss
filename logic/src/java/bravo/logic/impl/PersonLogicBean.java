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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stateless EJB handling business logic for Person entities.
 */
@Stateless
public class PersonLogicBean implements PersonLogic {

    @EJB
    private PersonDAO personDAO;

    @Inject
    private PersonMapper personMapper;

    @Inject
    private RoleDAO roleDao;

    // --- Methods ---

    /** Creates a new person in the database. */
    @Override
    public PersonDTO create(PersonDTO person) {
        Person entity = personMapper.toEntity(person);
        personDAO.save(entity);
        return personMapper.toDto(entity);
    }

    /** Updates an existing person based on the provided DTO. */
    @Override
    public PersonDTO update(PersonDTO person) {
        Person entity = personDAO.findByUuid(person.getUuid());
        personMapper.updateExistingPersonFromDto(person, entity);
        personDAO.save(entity);
        return personMapper.toDto(entity);
    }
/** Retrieves a person by their UUID. */
    @Override
    public PersonDTO findByUuid(String uuid) {
        Person entity = personDAO.findByUuid(uuid);
        return personMapper.toDto(entity);
    }

    /** Retrieves a person by their email address. */
    @Override
    public PersonDTO findByEmail(String email) {
        Person entity = personDAO.findByEmailAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("Person with email " + email + " not found."));
        return personMapper.toDto(entity);
    }

    /** Retrieves all persons in the database. */
    @Override
    public List<PersonDTO> findAll() {
        List<Person> entities = personDAO.findAll();
        List<PersonDTO> dtos = new java.util.ArrayList<>(entities.size());
        for (Person entity : entities) dtos.add(personMapper.toDto(entity));
        return dtos;
    }

    /** Returns the UUIDs of all roles associated with the given person. */
    @Override
    public List<String> findPersonRoles(PersonDTO person) {
        List<Role> personRoles = roleDao.findByPerson(personMapper.toEntity(person));
        return personRoles.stream()
                .map(role -> role.getUuid().toString())
                .collect(Collectors.toList());
    }

    /** Returns the person associated with a specific role UUID. */
    @Override
    public PersonDTO findByRole(String roleUuid) {
        if (roleUuid == null) return null;

        Role role = roleDao.findByUuid(roleUuid);
        if (role == null) return null;

        Person person = role.getPerson();
        if (person == null) return null;

        return personMapper.toDto(person);
    }

    /** Returns a list of persons associated with the given list of role UUIDs. */
    @Override
    public List<PersonDTO> findByRole(List<String> roleUuids) {
        if (roleUuids == null || roleUuids.isEmpty()) return List.of();

        return roleUuids.stream()
                .map(uuid -> {
                    if (uuid == null) return null;

                    Role role = roleDao.findByUuid(uuid);
                    if (role == null) return null;

                    Person person = role.getPerson();
                    if (person == null) return null;

                    return personMapper.toDto(person);
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}