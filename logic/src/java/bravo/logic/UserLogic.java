package bravo.logic;

import bravo.dto.UserDTO;

import java.util.List;

public interface UserLogic {

    /**
     * Creates a new user.
     *
     * @param dto the user DTO to create
     * @return the created user DTO
     */
    UserDTO createUser(UserDTO dto);

    /**
     * Updates an existing user.
     *
     * @param dto the user DTO with updated data
     * @return the updated user DTO
     */
    UserDTO updateUser(UserDTO dto);

    /**
     * Deletes a user by its database ID.
     *
     * @param id the ID of the user to delete
     */
    void deleteUser(Long id);

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email
     * @return the found user DTO, or null if not found
     */
    UserDTO findByEmail(String email);

    /**
     * Finds a user by their database ID.
     *
     * @param id the user's ID
     * @return the found user DTO, or null if not found
     */
    UserDTO findById(Long id);

    /**
     * Finds all users
     * @return all found users DTO
     */
    List<UserDTO> findAll();
}
