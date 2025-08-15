package bravo.entities;

import bravo.enums.ReportType;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "timesheet_entry")
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "timesheet_id")
    private Timesheet timesheet;

    private LocalDate entryDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private ReportType type = ReportType.WORK;  // WORK, VACATION, SICK_LEAVE

    @Column(length = 500)
    private String description;

    @Transient
    public double getHours() {
        if (startTime == null || endTime == null) return 0.0;
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return Math.max(0, minutes) / 60.0;
    }

    //Getter&Setter
    public Long getId() { return id; }

    public Timesheet getTimesheet() { 
        return timesheet; 
    }
    public void setTimesheet(Timesheet timesheet) { 
        this.timesheet = timesheet; 
    }

    public LocalDate getEntryDate() { 
        return entryDate; 
    }
    public void setEntryDate(LocalDate entryDate) { 
        this.entryDate = entryDate; 
    }

    public LocalTime getStartTime() { 
        return startTime; 
    }
    public void setStartTime(LocalTime startTime) { 
        this.startTime = startTime; 
    }

    public LocalTime getEndTime() { 
        return endTime; 
    }
    public void setEndTime(LocalTime endTime) { 
        this.endTime = endTime; 
    }

    public ReportType getType() { 
        return type; 
    }
    public void setType(ReportType type) { 
        this.type = type; 
    }

    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }
}
