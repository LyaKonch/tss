package bravo.logic;

import bravo.dto.UserDTO;

public interface UserLogic {

    UserDTO createUser(UserDTO dto);

    UserDTO updateUser(UserDTO dto);

    void deleteUser(Long id);

    UserDTO findByEmail(String email);

    UserDTO findById(Long id);
}
