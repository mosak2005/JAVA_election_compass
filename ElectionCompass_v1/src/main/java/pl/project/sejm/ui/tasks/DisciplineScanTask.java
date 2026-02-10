package pl.project.sejm.ui.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;

import pl.project.sejm.SejmApiClient;
import pl.project.sejm.Voting;
import pl.project.sejm.ui.DisciplineAnalyzer;
import pl.project.sejm.ui.model.ClubUnityTracker;
import pl.project.sejm.ui.model.DisciplineReport;
import pl.project.sejm.ui.model.MPRebelTracker;
import pl.project.sejm.ui.model.VotingDetailsResult;
import pl.project.sejm.ui.model.VotingRef;

public class DisciplineScanTask extends Task<DisciplineReport> {

    private static final int DETAILS_THREADS = 8;

    private final SejmApiClient api;
    private final int lastSittings;
    private final DisciplineAnalyzer analyzer;

    public DisciplineScanTask(SejmApiClient api, int lastSittings, DisciplineAnalyzer analyzer) {
        this.api = api;
        this.lastSittings = lastSittings;
        this.analyzer = analyzer;
    }

    @Override
    protected DisciplineReport call() throws Exception {
        updateMessage("Status: pobieram listę posiedzeń");
        updateProgress(-1, 1);

        List<Integer> sittings = api.getSittingNumbers();
        if (sittings.isEmpty()) {
            return new DisciplineReport(List.of(), List.of());
        }
        int from = Math.max(0, sittings.size() - lastSittings);
        List<Integer> recent = sittings.subList(from, sittings.size());
        updateProgress(-1, 1);

        List<VotingRef> refs = new ArrayList<>();
        for (int i = 0; i < recent.size(); i++) {
            if (isCancelled()) {
                return null;
            }

            int sitting = recent.get(i);
            updateMessage(String.format(Locale.US,
                    "Status: posiedzenie %d/%d — pobieram listę głosowań",
                    (i + 1), recent.size()));

            List<Voting> votings = api.getVotings(sitting);
            for (Voting v : votings) {
                refs.add(new VotingRef(sitting, v.votingNumber));
            }
        }

        int total = refs.size();
        if (total == 0) {
            return new DisciplineReport(List.of(), List.of());
        }
        updateProgress(0, total);

        ExecutorService pool = Executors.newFixedThreadPool(DETAILS_THREADS);
        CompletionService<VotingDetailsResult> cs = new ExecutorCompletionService<>(pool);

        for (VotingRef ref : refs) {
            cs.submit(() -> {
                try {
                    Voting details = api.getVotingDetails(ref.sitting(), ref.votingNumber());
                    return VotingDetailsResult.ok(details);
                } catch (Exception ex) {
                    return VotingDetailsResult.fail(ex);
                }
            });
        }

        List<Voting> downloaded = new ArrayList<>(total);
        int done = 0;
        int failed = 0;

        try {
            for (int i = 0; i < total; i++) {
                if (isCancelled()) {
                    return null;
                }

                VotingDetailsResult r = cs.take().get();
                if (r.getDetails() != null) {
                    downloaded.add(r.getDetails());
                } else {
                    failed++;
                }

                done++;
                updateProgress(done, total);

                if (done % 25 == 0 || done == total) {
                    updateMessage(String.format(Locale.US,
                            "Status: pobrano detale %d/%d (błędy: %d)", done, total, failed));
                }
            }
        } finally {
            pool.shutdownNow();
        }

        updateMessage("Status: liczę spójność klubów i buntowników");
        updateProgress(-1, 1);

        Map<String, ClubUnityTracker> clubStats = new HashMap<>();
        Map<Integer, MPRebelTracker> mpStats = new HashMap<>();

        for (Voting v : downloaded) {
            if (isCancelled()) {
                return null;
            }
            analyzer.processVotingForDiscipline(v, clubStats, mpStats);
        }

        return analyzer.createReport(clubStats, mpStats);
    }
}
