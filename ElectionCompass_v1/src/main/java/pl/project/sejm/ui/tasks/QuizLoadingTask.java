package pl.project.sejm.ui.tasks;

import java.util.List;

import javafx.concurrent.Task;

import pl.project.sejm.Voting;
import pl.project.sejm.ui.ElectionDataService;

public class QuizLoadingTask extends Task<List<Voting>> {
    private final ElectionDataService dataService;
    private final int lastSittings;
    private final int count;

    public QuizLoadingTask(ElectionDataService dataService, int lastSittings, int count) {
        this.dataService = dataService;
        this.lastSittings = lastSittings;
        this.count = count;
    }

    @Override
    protected List<Voting> call() throws Exception {
        updateMessage("Status: pobieram i losuję pytania");
        updateProgress(-1, 1);
        return dataService.pickQuizVotings(lastSittings, count);
    }
}
