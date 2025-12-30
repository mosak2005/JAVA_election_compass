package pl.project.sejm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class SejmApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "https://api.sejm.gov.pl/sejm/term10";

    /**
     * Pobiera numery wszystkich odbytych posiedzeń.
     */
    public List<Integer> getSittingNumbers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/proceedings"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(response.body());

        Set<Integer> sittingSet = new HashSet<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                if (node.has("number")) {
                    sittingSet.add(node.get("number").asInt());
                }
            }
        }
        List<Integer> sortedSittings = new ArrayList<>(sittingSet);
        Collections.sort(sortedSittings);
        return sortedSittings;
    }

    /**
     * Pobiera listę głosowań dla konkretnego posiedzenia.
     */
    public List<Voting> getVotings(int sitting) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/votings/" + sitting))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Voting[] votingsArray = mapper.readValue(response.body(), Voting[].class);
        return Arrays.asList(votingsArray);
    }

    /**
     * Pobiera szczegółowe wyniki głosowania (z listą głosów posłów).
     */
    public Voting getVotingDetails(int sitting, int votingNum) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/votings/" + sitting + "/" + votingNum))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), Voting.class);
    }

    /**
     * Pobiera dane o konkretnym druku (tytuł ustawy).
     */
    public Print getPrintDetails(String drukNr) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prints/" + drukNr))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return null;

        return mapper.readValue(response.body(), Print.class);
    }
    /**
     * Pobiera mapę posłów (ID -> MP).
     */
    public Map<Integer, MP> getMPMap() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/MP"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        MP[] mps = mapper.readValue(response.body(), MP[].class);

        Map<Integer, MP> mpMap = new HashMap<>();
        for (MP mp : mps) {
            mpMap.put(mp.id, mp);
        }
        return mpMap;
    }
}