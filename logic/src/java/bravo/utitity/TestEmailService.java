package bravo.utitity;

import bravo.logic.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

/**
 * TestEmailService is a startup singleton used to verify that the {@link EmailService}
 * is configured and working correctly.
 * <p>
 * When the application starts, it automatically sends a test email to a predefined
 * recipient. This helps confirm that the email subsystem is functioning as expected.
 * <p>
 * !️ This class is intended for development and testing only.
 * It should not be deployed in production environments.
 */
@Singleton
@Startup
public class TestEmailService {

    @EJB
    private EmailService emailService;

    public TestEmailService() {
    }

    @PostConstruct
    public void testEmailService() {
        String fullBody = "\nHello,\nI'm working as intented!";
        String subject = "Test: Timesheet System Notification";
        String recipientMail = "yotrokh@uni-koblenz.de";
        emailService.sendEmail(recipientMail, subject, fullBody);
    }

}
