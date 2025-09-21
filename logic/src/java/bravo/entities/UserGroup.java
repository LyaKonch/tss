package bravo.entities;

import bravo.enums.GroupName;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entity representing a mapping between a user and a group.
 * Uses a composite primary key (UserGroupId).
 *
 * <p>Includes convenience getters for email and group name.</p>
 */
@Entity
@Table(name = "user_groups")
public class UserGroup {

    @EmbeddedId
    private UserGroupId id;

    public UserGroup() {
    }

    public UserGroup(UserGroupId id) {
        this.id = id;
    }

    public UserGroup(String username, GroupName groupName) {
        this.id = new UserGroupId(username, groupName);
    }

    // Getter and setter for the composite ID
    public UserGroupId getId() {
        return id;
    }

    public void setId(UserGroupId id) {
        this.id = id;
    }

    // Convenience getters
    public String getEmail() {
        return id != null ? id.getEmail() : null;
    }

    public String getGroupName() {
        return id != null ? id.getGroupName().name() : null;
    }
}