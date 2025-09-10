
package bravo.utitity;

import bravo.enums.FederalState;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.time.LocalDate;
import java.time.Month;

@Singleton
@Startup
public class TestPublicHolidayService {

    @EJB
    private PublicHolidayService publicHolidayService;
    
    private static final FederalState TEST_STATE = FederalState.RHINELAND_PALATINATE;

    @PostConstruct
    public void testHolidayService() {
        System.out.println("--- Starting PublicHolidayService Test ---");

        // Test Case 1: A known holiday (New Year's Day)
        LocalDate newYearsDay = LocalDate.of(2025, Month.JANUARY, 1);
        boolean isHoliday = publicHolidayService.isHoliday(newYearsDay, TEST_STATE);
        System.out.println("Is " + newYearsDay + " a holiday in " + TEST_STATE.getName() + "? " + isHoliday);
        // must be true

        // Test Case 2: A normal working day
        LocalDate normalWorkDay = LocalDate.of(2025, Month.SEPTEMBER, 10);
        isHoliday = publicHolidayService.isHoliday(normalWorkDay, TEST_STATE);
        System.out.println("Is " + normalWorkDay + " a holiday in " + TEST_STATE.getName() + "? " + isHoliday);
        // must be false

        // Test Case 3: A weekend day (Saturday)
        LocalDate saturday = LocalDate.of(2025, Month.SEPTEMBER, 13);
        isHoliday = publicHolidayService.isHoliday(saturday, TEST_STATE);
        System.out.println("Is " + saturday + " a holiday in " + TEST_STATE.getName() + "? " + isHoliday);
        // must be false
        
        // Test Case 4: A holiday that falls on a weekend (Christmas Day 2027).
        LocalDate christmasDay2027 = LocalDate.of(2027, Month.DECEMBER, 25);
        isHoliday = publicHolidayService.isHoliday(christmasDay2027, TEST_STATE);
        System.out.println("Is " + christmasDay2027 + " a holiday in " + TEST_STATE.getName() + "? " + isHoliday);
        // must be true

        System.out.println("--- PublicHolidayService Test Finished ---");
    }
}
