package bravo.logic.impl;

import bravo.dao.PersonDAO;
import bravo.dao.TimesheetDAO;
import bravo.entities.Assistant;
import bravo.entities.Person;
import bravo.entities.Secretary;
import bravo.entities.Timesheet;
import bravo.enums.TimesheetStatus;
import bravo.logic.EmailService;
import com.sun.istack.logging.Logger;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//RE1 MIN On the last day of a time sheet (either end of week or end of month), the TSS SHALL send
//a reminder mail to the employee if the time sheet is in state IN_PROGRESS.
//RE2 The TSS SHALL send a reminder mail to the supervisor and the assistants if the time sheet
//is in state SIGNED_BY_EMPLOYEE. (then, the supervisor may reject or sign the time
//sheet)
//RE3 The TSS SHALL send a reminder mail to the secretaries if the time sheet is in state
//SIGNED_BY_SUPERVISOR.
//RE4 The TSS SHALL repeat reminders every day.
//RE5 The TSS SHOULD collect all reminders so that a person receives at most one e-mail per
//day. 
@Singleton
@Startup
public class ReminderScheduler {

    private static final Logger LOGGER = Logger.getLogger(ReminderScheduler.class);

    @EJB
    private TimesheetDAO timesheetDAO;

    @EJB
    private PersonDAO personDAO;

    @EJB
    private EmailService emailService;

    //RE4 remind every day
    @Schedule(hour = "10", minute = "0", persistent = false)
    public void sendReminders() {
        LOGGER.info("ReminderScheduler: Checking for overdue timesheets...");
        LocalDate today = LocalDate.now();

        // direct implication of RE5, we should collect all reminders
        // I've chosen the structure of hashmap: key(email of a person)-> value(email body)
        // then i iterate through this map in the end of this function, accumulate all email bodys and send only one mail per person
        Map<String, StringBuilder> Reminders = new HashMap<>();

        // RE1 remind employee for IN_PROGRESS timesheet
        List<Timesheet> inProgressTimesheets = timesheetDAO.findbyStatus(TimesheetStatus.IN_PROGRESS);
        for (Timesheet timesheet : inProgressTimesheets) {
            if (timesheet.getEndDate().isEqual(today)) {
                //!!! i suspect below is not a good way to get a person. does this functionality have to be accessed through personDAO?
                Person employee = timesheet.getContract().getEmployee().getPerson();
                String email = employee.getEmailAddress();
                String reminderBody = String.format(
                        "Hi %s,\n\nYour timesheet for the period %s - %s is IN_PROGRESS and overdue. "
                        + "Please complete and submit it as soon as possible.\n\n",
                        employee.getFirstName(), timesheet.getStartDate(), timesheet.getEndDate());

                Reminders.computeIfAbsent(email, k -> new StringBuilder())
                        .append(reminderBody);
            }
        }

        // RE2 notify supervisors and assistants if employee signed the contract
        List<Timesheet> signedByEmployeeTimesheets = timesheetDAO.findbyStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        for (Timesheet timesheet : signedByEmployeeTimesheets) {
            Person employee = timesheet.getContract().getEmployee().getPerson();
            // Remind the supervisor
            Optional<Person> supervisor = Optional.ofNullable(timesheet.getContract().getSupervisor().getPerson());
            supervisor.ifPresent(s -> {
                String email = s.getEmailAddress();
                String reminderBody = String.format(
                        "Hi %s,\n\nThe timesheet for employee %s %s for the period %s - %s has been SIGNED_BY_EMPLOYEE and is awaiting your review.\n\n",
                        s.getFirstName(),
                        employee.getFirstName(), employee.getLastName(),
                        timesheet.getStartDate(), timesheet.getEndDate());

                Reminders.computeIfAbsent(email, k -> new StringBuilder())
                        .append(reminderBody);
            });

            // gathering persons of assistants
            List<Person> assistants = timesheet.getContract().getAssistants().stream().map(Assistant::getPerson).collect(Collectors.toList());
            for (Person assistant : assistants) {
                String email = assistant.getEmailAddress();
                String reminderBody = String.format(
                        "Hi %s,\n\nThe timesheet for employee %s %s for the period %s - %s has been SIGNED_BY_EMPLOYEE and is awaiting your review.\n\n",
                        assistant.getFirstName(),
                        employee.getFirstName(), employee.getLastName(),
                        timesheet.getStartDate(), timesheet.getEndDate());

                Reminders.computeIfAbsent(email, k -> new StringBuilder())
                        .append(reminderBody);
            }
        }

        // RE3 notify secretaries when contracts proceed to SIGNED_BY_SUPERVISOR
        List<Timesheet> signedBySupervisorTimesheets = timesheetDAO.findbyStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        for (Timesheet timesheet : signedBySupervisorTimesheets) {
            // gathering persons of secretaries
            List<Person> secretaries = timesheet.getContract().getSecretaries().stream().map(Secretary::getPerson).collect(Collectors.toList());;
            for (Person secretary : secretaries) {
                String email = secretary.getEmailAddress();
                Person employee = timesheet.getContract().getEmployee().getPerson();
                String reminderBody = String.format(
                        "Hi %s,\n\nThe timesheet for employee %s %s for the period %s - %s has been SIGNED_BY_SUPERVISOR and is ready for final processing.\n\n",
                        secretary.getFirstName(),
                        employee.getFirstName(), employee.getLastName(),
                        timesheet.getStartDate(), timesheet.getEndDate());

                Reminders.computeIfAbsent(email, k -> new StringBuilder())
                        .append(reminderBody);
            }
        }

        // RE5 sending all consolidated reminders
        if (emailService != null) {
            for (Map.Entry<String, StringBuilder> entry : Reminders.entrySet()) {
                String recipientEmail = entry.getKey();
                String fullBody = entry.getValue().toString() + "\nBest regards,\nThe TSS System";
                String subject = "Reminder: Timesheet System Notification";
                emailService.sendEmail(recipientEmail, subject, fullBody);
            }
        }
    }
}
