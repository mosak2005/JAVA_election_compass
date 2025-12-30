package pl.project.sejm;
import java.util.*;

public class DisciplineService {
    public static class ClubUnityTracker {
        public double sumOfPercentages = 0;
        public int votingCount = 0;
        public void add(double pct) { sumOfPercentages += pct; votingCount++; }
        public double getAvg() { return votingCount == 0 ? 0 : sumOfPercentages / votingCount; }
    }

    public static class MPRebelTracker {
        public String name, club;
        public int rebellionCount = 0;
    }

    private final Map<String, ClubUnityTracker> clubStats = new HashMap<>();
    private final Map<Integer, MPRebelTracker> mpStats = new HashMap<>();

    public void processVoting(Voting v) {
        if (v.votes == null) return;
        Map<String, List<VoteDetail>> byClub = new HashMap<>();
        for (VoteDetail vd : v.votes) byClub.computeIfAbsent(vd.club, k -> new ArrayList<>()).add(vd);

        for (Map.Entry<String, List<VoteDetail>> entry : byClub.entrySet()) {
            String club = entry.getKey();
            if (club == null || "niez.".equalsIgnoreCase(club)) continue;

            List<VoteDetail> votes = entry.getValue();
            if (votes.size() < 3) continue;

            Map<String, Integer> counts = new HashMap<>();
            for (VoteDetail vd : votes) counts.put(vd.vote, counts.getOrDefault(vd.vote, 0) + 1);

            String majorityVote = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("ABSENT");

            double unity = (double) counts.get(majorityVote) / votes.size() * 100;
            clubStats.computeIfAbsent(club, k -> new ClubUnityTracker()).add(unity);

            if (unity >= 75.0) {
                for (VoteDetail vd : votes) {
                    if (!vd.vote.equals(majorityVote) && !vd.vote.equals("ABSENT")) {
                        mpStats.putIfAbsent(vd.MP, new MPRebelTracker());
                        mpStats.get(vd.MP).name = vd.firstName + " " + vd.lastName;
                        mpStats.get(vd.MP).club = vd.club;
                        mpStats.get(vd.MP).rebellionCount++;
                    }
                }
            }
        }
    }

    public void printReport() {
        System.out.println("\n=== ANALIZA DYSCYPLINY PARTYJNEJ ===");
        clubStats.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getAvg(), e1.getValue().getAvg()))
                .forEach(e -> System.out.printf("%-15s : %6.2f%% spójności (%d głosowań)\n", e.getKey(), e.getValue().getAvg(), e.getValue().votingCount));

        System.out.println("\n=== TOP 5 BUNTOWNIKÓW KADENCJI (W TYM OKRESIE) ===");
        mpStats.values().stream().sorted(Comparator.comparingInt(r -> -r.rebellionCount)).limit(5)
                .forEach(r -> System.out.printf("%-25s (%-10s) : %d razy głosował niezgodnie z klubem, do którego przynajeleży\n", r.name, r.club, r.rebellionCount));
    }
}