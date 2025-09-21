package bravo.logic.impl;

import bravo.dao.PersonDAO;
import bravo.dao.UserDAO;
import bravo.dao.UserGroupDAO;
import bravo.dto.UserDTO;
import bravo.entities.Person;
import bravo.entities.User;
import bravo.enums.GroupName;
import bravo.utitity.HashUtils;
import bravo.logic.UserLogic;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stateless EJB implementing {@link UserLogic}.
 * <p>
 * Provides business logic for managing users, including creation, update, deletion, and retrieval.
 * Handles mapping between users and groups, password hashing, and validation.
 */
@Stateless
public class UserLogicBean implements UserLogic {

    @Inject
    private UserDAO userDAO;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private UserGroupDAO userGroupDAO;

    /**
     * Finds a user by its database ID.
     *
     * @param id primary key of the user
     * @return corresponding {@link UserDTO} or null if not found
     */
    @Override
    public UserDTO findById(Long id) {
        User u = userDAO.findById(id);
        return toDTO(u);
    }

    /**
     * Finds a user by email (username).
     *
     * @param username user's email
     * @return corresponding {@link UserDTO} or null if not found
     */
    @Override
    public UserDTO findByEmail(String username) {
        User u = userDAO.findByEmail(username);
        return toDTO(u);
    }

    /**
     * Returns all users as DTOs.
     *
     * @return list of {@link UserDTO} objects
     */
    public List<UserDTO> findAll() {
        return userDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Creates a new user with email, password, and group mapping.
     * Validates that the user does not already exist and hashes the password.
     *
     * @param dto the user data transfer object
     * @return created {@link UserDTO}
     * @throws IllegalArgumentException if dto is null, missing password, or person not found
     * @throws IllegalStateException    if user with same email already exists
     */
    @Override
    @Transactional
    public UserDTO createUser(UserDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto is null");
        if (dto.getPassword() == null || dto.getPassword().isBlank())
            throw new IllegalArgumentException("password required");

        Person person = personDAO.findByEmailAddress(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Person not found by email"));

        if (userDAO.findByEmail(person.getEmailAddress()) != null) {
            throw new IllegalStateException("User for this email already exists: " + dto.getEmail());
        }

        GroupName groupName;
        try {
            groupName = GroupName.valueOf(dto.getGroupName());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid groupName: " + dto.getGroupName());
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(HashUtils.sha256Hex(dto.getPassword()));

        // Persist user
        user = userDAO.save(user);

        // Add mapping user -> group
        userGroupDAO.addMapping(user.getEmail(), groupName);

        return toDTO(user);
    }

    /**
     * Updates an existing user, including email, password, and group mapping.
     *
     * @param dto the user DTO with updated values
     * @return updated {@link UserDTO}
     * @throws IllegalArgumentException if dto is null or user/person not found
     */
    @Override
    @Transactional
    public UserDTO updateUser(UserDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto is null");

        User user = userDAO.findByEmail(dto.getEmail());
        if (user == null) throw new IllegalArgumentException("User not found: " + dto.getId());

        Person person = personDAO.findByEmailAddress(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Person not found by email"));

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(person.getEmailAddress());
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(HashUtils.sha256Hex(dto.getPassword()));
        }

        if (dto.getGroupName() != null && !dto.getGroupName().isBlank()) {
            GroupName newGroup;
            try {
                newGroup = GroupName.valueOf(dto.getGroupName());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid groupName: " + dto.getGroupName());
            }

            userGroupDAO.deleteAllMappingsForUser(user.getEmail());
            userGroupDAO.addMapping(user.getEmail(), newGroup);
        }

        user = userDAO.save(user);
        return toDTO(user);
    }

    /**
     * Deletes a user by ID and removes all group mappings.
     *
     * @param id primary key of the user
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userDAO.findById(id);
        if (user == null) return;

        userGroupDAO.deleteAllMappingsForUser(user.getEmail());
        userDAO.deleteById(id);
    }

    /**
     * Converts a {@link User} entity to {@link UserDTO}.
     *
     * @param user the user entity
     * @return corresponding {@link UserDTO} or null if user is null
     */
    private UserDTO toDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUuid(user.getUuid() != null ? user.getUuid().toString() : null);
        dto.setEmail(user.getEmail());

        List<GroupName> groups = userGroupDAO.findGroupsByEmail(user.getEmail());
        dto.setGroupName(groups.isEmpty() ? null : groups.get(0).name());

        return dto;
    }
}