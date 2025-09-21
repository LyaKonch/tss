package bravo.logic.impl;

import bravo.dao.PersonDAO;
import bravo.dao.UserDAO;
import bravo.dao.UserGroupDAO;
import bravo.dto.UserDTO;
import bravo.entities.Person;
import bravo.entities.User;

import bravo.enums.GroupName;
import bravo.utitity.AuthorizationService;
import bravo.utitity.HashUtils;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


import bravo.logic.UserLogic;

@Stateless
public class UserLogicBean implements UserLogic {

    @Inject
    private AuthorizationService authService;

    @Inject
    private UserDAO userDAO;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private UserGroupDAO userGroupDAO;

    @Override
    public UserDTO findById(Long id) {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can get users info.");
        }
        User u = userDAO.findById(id);
        u.setPassword(null);
        return toDTO(u);
    }

    @Override
    public UserDTO findByEmail(String username) {
        //if (!authService.isUserInRole("ADMIN")) {
        //    throw new SecurityException("Only ADMIN can get users info.");
        //}
        User u = userDAO.findByEmail(username);
        return toDTO(u);
    }

    @Override
    public List<UserDTO> findAll() {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can get users info.");
        }
        return userDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO dto) {
        // for default user creation at startup, normally we have it
        //if (!authService.isUserInRole("ADMIN")) {
        //    throw new SecurityException("Only ADMIN can create user.");
        //}
        if (dto == null) throw new IllegalArgumentException("dto is null");
        if (dto.getPassword() == null || dto.getPassword().isBlank()) throw new IllegalArgumentException("password required");

        Optional<Person> personOptional = personDAO.findByEmailAddress(dto.getEmail());
        Person person;

        try{
            person = personOptional.get();
        }catch(NoSuchElementException e){
            throw new IllegalArgumentException("Person wasn't not found by email");
        }

        if (userDAO.findByEmail(person.getEmailAddress()) != null) {
            throw new IllegalStateException("User for this email already exists: " + dto.getEmail());
        }

        // groupName from dto
        if (dto.getGroupName() == null || dto.getGroupName().isEmpty()) {
            throw new IllegalArgumentException("At least one user group is required.");
        }
        List<GroupName> groupNames = dto.getGroupName().stream()
                .map(GroupName::valueOf)
                .collect(Collectors.toList());


        User user = new User();

        user.setEmail(dto.getEmail());
        user.setPassword(HashUtils.sha256Hex(dto.getPassword()));

        // persist user
        user = userDAO.save(user);

        // add mapping user->group
        for (GroupName groupName : groupNames) {
            userGroupDAO.addMapping(user.getEmail(), groupName);
        }

        return toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UserDTO dto) {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can update user.");
        }
        if (dto == null) throw new IllegalArgumentException("dto is null");
        User user = userDAO.findByEmail(dto.getEmail());
        if (user == null) throw new IllegalArgumentException("User not found: " + dto.getId());

        Optional<Person> personOptional = personDAO.findByEmailAddress(dto.getEmail());
        Person person;

        try{
            person = personOptional.get();
        }catch(NoSuchElementException e){
            throw new IllegalArgumentException("Person wasn't not found by email");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(person.getEmailAddress());
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(HashUtils.sha256Hex(dto.getPassword()));
        }

        if (dto.getGroupName() != null && !dto.getGroupName().isEmpty()) {
            List<GroupName> newGroups = dto.getGroupName().stream()
                    .map(GroupName::valueOf)
                    .collect(Collectors.toList());

            userGroupDAO.deleteAllMappingsForUser(user.getEmail());
            for (GroupName groupName : newGroups) {
                userGroupDAO.addMapping(user.getEmail(), groupName);
            }
        }

        user = userDAO.save(user);
        return toDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!authService.isUserInRole("ADMIN")) {
            throw new SecurityException("Only ADMIN can delete user.");
        }
        User user = userDAO.findById(id);
        if (user == null) return;

        personDAO.delete(personDAO.findByEmailAddress(user.getEmail()).get());
        userGroupDAO.deleteAllMappingsForUser(user.getEmail());
        userDAO.deleteById(id);

    }

    private UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUuid(user.getUuid() != null ? user.getUuid().toString() : null);
        dto.setEmail(user.getEmail());
        // we dont even send hashed password to front beans, so we are good.
        // even admin can't see password, only update
        List<GroupName> groups = userGroupDAO.findGroupsByEmail(user.getEmail());
        dto.setGroupName(groups.stream()
                .map(GroupName::name)
                .collect(Collectors.toList()));
        return dto;
    }

}
