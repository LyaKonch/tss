package bravo.enums;

public enum TimesheetStatus {
    IN_PROGRESS("In Progress", "#FFC107"),
    SIGNED_BY_EMPLOYEE("Signed by Employee", "#4CAF50"),
    SIGNED_BY_SUPERVISOR("Signed by Supervisor", "#F45256"),
    ARCHIVED("Archived", "#9E9E9E");

    private final String displayName;

    private final String color;

    TimesheetStatus(String displayName, String color) {
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
