
package bravo.logic;

import jakarta.ejb.Local;

@Local
public interface EmailService {
    /**
     * Sends an email with the given subject and body to the specified recipient.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body content
     */
    void sendEmail(String to, String subject, String body);
}
