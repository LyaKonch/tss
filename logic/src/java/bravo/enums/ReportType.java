package bravo.enums;

public enum ReportType {
    WORK("Work", "#FFC107"),
    VACATION("Vacation", "#4CAF50"),
    SICK_LEAVE("Sick Leave", "#F45256");

    private final String displayName;

    private final String color;

    ReportType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

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
