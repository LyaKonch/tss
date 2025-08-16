/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bravo.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import bravo.enums.ReportType;
import jakarta.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author Женіч
 */
@XmlRootElement
public class TimesheetEntryDTO extends AbstractDTO {
    // I basically copied AbstractDTO from Contacts example
    // but i think it's a good practice to use such template
    // should we add AbstactEntity for all entities like AbstractDTO?
    
    private Long timesheetID; // shouldn't it be a timesheetID instead of ID? perhaps, since i use AbstractDTO where uuid is present already
    
    private LocalDate entryDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ReportType type;
    private String description;
    
    //Constructors
    
    public TimesheetEntryDTO(){
    }
    
    public TimesheetEntryDTO(String uuid, int jpaVersion,Long timesheetID, LocalDate entryDate, LocalTime startTime, LocalTime endTime, ReportType type, String description) {
        super(uuid, jpaVersion);
        this.timesheetID = timesheetID;
        this.entryDate = entryDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.description = description;
    }
    
    //Getter&Setter
    public Long gettimesheetID() { return timesheetID; }

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
