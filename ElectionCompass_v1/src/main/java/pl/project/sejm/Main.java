package pl.project.sejm;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        SejmApiClient api = new SejmApiClient();
        MatchService matchService = new MatchService();
        DisciplineService disciplineService = new DisciplineService();

        System.out.println("Inicjalizacja systemu...");
        Map<Integer, MP> mpMap = api.getMPMap();
        List<Integer> sittings = api.getSittingNumbers();
        List<Integer> last10 = sittings.subList(Math.max(0, sittings.size() - 10), sittings.size());

        System.out.println("ZAKRES: Ostatnie 10 posiedzeń (od nr " + last10.get(0) + ")");

        List<Voting> pool = new ArrayList<>();
        for (Integer sId : last10) {
            for (Voting v : api.getVotings(sId)) {
                if (!SejmUtils.extractDruki(v.title).isEmpty()) pool.add(v);
            }
        }
        Collections.shuffle(pool);

        Map<Integer, String> userVotes = new HashMap<>();
        List<Voting> userVotingDetails = new ArrayList<>();

        System.out.println("\n>>> CZĘŚĆ 1: TWÓJ DOPASOWANIE POLITYCZNE (5 LOSOWYCH USTAW)");
        for (int i = 0; i < Math.min(5, pool.size()); i++) {
            Voting details = api.getVotingDetails(pool.get(i).sitting, pool.get(i).votingNumber);
            System.out.println("\n[" + (i+1) + "/5] " + details.title);
            SejmUtils.extractDruki(details.title).forEach(nr -> System.out.println("DRUK " + nr + ": https://www.sejm.gov.pl/Sejm10.nsf/druk.xsp?nr=" + nr));

            System.out.print("Twój głos [1-TAK, 2-NIE, 3-WSTRZYMAJ]: ");
            int choice = sc.nextInt();
            String vStr = switch(choice) { case 1->"YES"; case 2->"NO"; default->"ABSTAIN"; };
            userVotes.put(details.votingNumber, vStr);
            userVotingDetails.add(details);
        }

        matchService.calculate(userVotes, userVotingDetails, mpMap);

        System.out.println("\n>>> CZĘŚĆ 2: GŁĘBOKA ANALIZA DYSCYPLINY Z OSTATNICH 10 POSIEDZEŃ");
        System.out.println("(To potrwa chwilę, skanuję setki głosowań...)");

        for (Integer sId : last10) {
            System.out.print("Skanuję posiedzenie " + sId + "... ");
            List<Voting> allInSitting = api.getVotings(sId);
            for (Voting v : allInSitting) {
                Voting details = api.getVotingDetails(sId, v.votingNumber);
                disciplineService.processVoting(details);
            }
            System.out.println("OK");
        }

        disciplineService.printReport();
        System.out.println("\nDziękujemy za skorzystanie z systemu!");
    }
}