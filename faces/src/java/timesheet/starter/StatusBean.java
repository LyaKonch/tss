package timesheet.starter;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class StatusBean {
    @EJB
    private TimesheetStatusService statusService;

    public List<String> getStatuses() {
        return statusService.getAvailableStatuses();
    }
}
