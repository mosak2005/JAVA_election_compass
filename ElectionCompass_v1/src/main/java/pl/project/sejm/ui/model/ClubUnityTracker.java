package pl.project.sejm.ui.model;

public final class ClubUnityTracker {

    private double sumOfPercentages = 0;
    private int votingCount = 0;

    public void add(double pct) {
        sumOfPercentages += pct;
        votingCount++;
    }

    public int getVotingCount() { return votingCount; }

    public double getAvg() {
        return votingCount == 0 ? 0 : sumOfPercentages / votingCount;
    }
}
