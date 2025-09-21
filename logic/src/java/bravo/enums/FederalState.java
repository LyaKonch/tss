package bravo.enums;

public enum FederalState {
    BADEN_WURTTEMBERG("BW", "Baden-Württemberg"),
    BAVARIA("BY", "Bavaria"),
    BERLIN("BE", "Berlin"),
    BRANDENBURG("BB", "Brandenburg"),
    BREMEN("HB", "Bremen"),
    HAMBURG("HH", "Hamburg"),
    HESSE("HE", "Hesse"),
    MECKLENBURG_VORPOMMERN("MV", "Mecklenburg-Vorpommern"),
    LOWER_SAXONY("NI", "Lower Saxony"),
    NORTH_RHINE_WESTPHALIA("NW", "North Rhine-Westphalia"),
    RHINELAND_PALATINATE("RP", "Rhineland-Palatinate"),
    SAARLAND("SL", "Saarland"),
    SAXONY("SN", "Saxony"),
    SAXONY_ANHALT("ST", "Saxony-Anhalt"),
    SCHLESWIG_HOLSTEIN("SH", "Schleswig-Holstein"),
    THURINGIA("TH", "Thuringia");

    private final String abbreviation;

    private final String name;

    FederalState(String abbreviation, String name) {
        this.abbreviation = abbreviation;
        this.name = name;
    }

    // --- Getters and Setters ---
    public String getAbbreviation() {
        return abbreviation;
    }

    public String getName() {
        return name;
    }
}
