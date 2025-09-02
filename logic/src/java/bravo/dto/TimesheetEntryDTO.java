package bravo.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import bravo.enums.ReportType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TimesheetEntryDTO extends AbstractDTO {
    
    private String timesheetUUID; 
    
    private LocalDate entryDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ReportType type;
    private String description;
    
    //Constructors
    
    public TimesheetEntryDTO(){
    }
    
    public TimesheetEntryDTO(String uuid, int jpaVersion,String timesheetUUID, LocalDate entryDate, LocalTime startTime, LocalTime endTime, ReportType type, String description) {
        super(uuid, jpaVersion);
        this.timesheetUUID = timesheetUUID;
        this.entryDate = entryDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.description = description;
    }
    
    //Getter&Setter
    public String gettimesheetUUID() { return timesheetUUID; }

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
