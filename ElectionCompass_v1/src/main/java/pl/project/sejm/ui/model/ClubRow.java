package pl.project.sejm.ui.model;

public final class ClubRow {
    private final String club;
    private final double pct;

    public ClubRow(String club, double pct) {
        this.club = club;
        this.pct = pct;
    }

    public String getClub() { return club; }
    public double getPct() { return pct; }
}
