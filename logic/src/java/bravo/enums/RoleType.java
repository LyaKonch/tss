package bravo.enums;

public enum RoleType {
    Employee("Employee", "#FFC107"),
    Supervisor("Supervisor", "#4CAF50"),
    Secretary("Secretary", "#F45256"),
    Assistant("Assistant", "#9E9E9E");

    private final String displayName;

    private final String color;

    RoleType(String displayName, String color) {
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
