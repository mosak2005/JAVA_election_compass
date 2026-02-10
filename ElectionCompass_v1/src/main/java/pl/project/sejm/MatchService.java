package pl.project.sejm;

import java.util.*;

public final class MatchService {

    private static final class Stat {
        private double score = 0;
        private int total = 0;

        private double getPct() {
            return total == 0 ? 0 : score / total * 100;
        }
    }

    public static final class ClubResult {
        private final String club;
        private final double pct;

        public ClubResult(String club, double pct) {
            this.club = club;
            this.pct = pct;
        }

        public String getClub() { return club; }
        public double getPct() { return pct; }
    }

    public static final class MatchResult {
        private final List<ClubResult> clubsSorted;
        private final String bestMp;
        private final double bestMpPct;

        public MatchResult(List<ClubResult> clubsSorted, String bestMp, double bestMpPct) {
            this.clubsSorted = List.copyOf(clubsSorted);
            this.bestMp = bestMp;
            this.bestMpPct = bestMpPct;
        }

        public List<ClubResult> getClubsSorted() { return clubsSorted; }
        public String getBestMp() { return bestMp; }
        public double getBestMpPct() { return bestMpPct; }
    }

    public MatchResult calculateResult(Map<Integer, String> userVotes, List<Voting> details, Map<Integer, MP> mpMap) {
        
        Map<String, Stat> clubStats = new HashMap<>();
        Map<String, Stat> mpStats = new HashMap<>();

        for (Voting v : details) {
            if (v == null || v.votes == null) {
                continue;
            }
            String myVote = userVotes.get(v.votingNumber);

            for (VoteDetail vd : v.votes) {
                if (vd == null) {
                    continue;
                }

                String mpKey = buildMpKey(vd, mpMap);
                String clubKey = vd.club != null ? vd.club : "Brak klubu";

                clubStats.computeIfAbsent(clubKey, k -> new Stat());
                mpStats.computeIfAbsent(mpKey, k -> new Stat());

                double points = computePoints(myVote, vd.vote);

                clubStats.get(clubKey).total++;
                mpStats.get(mpKey).total++;
                clubStats.get(clubKey).score += points;
                mpStats.get(mpKey).score += points;
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

    // zgodnosc 1pkt, sprzecznosc 0pkt, wstrzymanie się 0.5pkt
    private static double computePoints(String myVote, String mpVote) {
        if (myVote == null || mpVote == null) {
            return 0;
        }
        if (mpVote.equalsIgnoreCase(myVote)) {
            return 1.0;
        }
        if ("ABSTAIN".equalsIgnoreCase(myVote) || "ABSTAIN".equalsIgnoreCase(mpVote)) {
            return 0.5;
        }
        return 0;
    }

    private String buildMpKey(VoteDetail vd, Map<Integer, MP> mpMap) {
        MP mpInfo = (mpMap != null) ? mpMap.get(vd.mpId) : null;

        String firstName;
        String lastName;
        String club;

        if (mpInfo != null) {
            firstName = mpInfo.firstName != null ? mpInfo.firstName : "";
            lastName = mpInfo.lastName != null ? mpInfo.lastName : "";
            club = mpInfo.club != null ? mpInfo.club : "Brak klubu";
        } else {
            firstName = vd.firstName != null ? vd.firstName : "";
            lastName = vd.lastName != null ? vd.lastName : "";
            club = vd.club != null ? vd.club : "Brak klubu";
        }

        return firstName + " " + lastName + " (" + club + ")";
    }
}
