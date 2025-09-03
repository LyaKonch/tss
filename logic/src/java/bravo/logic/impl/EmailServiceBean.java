package bravo.logic.impl;

import bravo.logic.EmailService;
import com.sun.istack.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;

import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Stateless
public class EmailServiceBean implements EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailServiceBean.class);

    @Resource(name = "mail/TSSMailSession")
    private Session mailSession;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            Message message = new MimeMessage(mailSession);

            message.setFrom(new InternetAddress(mailSession.getProperty("mail.user")));

            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            LOGGER.info("Sending email to: " + to + " with subject: " + subject);

        } catch (MessagingException e) {
            LOGGER.severe("Error sending email: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }

    }
}
