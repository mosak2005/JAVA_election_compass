package pl.project.sejm.ui;

import pl.project.sejm.SejmApiClient;
import pl.project.sejm.Voting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pl.project.sejm.SejmUtils;
import pl.project.sejm.MatchService;
import pl.project.sejm.MP;
import pl.project.sejm.SejmApiException;



public class ElectionDataService {

    private final SejmApiClient api = new SejmApiClient();

    public List<Voting> loadVotingPool(int lastSittings) throws SejmApiException {
        List<Integer> sittings = api.getSittingNumbers();
        if (sittings.isEmpty()) return Collections.emptyList();

        int from = Math.max(0, sittings.size() - lastSittings);
        List<Integer> recent = sittings.subList(from, sittings.size());

        List<Voting> pool = new ArrayList<>();
        for (int sitting : recent) {
            pool.addAll(api.getVotings(sitting));
        }
        return pool;
    }
    public List<Voting> pickQuizVotings(int lastSittings, int count) throws SejmApiException {
    List<Voting> pool = loadVotingPool(lastSittings);

    List<Voting> withPrints = new ArrayList<>();
    for (Voting v : pool) {
        if (!SejmUtils.extractDruki(v.title).isEmpty()) {
            withPrints.add(v);
        }
    }

    Collections.shuffle(withPrints);
    if (withPrints.size() > count) {
        return new ArrayList<>(withPrints.subList(0, count));
    }
    return withPrints;
    }
    public MatchService.MatchResult computeMatchResult(List<Voting> quizVotings, java.util.Map<Integer, String> userVotes) throws SejmApiException {
    java.util.Map<Integer, MP> mpMap = api.getMPMap();

    java.util.List<Voting> details = new java.util.ArrayList<>();
    for (Voting v : quizVotings) {
        details.add(api.getVotingDetails(v.sitting, v.votingNumber));
    }

    MatchService ms = new MatchService();
    return ms.calculateResult(userVotes, details, mpMap);
}
}
