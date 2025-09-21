package bravo.logic;

import bravo.dto.PersonDTO;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface PersonLogic {

    /**
     * Creates a new person record.
     *
     * @param person the person DTO to create
     * @return the created PersonDTO
     */
    PersonDTO create(PersonDTO person);

    /**
     * Updates an existing person record.
     *
     * @param person the person DTO with updated data
     * @return the updated PersonDTO
     */
    PersonDTO update(PersonDTO person);

    /**
     * Finds a person by their UUID.
     *
     * @param uuid the UUID of the person
     * @return the found PersonDTO or null if not found
     */
    PersonDTO findByUuid(String uuid);

    /**
     * Finds a person by their email address.
     *
     * @param email the email of the person
     * @return the found PersonDTO or null if not found
     */
    PersonDTO findByEmail(String email);

    /**
     * Retrieves all persons.
     *
     * @return a list of all PersonDTOs
     */
    List<PersonDTO> findAll();

    /**
     * Finds all role UUIDs assigned to a given person.
     *
     * @param person the person to query
     * @return a list of role UUIDs
     */
    List<String> findPersonRoles(PersonDTO person);

    /**
     * Finds a person by a specific role UUID.
     *
     * @param roleUuid the role UUID to search
     * @return the found PersonDTO or null if not found
     */
    PersonDTO findByRole(String roleUuid);

    /**
     * Finds persons by a list of role UUIDs.
     *
     * @param roleUuid list of role UUIDs
     * @return a list of PersonDTOs matching any of the roles
     */
    List<PersonDTO> findByRole(List<String> roleUuid);
}
