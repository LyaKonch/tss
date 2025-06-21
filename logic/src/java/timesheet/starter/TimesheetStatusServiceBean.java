package timesheet.starter;

import jakarta.ejb.Stateless;

import java.util.Arrays;
import java.util.List;

@Stateless
public class TimesheetStatusServiceBean implements TimesheetStatusService {
    @Override
    public List<String> getAvailableStatuses() {
        return Arrays.asList("Started", "Running", "Expanding");
    }
}
