package bravo.utitity;

import jakarta.annotation.security.RolesAllowed;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.function.Predicate;

public class HolidayCalculator {
    private final Predicate<LocalDate> holidayChecker;
    
    //private String land;
    
    public HolidayCalculator(Predicate<LocalDate> holidayChecker) {
        this.holidayChecker = holidayChecker;
    }
    
    @RolesAllowed("Employee")
    public int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

    public int countHolidaysOnWorkingDays(LocalDate start, LocalDate end) {
        if (holidayChecker == null) return 0;
        int count = 0;
        for (LocalDate dat = start; !dat.isAfter(end); dat = dat.plusDays(1)) {
            DayOfWeek w = dat.getDayOfWeek();
            boolean workday = (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY);
            if (workday && holidayChecker.test(dat)) count++;
        }
        return count;
    }
    
    public static boolean isGermanHoliday(LocalDate date) {
        //throw new UnsupportedOperationException("Not supported yet.");
        return false; 
    }
}
