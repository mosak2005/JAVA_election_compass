package pl.project.sejm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class SejmApiClient {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "https://api.sejm.gov.pl/sejm/term10";

    private HttpRequest.Builder reqBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(20));
    }

    /**
     * Pobiera numery wszystkich odbytych posiedzeń.
     */
    public List<Integer> getSittingNumbers() throws SejmApiException {
        HttpRequest request = reqBuilder("/proceedings").build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) throw new SejmApiException("Błąd serwera: " + response.statusCode());

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
                } catch (Exception ex) {
                  throw new SejmApiException("Nie można pobrać listy posiedzeń: " + ex.getMessage(), ex);
                }
              }

    /**
     * Pobiera listę głosowań dla konkretnego posiedzenia.
     */
    public List<Voting> getVotings(int sitting) throws SejmApiException {
        HttpRequest request = reqBuilder("/votings/" + sitting).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) throw new SejmApiException("Błąd serwera: " + response.statusCode());

            Voting[] votingsArray = mapper.readValue(response.body(), Voting[].class);
            return Arrays.asList(votingsArray);
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać listy głosowań dla posiedzenia " + sitting + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Pobiera szczegółowe wyniki głosowania (z listą głosów posłów).
     */
    public Voting getVotingDetails(int sitting, int votingNum) throws SejmApiException {
        HttpRequest request = reqBuilder("/votings/" + sitting + "/" + votingNum).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) throw new SejmApiException("Błąd serwera: " + response.statusCode());

            return mapper.readValue(response.body(), Voting.class);
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać szczegółów głosowania " + votingNum + " z posiedzenia " + sitting + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Pobiera dane o konkretnym druku (tytuł ustawy).
     */
    public Print getPrintDetails(String drukNr) throws SejmApiException {
        HttpRequest request = reqBuilder("/prints/" + drukNr).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            return mapper.readValue(response.body(), Print.class);
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać danych druku " + drukNr + ": " + ex.getMessage(), ex);
        }
    }
    /**
     * Pobiera mapę posłów (ID -> MP).
     */
    public Map<Integer, MP> getMPMap() throws SejmApiException {
        HttpRequest request = reqBuilder("/MP").build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) throw new SejmApiException("Błąd serwera: " + response.statusCode());

            MP[] mps = mapper.readValue(response.body(), MP[].class);

            Map<Integer, MP> mpMap = new HashMap<>();
            for (MP mp : mps) {
                mpMap.put(mp.id, mp);
            }
            return mpMap;
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać listy posłów: " + ex.getMessage(), ex);
        }
    }
}