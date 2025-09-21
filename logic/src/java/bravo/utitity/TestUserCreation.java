package bravo.utitity;

import bravo.dto.UserDTO;
import bravo.entities.Person;
import bravo.enums.GroupName;
import bravo.logic.UserLogic;
import bravo.dao.PersonDAO;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Creates initial users at application startup if they do not exist.
 *<p>
 * This ensures that an ADMIN and a USER account are present in the system
 * to allow immediate access after deployment.
 */
@Singleton
@Startup
public class TestUserCreation {

    private static final Logger log = Logger.getLogger(TestUserCreation.class.getName());

    @Inject
    private UserLogic userLogic;

    @Inject
    private PersonDAO personDAO;

    @PostConstruct
    public void init() {
        try {
            ensureUser("chef@example.com", GroupName.ADMIN, "admin123");

            ensureUser("bob@example.com", GroupName.USER, "user123");

            log.info("Initial users ensured");
        } catch (Exception e) {
            log.severe("Failed to create initial users: " + e.getMessage());
        }
    }

    private void ensureUser(String email, GroupName group, String password) {
        Person person = personDAO.findByEmailAddress(email).orElse(null);
        if (person == null) {
            log.warning("No Person found with email=" + email + " → cannot create user");
            return;
        }

        if (userLogic.findByEmail(email) != null) {
            log.info("User already exists for person " + email);
            return;
        }

        UserDTO dto = new UserDTO();
        dto.setEmail(email);
        dto.setGroupName(group.name());
        dto.setPassword(password);

        UserDTO created = userLogic.createUser(dto);
        log.info("Created initial user: " + created.getEmail() + " with group " + created.getGroupName());
    }
}
