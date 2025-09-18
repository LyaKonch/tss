package bravo.enums;

/**
 * Represents the status of a contract.
 */
public enum ContractStatus {
    PREPARED("Prepared", "#FFC107"),
    STARTED("Started", "#4CAF50"),
    TERMINATED("Terminated", "#F44336"),
    ARCHIVED("Archived", "#9E9E9E");

    private final String displayName;

    private final String color;

    ContractStatus(String displayName, String color) {
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
