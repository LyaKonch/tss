package bravo.utitity;

import org.itsallcode.holidays.calculator.logic.variants.Holiday;
import org.itsallcode.holidays.calculator.logic.parser.HolidaysFileParser;

import bravo.enums.FederalState;
import jakarta.ejb.Singleton;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class PublicHolidayService {
    
    private static final Logger LOGGER = Logger.getLogger(PublicHolidayService.class.getName());
    private static final String GERMAN_HOLIDAYS_FILE = "/resources/holiday_cfgs/holidays-de.cfg";
    private static final String STATE_HOLIDAYS_BASE_PATH = "/resources/holiday_cfgs/holidays-";

    private final Map<String, List<Holiday>> cachedHolidays = new HashMap<>();
    HolidaysFileParser parser = new HolidaysFileParser(GERMAN_HOLIDAYS_FILE);

    public PublicHolidayService() {
        List<Holiday> allHolidays = new ArrayList<Holiday>();
        try {
            InputStream deStream = this.getClass().getResourceAsStream(GERMAN_HOLIDAYS_FILE);
            if (deStream != null) {
                allHolidays.addAll(parser.parse(deStream));
                cachedHolidays.put("de", allHolidays);
                LOGGER.severe("German holiday file found: " + GERMAN_HOLIDAYS_FILE);
            } else {
                LOGGER.severe("German holiday file not found: " + GERMAN_HOLIDAYS_FILE);
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading German holiday configuration: " + e.getMessage());
        }
    }

    private List<Holiday> getHolidaysForState(FederalState federalState) {
        String stateKey = federalState.getAbbreviation().toLowerCase();
        List<Holiday> allHolidays = cachedHolidays.get("de");
        if (cachedHolidays.containsKey(stateKey)) {
            LOGGER.info("Holidays for " + federalState.getName() + " found in cache.");
            allHolidays.addAll(cachedHolidays.get(stateKey));
            return allHolidays;
        }

        LOGGER.info("Loading holidays for " + federalState.getName() + "...");
        String stateHolidaysFile = STATE_HOLIDAYS_BASE_PATH + stateKey + ".cfg";
        try{
            InputStream stateStream = this.getClass().getResourceAsStream(stateHolidaysFile);
            if (stateStream != null) {
                cachedHolidays.put(stateKey, parser.parse(stateStream));
                allHolidays.addAll(parser.parse(stateStream));
                LOGGER.severe("Specififc holidays file found: " + stateHolidaysFile);
            } else {
                LOGGER.warning("State-specific holiday file not found for " + federalState.getName() + ": " + stateHolidaysFile);
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading holiday configuration for " + federalState.getName() + ": " + e.getMessage());
            return Collections.emptyList();
        }

        return allHolidays;
    }

    public boolean isHoliday(LocalDate date, FederalState federalState) {
        List<Holiday> holidays = getHolidaysForState(federalState);
        if (holidays.isEmpty()) {
            return false;
        }
        int year = date.getYear();
        return holidays.stream()
                .anyMatch(h -> {
                    LocalDate holidayDate = h.of(year);
                    return holidayDate != null && holidayDate.isEqual(date);
                });
    }

}
