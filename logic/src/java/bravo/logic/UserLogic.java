package bravo.logic;

import bravo.dto.UserDTO;

import java.util.List;

public interface UserLogic {

    UserDTO createUser(UserDTO dto);

    UserDTO updateUser(UserDTO dto);

    void deleteUser(Long id);

    UserDTO findByEmail(String email);

    UserDTO findById(Long id);

    List<UserDTO> findAll();
}
