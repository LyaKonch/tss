package bravo.utitity;

import org.itsallcode.holidays.calculator.logic.variants.Holiday;
import org.itsallcode.holidays.calculator.logic.parser.HolidaysFileParser;

import bravo.enums.FederalState;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PublicHolidayService {

    private static final Logger LOGGER = Logger.getLogger(PublicHolidayService.class.getName());
    private static final String GERMAN_HOLIDAYS_FILE = "holidays-de.cfg";
    private static final String STATE_HOLIDAYS_BASE_PATH = "holidays-";

    private final Map<String, List<Holiday>> cachedHolidays = new HashMap<>();
    HolidaysFileParser parser = new HolidaysFileParser(GERMAN_HOLIDAYS_FILE);

    public PublicHolidayService() {
        List<Holiday> allHolidays = null;
        try (InputStream deStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(GERMAN_HOLIDAYS_FILE)) {
            if (deStream != null) {
                allHolidays.addAll(parser.parse(deStream));
            } else {
                LOGGER.severe("German holiday file not found: " + GERMAN_HOLIDAYS_FILE);
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading German holiday configuration: " + e.getMessage());
        }
        cachedHolidays.put("de", allHolidays);
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
        try (InputStream stateStream = PublicHolidayService.class.getClassLoader().getResourceAsStream(stateHolidaysFile)) {
            if (stateStream != null) {
                cachedHolidays.put(stateKey, parser.parse(stateStream));
                allHolidays.addAll(parser.parse(stateStream));
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
