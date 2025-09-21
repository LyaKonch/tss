package bravo.dto;

import java.util.List;

public class UserDTO extends AbstractDTO {
    private String email;
    private List<String> groupName;
    private String password;

    public UserDTO() {
    }

    public UserDTO(String email, Long personId,  List<String> groupName) {
        this.email = email;
        this.groupName = groupName;
    }

    // getters / setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String username) {
        this.email = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public  List<String> getGroupName() {
        return groupName;
    }

    public void setGroupName( List<String> groupName) {
        this.groupName = groupName;
    }
}