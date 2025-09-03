package bravo.entities;

import bravo.enums.TimesheetStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "timesheet")
@NamedQueries({
    @NamedQuery(name = "Timesheet.findByUuid",
                query = "SELECT e FROM Timesheet e WHERE e.uuid = :uuid"),
    @NamedQuery(name = "Timesheet.findAll",
                query = "SELECT e FROM Timesheet e"),
    @NamedQuery(name = "Timesheet.findByContract",
                query = "SELECT e FROM Timesheet e WHERE e.contract = :contract"),
    @NamedQuery(name = "Timesheet.findByStatus",
                query = "SELECT e FROM Timesheet e WHERE e.status = :status"),
    @NamedQuery(name = "Timesheet.findBetweenDates",
                query = "SELECT e FROM Timesheet e WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
})
public class Timesheet extends AbstractEntity{

    @ManyToOne(optional = false) @JoinColumn(name = "contract_id")
    private Contract contract;

    private LocalDate startDate;     
    private LocalDate endDate;      

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status = TimesheetStatus.IN_PROGRESS;

    private LocalDate signedByEmployee;
    private LocalDate signedBySupervisor;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();

    //Getter&Setter
    public Timesheet(){
        super();
    }
    
    public Timesheet(boolean NewEntity){
        super(NewEntity);
    }
    
    public Contract getContract() { 
        return contract; 
    }
    public void setContract(Contract contract) { 
        this.contract = contract; 
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

    public List<TimesheetEntry> getEntries() { 
        return Collections.unmodifiableList(entries); 
    }
}
