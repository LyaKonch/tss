package bravo.enums;

/**
 * Represents the frequency at which timesheets are submitted.
 */
public enum TimesheetFrequency {
    WEEKLY("Weekly", "#FFC107"),
    MONTHLY("Monthly", "#FFC107");

    private final String displayName;

    private final String color;

    TimesheetFrequency(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    // --- Getters and Setters ---
    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
