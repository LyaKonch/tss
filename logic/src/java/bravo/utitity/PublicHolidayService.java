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

/**
 * Service for handling public holidays in Germany.
 * <p>
 * Loads holiday definitions from configuration files and determines if a given
 * date is a public holiday for a specific {@link FederalState}.
 * <p>
 * Uses caching to avoid repeated parsing of holiday configuration files.
 */
@Singleton
public class PublicHolidayService {

    private static final Logger LOGGER = Logger.getLogger(PublicHolidayService.class.getName());
    private static final String GERMAN_HOLIDAYS_FILE = "/resources/holiday_cfgs/holidays-de.cfg";
    private static final String STATE_HOLIDAYS_BASE_PATH = "/resources/holiday_cfgs/holidays-";

    private final Map<String, List<Holiday>> cachedHolidays = new HashMap<>();
    private final HolidaysFileParser parser = new HolidaysFileParser(GERMAN_HOLIDAYS_FILE);

    /**
     * Initializes the service by loading German-wide public holidays from the
     * default configuration file.
     */
    public PublicHolidayService() {
        List<Holiday> allHolidays = new ArrayList<>();
        try {
            InputStream deStream = this.getClass().getResourceAsStream(GERMAN_HOLIDAYS_FILE);
            if (deStream != null) {
                allHolidays.addAll(parser.parse(deStream));
                cachedHolidays.put("de", allHolidays);
                LOGGER.info("German holiday file found: " + GERMAN_HOLIDAYS_FILE);
            } else {
                LOGGER.severe("German holiday file not found: " + GERMAN_HOLIDAYS_FILE);
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading German holiday configuration: " + e.getMessage());
        }
    }

    /**
     * Retrieves holidays for the given federal state.
     * <p>
     * German-wide holidays are always included. State-specific holidays are
     * loaded on-demand and cached.
     *
     * @param federalState the federal state to retrieve holidays for
     * @return a list of holidays (German-wide + state-specific)
     */
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
        try {
            InputStream stateStream = this.getClass().getResourceAsStream(stateHolidaysFile);
            if (stateStream != null) {
                List<Holiday> stateHolidays = parser.parse(stateStream);
                cachedHolidays.put(stateKey, stateHolidays);
                allHolidays.addAll(stateHolidays);
                LOGGER.info("Specific holidays file found: " + stateHolidaysFile);
            } else {
                LOGGER.warning("State-specific holiday file not found for " + federalState.getName()
                        + ": " + stateHolidaysFile);
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading holiday configuration for "
                    + federalState.getName() + ": " + e.getMessage());
            return Collections.emptyList();
        }

        return allHolidays;
    }

    /**
     * Checks if a given date is a public holiday in the specified federal state.
     *
     * @param date         the date to check
     * @param federalState the federal state to consider
     * @return {@code true} if the date is a holiday in the given state,
     *         {@code false} otherwise
     */
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
