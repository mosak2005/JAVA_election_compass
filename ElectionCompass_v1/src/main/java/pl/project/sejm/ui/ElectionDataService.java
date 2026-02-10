package pl.project.sejm.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pl.project.sejm.MP;
import pl.project.sejm.MatchService;
import pl.project.sejm.SejmApiClient;
import pl.project.sejm.SejmApiException;
import pl.project.sejm.SejmUtils;
import pl.project.sejm.Voting;

// pobiera dane wyborcze
public final class ElectionDataService {

    private final SejmApiClient api = new SejmApiClient();

    // pobiera glosowania z ostatnich posiedzen
    public List<Voting> loadVotingPool(int lastSittings) throws SejmApiException {
        if (lastSittings <= 0) {
            throw new IllegalArgumentException("Liczba posiedzeń musi być większa od 0");
        }

        List<Integer> sittings = api.getSittingNumbers();
        if (sittings.isEmpty()) {
            return Collections.emptyList();
        }

        int from = Math.max(0, sittings.size() - lastSittings);
        List<Integer> recent = sittings.subList(from, sittings.size());

        List<Voting> pool = new ArrayList<>();
        for (int sitting : recent) {
            pool.addAll(api.getVotings(sitting));
        }
        return pool;
    }

    // losuje glosowania do quizu, wybiera tylko te z drukami aby uzytkownik mogl sprawdzic tresci ustaw
    public List<Voting> pickQuizVotings(int lastSittings, int count) throws SejmApiException {
        if (lastSittings <= 0) {
            throw new IllegalArgumentException("Liczba posiedzeń musi być większa od 0");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Liczba głosowań do wylosowania musi być większa od 0");
        }

        List<Voting> pool = loadVotingPool(lastSittings);

        List<Voting> withPrints = new ArrayList<>();
        for (Voting v : pool) {
            if (v != null && v.title != null && !SejmUtils.extractDruki(v.title).isEmpty()) {
                withPrints.add(v);
            }
        }

        Collections.shuffle(withPrints);
        if (withPrints.size() > count) {
            return new ArrayList<>(withPrints.subList(0, count));
        }
        return withPrints;
    }

    // oblicza wyniki dopasowan klubow parlamentarych i poslow do uzytkowniak
    public MatchService.MatchResult computeMatchResult(List<Voting> quizVotings, Map<Integer, String> userVotes) throws SejmApiException {
        if (quizVotings == null || quizVotings.isEmpty()) {
            throw new IllegalArgumentException("Lista głosowań nie może być null lub pusta");
        }
        if (userVotes == null) {
            throw new IllegalArgumentException("Mapa odpowiedzi użytkownika nie może być null");
        }

        Map<Integer, MP> mpMap = api.getMPMap();

        List<Voting> details = new ArrayList<>();
        for (Voting v : quizVotings) {
            if (v != null) {
                details.add(api.getVotingDetails(v.sitting, v.votingNumber));
            }
        }

        MatchService ms = new MatchService();
        return ms.calculateResult(userVotes, details, mpMap);
    }
}
