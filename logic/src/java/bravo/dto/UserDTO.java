package bravo.dto;

/**
 * Data Transfer Object (DTO) for {@link bravo.entities.User} information.
 * <p>
 * Extends {@link AbstractDTO} to inherit common DTO properties such as {@code id}, {@code uuid}, and {@code version}.
 * Contains basic user details such as email, group, and password.
 */
public class UserDTO extends AbstractDTO {
    /** Email of the user, used as username. */
    private String email;
    
    /** Name of the user group this user belongs to. */
    private String groupName;
    
    /** Password for authentication purposes. */
    private String password;

    /** Default constructor. */
    public UserDTO() {
    }

    /**
     * Constructor with partial initialization.
     *
     * @param email      email of the user
     * @param personId   associated person ID (not used in this DTO)
     * @param groupName  name of the group
     */
    public UserDTO(String email, Long personId, String groupName) {
        this.email = email;
        this.groupName = groupName;
    }

    // --- Getters and Setters ---
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}