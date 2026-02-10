package pl.project.sejm.ui.model;

import java.util.List;

public final class DisciplineReport {

    private final List<ClubDisc> clubsSorted;
    private final List<Rebel> topRebels;

    public DisciplineReport(List<ClubDisc> clubsSorted, List<Rebel> topRebels) {
        this.clubsSorted = List.copyOf(clubsSorted);
        this.topRebels = List.copyOf(topRebels);
    }

    public List<ClubDisc> getClubsSorted() { return clubsSorted; }
    public List<Rebel> getTopRebels() { return topRebels; }

    public record ClubDisc(String club, double avgUnityPct, int votingCount) {
    }

    public record Rebel(String name, String club, int rebellionCount) {
    }
}
