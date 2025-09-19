
package bravo.logic;

import jakarta.ejb.Local;

@Local
public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
