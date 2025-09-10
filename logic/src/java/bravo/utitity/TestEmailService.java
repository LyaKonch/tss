package bravo.utitity;

import bravo.logic.EmailService;
import bravo.logic.impl.EmailServiceBean;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

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
