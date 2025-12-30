package pl.project.sejm;
import java.util.*;

public class MatchService {
    public static class Stat {
        public int score = 0;
        public int total = 0;
        public double getPct() { return total == 0 ? 0 : (double) score / total * 100; }
    }

    public void calculate(Map<Integer, String> userVotes, List<Voting> details, Map<Integer, MP> mpMap) {
        Map<String, Stat> clubStats = new HashMap<>();
        Map<String, Stat> mpStats = new HashMap<>();

        for (Voting v : details) {
            String myVote = userVotes.get(v.votingNumber);
            if (v.votes == null) continue;
            for (VoteDetail vd : v.votes) {
                String mpKey;
                try {
                    MP mpInfo = (vd != null && mpMap != null) ? mpMap.get(vd.MP) : null;
                    if (mpInfo != null) {
                        mpKey = mpInfo.firstName + " " + mpInfo.lastName + " (" + (mpInfo.club != null ? mpInfo.club : "Brak klubu") + ")";
                    } else {
                        mpKey = (vd != null ? (vd.firstName + " " + vd.lastName + " (" + (vd.club != null ? vd.club : "Brak klubu") + ")") : "Nieznany");
                    }
                } catch (Exception ex) {
                    mpKey = "Nieznany";
                }

                String clubKey = (vd != null && vd.club != null) ? vd.club : "Brak klubu";

                clubStats.putIfAbsent(clubKey, new Stat());
                mpStats.putIfAbsent(mpKey, new Stat());

                boolean match = myVote != null && vd != null && vd.vote != null && vd.vote.equalsIgnoreCase(myVote);

                clubStats.get(clubKey).total++;
                mpStats.get(mpKey).total++;
                if (match) {
                    clubStats.get(clubKey).score++;
                    mpStats.get(mpKey).score++;
                }
            }
        }

        System.out.println("\n=== TWOJA ZGODNOŚĆ Z KLUBAMI ===");
        clubStats.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getPct(), e1.getValue().getPct()))
                .forEach(e -> System.out.printf("%-20s : %.2f%%\n", e.getKey(), e.getValue().getPct()));

        System.out.println("\n=== TWÓJ POSEŁ BLIŹNIAK ===");
        mpStats.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue().getPct()))
                .ifPresent(e -> System.out.printf("%s : %.2f%%\n", e.getKey(), e.getValue().getPct()));
    }
}