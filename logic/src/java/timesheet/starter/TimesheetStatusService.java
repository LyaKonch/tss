package timesheet.starter;

import jakarta.ejb.Local;

import java.util.List;

@Local
public interface TimesheetStatusService {
    List<String> getAvailableStatuses();
}
