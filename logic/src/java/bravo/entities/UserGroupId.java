package bravo.entities;

import java.io.Serializable;
import java.util.Objects;

import bravo.enums.GroupName;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Composite primary key for UserGroup entity.
 * Consists of user email and group name.
 */
@Embeddable
public class UserGroupId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "group_name", length = 100)
    @Enumerated(EnumType.STRING)
    private GroupName groupName;

    public UserGroupId() {}

    public UserGroupId(String email, GroupName groupName) {
        this.email = email;
        this.groupName = groupName;
    }

    // --getter and setter--
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public GroupName getGroupName() {
        return groupName;
    }

    public void setGroupName(GroupName groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGroupId)) return false;
        UserGroupId that = (UserGroupId) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, groupName);
    }
}