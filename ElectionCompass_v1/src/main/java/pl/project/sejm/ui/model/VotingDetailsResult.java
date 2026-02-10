package pl.project.sejm.ui.model;

import pl.project.sejm.Voting;

// Wynik pobrania szczegółów głosowania dane/blad
public final class VotingDetailsResult {

    private final Voting details;
    private final Exception error;

    private VotingDetailsResult(Voting details, Exception error) {
        this.details = details;
        this.error = error;
    }

    public static VotingDetailsResult ok(Voting v) {
        return new VotingDetailsResult(v, null);
    }

    public static VotingDetailsResult fail(Exception ex) {
        return new VotingDetailsResult(null, ex);
    }

    public Voting getDetails() { return details; }
    public Exception getError() { return error; }
}
