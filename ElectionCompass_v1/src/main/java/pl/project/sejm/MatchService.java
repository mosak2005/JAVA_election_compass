package pl.project.sejm;

import java.util.*;

public class MatchService {

    public static class Stat {
        public int score = 0;
        public int total = 0;
        public double getPct() { return total == 0 ? 0 : (double) score / total * 100; }
    }

    public static class ClubResult {
        public final String club;
        public final double pct;
        public ClubResult(String club, double pct) {
            this.club = club;
            this.pct = pct;
        }
    }

    public static class MatchResult {
        public final List<ClubResult> clubsSorted; 
        public final String bestMp;
        public final double bestMpPct;

        public MatchResult(List<ClubResult> clubsSorted, String bestMp, double bestMpPct) {
            this.clubsSorted = clubsSorted;
            this.bestMp = bestMp;
            this.bestMpPct = bestMpPct;
        }
    }

    public MatchResult calculateResult(Map<Integer, String> userVotes, List<Voting> details, Map<Integer, MP> mpMap) {
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

        List<ClubResult> clubs = clubStats.entrySet().stream()
                .map(e -> new ClubResult(e.getKey(), e.getValue().getPct()))
                .sorted((a, b) -> Double.compare(b.pct, a.pct))
                .toList();

        String bestMp = null;
        double bestPct = -1.0;
        for (var e : mpStats.entrySet()) {
            double pct = e.getValue().getPct();
            if (pct > bestPct) {
                bestPct = pct;
                bestMp = e.getKey();
            }
        }
        if (bestMp == null) {
            bestMp = "Brak danych";
            bestPct = 0.0;
        }

        return new MatchResult(clubs, bestMp, bestPct);
    }
}