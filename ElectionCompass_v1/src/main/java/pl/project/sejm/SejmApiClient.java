package pl.project.sejm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class SejmApiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BASE_URL = "https://api.sejm.gov.pl/sejm/term10";

    private final HttpClient client;

    public SejmApiClient() {
        HttpClient httpClient;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        } catch (Exception e) {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
        this.client = httpClient;
    }

    private HttpRequest.Builder reqBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(20));
    }

    // pobiera numery posiedzen
    public List<Integer> getSittingNumbers() throws SejmApiException {
        HttpRequest request = reqBuilder("/proceedings").build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new SejmApiException("Błąd serwera: " + response.statusCode());
            }

            JsonNode root = MAPPER.readTree(response.body());

            Set<Integer> sittingSet = new HashSet<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("number")) {
                        sittingSet.add(node.get("number").asInt());
                    }
                }
            }
            List<Integer> sortedSittings = new ArrayList<>(sittingSet);
            sortedSittings.sort(Integer::compareTo);
            return sortedSittings;
        } catch (SejmApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać listy posiedzeń: " + ex.getMessage(), ex);
        }
    }

    // pobiera listę głosowań dla konkretnego posiedzenia
    public List<Voting> getVotings(int sitting) throws SejmApiException {
        HttpRequest request = reqBuilder("/votings/" + sitting).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new SejmApiException("Błąd serwera: " + response.statusCode());
            }

            Voting[] votingsArray = MAPPER.readValue(response.body(), Voting[].class);
            return Arrays.asList(votingsArray);
        } catch (SejmApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać listy głosowań dla posiedzenia " + sitting + ": " + ex.getMessage(), ex);
        }
    }

    // pobiera dane o głosowaniahc
    public Voting getVotingDetails(int sitting, int votingNum) throws SejmApiException {
        HttpRequest request = reqBuilder("/votings/" + sitting + "/" + votingNum).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new SejmApiException("Błąd serwera: " + response.statusCode());
            }

            return MAPPER.readValue(response.body(), Voting.class);
        } catch (SejmApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać szczegółów głosowania " + votingNum + " z posiedzenia " + sitting + ": " + ex.getMessage(), ex);
        }
    }

    // pobiera dane o druku sejmowym
    public Print getPrintDetails(String drukNr) throws SejmApiException {
        HttpRequest request = reqBuilder("/prints/" + drukNr).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }

            return MAPPER.readValue(response.body(), Print.class);
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać danych druku " + drukNr + ": " + ex.getMessage(), ex);
        }
    }

    // pobiera mapę posłów
    public Map<Integer, MP> getMPMap() throws SejmApiException {
        HttpRequest request = reqBuilder("/MP").build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new SejmApiException("Błąd serwera: " + response.statusCode());
            }

            MP[] mps = MAPPER.readValue(response.body(), MP[].class);

            Map<Integer, MP> mpMap = new HashMap<>();
            for (MP mp : mps) {
                if (mp != null) {
                    mpMap.put(mp.id, mp);
                }
            }
            return Collections.unmodifiableMap(mpMap);
        } catch (SejmApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SejmApiException("Nie można pobrać listy posłów: " + ex.getMessage(), ex);
        }
    }
}
