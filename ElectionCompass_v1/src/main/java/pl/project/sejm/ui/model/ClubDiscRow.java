package pl.project.sejm.ui.model;

public final class ClubDiscRow {
    private final String club;
    private final double avg;
    private final int count;

    public ClubDiscRow(String club, double avg, int count) {
        this.club = club;
        this.avg = avg;
        this.count = count;
    }

    public String getClub() { return club; }
    public double getAvg() { return avg; }
    public int getCount() { return count; }
}
