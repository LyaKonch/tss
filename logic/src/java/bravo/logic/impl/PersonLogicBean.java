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
import bravo.utitity.AuthorizationService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class PersonLogicBean implements PersonLogic {

    @Inject
    private AuthorizationService authService;

    @EJB
    private PersonDAO personDAO;

    @Inject
    private PersonMapper personMapper;

    @Inject
    private RoleDAO roleDao;

    /** Creates a new person in the database. */
    @Override
    public PersonDTO create(PersonDTO person) {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can create persons.");
        }
        Person entity = personMapper.toEntity(person);
        personDAO.save(entity);
        return personMapper.toDto(entity);
    }


    /** Updates an existing person based on the provided DTO. */
    @Override
    public PersonDTO update(PersonDTO person) {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can update persons.");
        }
        Person entity = personDAO.findByUuid(person.getUuid());
        personMapper.updateExistingPersonFromDto(person, entity);
        personDAO.save(entity);
        return personMapper.toDto(entity);
    }

    /** Retrieves a person by their UUID. */
    @Override
    public PersonDTO findByUuid(String uuid) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        Person entity = personDAO.findByUuid(uuid);
        return personMapper.toDto(entity);
    }

    /** Retrieves all persons in the database. */
    @Override
    public List<PersonDTO> findAll() {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        List<Person> entities = personDAO.findAll();
        List<PersonDTO> dtos = new java.util.ArrayList<>(entities.size());
        for (Person entity : entities) dtos.add(personMapper.toDto(entity));
        return dtos;
    }

    /** Returns the person associated with the given email. */
    @Override
    public PersonDTO findByEmail(String email){
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        Optional<Person> entity = personDAO.findByEmailAddress(email);
        return entity.map(person -> personMapper.toDto(person)).orElse(null);
    }

    /** Returns the UUIDs of all roles associated with the given person. */
    @Override
    public List<String> findPersonRoles(PersonDTO person) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
        List<Role> personRoles = roleDao.findByPerson(personMapper.toEntity(person));
        return personRoles.stream().map(role -> role.getUuid().toString()).collect(Collectors.toList());
    }

    /** Returns the person associated with a specific role UUID. */
    @Override
    public PersonDTO findByRole(String roleUuid) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }
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

    /** Returns the person associated with a specific role UUID. */
    @Override
    public List<PersonDTO> findByRole(List<String> roleUuids) {
        if (!authService.isAuthorized()) {
            throw new SecurityException("Unauthorized parties can't retrieve information.");
        }

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
