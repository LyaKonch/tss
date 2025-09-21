package bravo.mapper;

import bravo.dao.PersonDAO;
import bravo.dao.RoleDAO;
import bravo.dto.PersonDTO;
import bravo.entities.Person;
import bravo.entities.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between {@link Person} entities and {@link PersonDTO} objects.
 * <p>
 * Handles both simple fields (name, email, dateOfBirth, consent, state) and relationships
 * (roles). Can create new {@link Person} instances or update existing ones.
 */
@ApplicationScoped
public class PersonMapper extends AbstractMapper<Person, PersonDTO> {

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private PersonDAO personDAO;

    /**
     * Converts a {@link PersonDTO} to a {@link Person} entity.
     * <p>
     * If a person with the given UUID exists, it returns the existing entity.
     * Otherwise, a new entity is created. Also maps role relationships.
     *
     * @param dto the DTO to convert
     * @return the mapped {@link Person} entity
     */
    @Override
    public Person toEntity(PersonDTO dto) {
        Person maybe = personDAO.findByUuid(dto.getUuid());
        if (maybe != null) return maybe;
        Person entity = new Person();

        if (dto.getId() != null) entity.setId(dto.getId());
        if (dto.getUuid() != null) entity.setUuid(UUID.fromString(dto.getUuid()));

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setEmailAddress(dto.getEmailAddress());
        entity.setConsent(dto.isConsent());
        entity.setState(dto.getState());

        Set<Role> roles = dto.getRoleUuids().stream()
                .map(uuid -> roleDAO.findByUuid(uuid))
                .collect(Collectors.toSet());
        entity.setRoles(roles);

        return entity;
    }

    /**
     * Converts a {@link Person} entity to a {@link PersonDTO}.
     * <p>
     * Maps all simple fields and converts role relationships to UUIDs.
     *
     * @param entity the entity to convert
     * @return the mapped {@link PersonDTO}
     */
    @Override
    public PersonDTO toDto(Person entity) {
        PersonDTO dto = new PersonDTO();

        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().toString());

        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setEmailAddress(entity.getEmailAddress());
        dto.setConsent(entity.isConsent());
        dto.setState(entity.getState());

        Set<String> roles = entity.getRoles().stream()
                .map(role -> role.getUuid().toString())
                .collect(Collectors.toSet());
        dto.setRoleUuids(roles);

        return dto;
    }

    /**
     * Updates an existing {@link Person} entity with values from a {@link PersonDTO}.
     * <p>
     * Updates simple fields (name, email, dateOfBirth, consent, state) and synchronizes
     * role relationships: removes roles not in the DTO and adds new roles.
     *
     * @param dto            the source DTO
     * @param existingPerson the target entity to update
     */
    public void updateExistingPersonFromDto(PersonDTO dto, Person existingPerson) {
        // Map basic fields
        existingPerson.setFirstName(dto.getFirstName());
        existingPerson.setLastName(dto.getLastName());
        existingPerson.setDateOfBirth(dto.getDateOfBirth());
        existingPerson.setEmailAddress(dto.getEmailAddress());
        existingPerson.setConsent(dto.isConsent());

        existingPerson.setState(dto.getState());

        // Roles logic
        Set<String> newRoleUuids = dto.getRoleUuids();
        Set<Role> oldRoles = existingPerson.getRoles();

        // Find roles to remove
        Set<Role> rolesToRemove = oldRoles.stream()
                .filter(role -> newRoleUuids == null || !newRoleUuids.contains(role.getUuid().toString()))
                .collect(Collectors.toSet());

        // Remove old roles from the person's set and unlink the person
        rolesToRemove.forEach(role -> {
            roleDAO.delete(role);
            existingPerson.getRoles().remove(role);
        });

        // Find roles to add
        Set<String> oldRoleUuids = oldRoles.stream()
                .map(role -> role.getUuid().toString())
                .collect(Collectors.toSet());

        if (newRoleUuids != null) {
            newRoleUuids.stream()
                    .filter(uuid -> !oldRoleUuids.contains(uuid))
                    .forEach(uuid -> {
                        Role newRole = roleDAO.findByUuid(uuid);
                        if (newRole != null) {
                            newRole.getPerson().getRoles().remove(newRole);
                            existingPerson.addRole(newRole);
                        }
                    });
        }
    }
}
