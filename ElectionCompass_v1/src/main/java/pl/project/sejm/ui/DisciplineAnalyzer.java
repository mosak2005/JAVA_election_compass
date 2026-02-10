package pl.project.sejm.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.project.sejm.VoteDetail;
import pl.project.sejm.Voting;
import pl.project.sejm.ui.model.ClubUnityTracker;
import pl.project.sejm.ui.model.DisciplineReport;
import pl.project.sejm.ui.model.MPRebelTracker;

public final class DisciplineAnalyzer {

    private static final double MIN_UNITY_THRESHOLD = 75.0;
    private static final int TOP_REBELS_LIMIT = 10;

    public void processVotingForDiscipline(Voting v, Map<String, ClubUnityTracker> clubStats,
                                           Map<Integer, MPRebelTracker> mpStats) {
        if (v == null || v.votes == null) {
            return;
        }

        Map<String, List<VoteDetail>> byClub = new HashMap<>();
        for (VoteDetail vd : v.votes) {
            if (vd != null && vd.club != null) {
                byClub.computeIfAbsent(vd.club, k -> new ArrayList<>()).add(vd);
            }
        }

        for (Map.Entry<String, List<VoteDetail>> entry : byClub.entrySet()) {
            String club = entry.getKey();
            if (club == null || "niez.".equalsIgnoreCase(club)) {
                continue;
            }

            List<VoteDetail> votes = entry.getValue();

            Map<String, Integer> counts = new HashMap<>();
            for (VoteDetail vd : votes) {
                if (vd != null && vd.vote != null) {
                    counts.put(vd.vote, counts.getOrDefault(vd.vote, 0) + 1);
                }
            }

            String majorityVote = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("ABSENT");

            double unity = (double) counts.getOrDefault(majorityVote, 0) / votes.size() * 100.0;
            clubStats.computeIfAbsent(club, k -> new ClubUnityTracker()).add(unity);

            if (unity >= MIN_UNITY_THRESHOLD) {
                for (VoteDetail vd : votes) {
                    if (!majorityVote.equals(vd.vote) && !"ABSENT".equals(vd.vote)) {
                        MPRebelTracker tr = mpStats.computeIfAbsent(vd.mpId, k -> new MPRebelTracker());
                        String firstName = vd.firstName != null ? vd.firstName : "";
                        String lastName = vd.lastName != null ? vd.lastName : "";
                        tr.setName((firstName + " " + lastName).trim());
                        tr.setClub(vd.club);
                        tr.incrementRebellionCount();
                    }
                }
            }
        }
    }

    public DisciplineReport createReport(Map<String, ClubUnityTracker> clubStats,
                                         Map<Integer, MPRebelTracker> mpStats) {
        List<DisciplineReport.ClubDisc> clubs = clubStats.entrySet().stream()
                .map(e -> new DisciplineReport.ClubDisc(
                        e.getKey(),
                        e.getValue().getAvg(),
                        e.getValue().getVotingCount()))
                .sorted((a, b) -> Double.compare(b.avgUnityPct(), a.avgUnityPct()))
                .toList();

        List<DisciplineReport.Rebel> rebels = mpStats.values().stream()
                .sorted((a, b) -> Integer.compare(b.getRebellionCount(), a.getRebellionCount()))
                .limit(TOP_REBELS_LIMIT)
                .map(x -> new DisciplineReport.Rebel(
                        x.getName() != null && !x.getName().isBlank() ? x.getName() : "Nieznany",
                        x.getClub() != null ? x.getClub() : "Brak klubu",
                        x.getRebellionCount()))
                .toList();

        return new DisciplineReport(clubs, rebels);
    }
}
