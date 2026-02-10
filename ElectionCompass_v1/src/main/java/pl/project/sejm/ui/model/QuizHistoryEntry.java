package pl.project.sejm.ui.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Wpis w historii quizów
public final class QuizHistoryEntry {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String time;
    private final int sittings;
    private final int questions;
    private final String bestClub;
    private final double bestClubPct;
    private final String bestMp;
    private final double bestMpPct;

    public QuizHistoryEntry(LocalDateTime timestamp, int sittings, int questions,
                            String bestClub, double bestClubPct,
                            String bestMp, double bestMpPct) {
        this.time = timestamp.format(FORMATTER);
        this.sittings = sittings;
        this.questions = questions;
        this.bestClub = bestClub;
        this.bestClubPct = bestClubPct;
        this.bestMp = bestMp;
        this.bestMpPct = bestMpPct;
    }

    public String getTime() { return time; }
    public int getSittings() { return sittings; }
    public int getQuestions() { return questions; }
    public String getBestClub() { return bestClub; }
    public double getBestClubPct() { return bestClubPct; }
    public String getBestMp() { return bestMp; }
    public double getBestMpPct() { return bestMpPct; }
}
