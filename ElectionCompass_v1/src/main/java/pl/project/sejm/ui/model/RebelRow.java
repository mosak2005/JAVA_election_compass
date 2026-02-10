package pl.project.sejm.ui.model;

public final class RebelRow {
    private final String name;
    private final String club;
    private final int rebels;

    public RebelRow(String name, String club, int rebels) {
        this.name = name;
        this.club = club;
        this.rebels = rebels;
    }

    public String getName() { return name; }
    public String getClub() { return club; }
    public int getRebels() { return rebels; }
}
