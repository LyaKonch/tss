
package bravo.dto;

import bravo.enums.TimesheetStatus;
import java.time.LocalDate;
import java.util.List;


public class TimesheetDTO extends AbstractDTO {
    private String contractUuid;
    private LocalDate startDate;
    private LocalDate endDate;
    private TimesheetStatus status = TimesheetStatus.IN_PROGRESS;
    private LocalDate signedByEmployee;
    private LocalDate signedBySupervisor;
    private List<TimesheetEntryDTO> entries;

    public TimesheetDTO() {
        super();
    }
    
    public TimesheetDTO(String uuid, int jpaVersion, String contractUuid, LocalDate startDate, LocalDate endDate, TimesheetStatus status, LocalDate signedByEmployee, LocalDate signedBySupervisor, List<TimesheetEntryDTO> entries) {
        super(uuid, jpaVersion);
        this.contractUuid = contractUuid;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.signedByEmployee = signedByEmployee;
        this.signedBySupervisor = signedBySupervisor;
        this.entries = entries;
    }

    // Getters and Setters
    public String getContractUuid() {
        return contractUuid;
    }

    public void setContractUuid(String contractUuid) {
        this.contractUuid = contractUuid;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public LocalDate getSignedByEmployee() {
        return signedByEmployee;
    }

    public void setSignedByEmployee(LocalDate signedByEmployee) {
        this.signedByEmployee = signedByEmployee;
    }

    public LocalDate getSignedBySupervisor() {
        return signedBySupervisor;
    }

    public void setSignedBySupervisor(LocalDate signedBySupervisor) {
        this.signedBySupervisor = signedBySupervisor;
    }

    public List<TimesheetEntryDTO> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntryDTO> entries) {
        this.entries = entries;
    }
}
