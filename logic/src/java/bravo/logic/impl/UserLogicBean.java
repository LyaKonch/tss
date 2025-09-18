package bravo.logic.impl;

import bravo.dao.PersonDAO;
import bravo.dao.UserDAO;
import bravo.dao.UserGroupDAO;
import bravo.dto.UserDTO;
import bravo.entities.Person;
import bravo.entities.User;

import bravo.enums.GroupName;
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
    private UserDAO userDAO;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private UserGroupDAO userGroupDAO;

    @Override
    public UserDTO findById(Long id) {
        User u = userDAO.findById(id);
        return toDTO(u);
    }

    @Override
    public UserDTO findByEmail(String username) {
        User u = userDAO.findByEmail(username);
        return toDTO(u);
    }

    public List<UserDTO> findAll() {
        return userDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO dto) {
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
        GroupName groupName;
        try {
            groupName = GroupName.valueOf(dto.getGroupName());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid groupName: " + dto.getGroupName());
        }


        User user = new User();

        user.setEmail(dto.getEmail());
        user.setPassword(HashUtils.sha256Hex(dto.getPassword()));

        // persist user
        user = userDAO.save(user);

        // add mapping user->group
        userGroupDAO.addMapping(user.getEmail(), groupName);

        return toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UserDTO dto) {
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

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userDAO.findById(id);
        if (user == null) return;

        userGroupDAO.deleteAllMappingsForUser(user.getEmail());
        userDAO.deleteById(id);
    }

    private UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUuid(user.getUuid() != null ? user.getUuid().toString() : null);
        dto.setEmail(user.getEmail());
        // get groups for dto (if you want single group, take first or null)
        List<GroupName> groups = userGroupDAO.findGroupsByEmail(user.getEmail());
        dto.setGroupName(groups.isEmpty() ? null : groups.get(0).name());
        return dto;
    }

}
