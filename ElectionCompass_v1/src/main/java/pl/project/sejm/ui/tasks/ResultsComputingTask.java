package pl.project.sejm.ui.tasks;

import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;

import pl.project.sejm.MatchService;
import pl.project.sejm.Voting;
import pl.project.sejm.ui.ElectionDataService;

public class ResultsComputingTask extends Task<MatchService.MatchResult> {
    private final ElectionDataService dataService;
    private final List<Voting> quizVotings;
    private final Map<Integer, String> userVotes;

    public ResultsComputingTask(ElectionDataService dataService, 
                               List<Voting> quizVotings, 
                               Map<Integer, String> userVotes) {
        this.dataService = dataService;
        this.quizVotings = quizVotings;
        this.userVotes = userVotes;
    }

    @Override
    protected MatchService.MatchResult call() throws Exception {
        updateMessage("Status: pobieram szczegóły i liczę wynik");
        updateProgress(-1, 1);
        return dataService.computeMatchResult(quizVotings, userVotes);
    }
}
