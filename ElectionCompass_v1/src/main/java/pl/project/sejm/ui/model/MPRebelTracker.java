package pl.project.sejm.ui.model;

public final class MPRebelTracker {

    private String name = "";
    private String club = "";
    private int rebellionCount = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClub() { return club; }
    public void setClub(String club) { this.club = club; }

    public int getRebellionCount() { return rebellionCount; }
    public void incrementRebellionCount() { rebellionCount++; }
}
