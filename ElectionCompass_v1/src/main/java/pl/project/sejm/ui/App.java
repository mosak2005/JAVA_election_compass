package pl.project.sejm.ui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import pl.project.sejm.MatchService;
import pl.project.sejm.Print;
import pl.project.sejm.SejmApiClient;
import pl.project.sejm.SejmUtils;
import pl.project.sejm.VoteDetail;
import pl.project.sejm.Voting;

import java.util.*;
import java.util.concurrent.*;

public class App extends Application {

    private final ElectionDataService dataService = new ElectionDataService();
    private final SejmApiClient api = new SejmApiClient();

    private HostServices hostServices;

    private static final int TERM = 10;
    private static final String PRINT_WEB_PREFIX = "https://www.sejm.gov.pl/sejm" + TERM + ".nsf/druk.xsp?nr=";

    private List<Voting> quiz = new ArrayList<>();
    private int index = 0;
    private final Map<Integer, String> userVotes = new HashMap<>(); 

    private Label status;
    private ProgressBar progressBar;

    private StackPane screens;
    private VBox startScreen;
    private VBox quizScreen;
    private VBox resultsScreen;
    private VBox disciplineScreen;

    private Button startQuizBtn;
    private Button disciplineBtn;

    private Label counter;
    private Label title;
    private Button backBtn;
    private Button yesBtn, noBtn, abstainBtn;

    private Button billInfoBtn;

    private TableView<ClubRow> clubTable;
    private Label bestMpLabel;
    private Button againBtn;
    private Button goDisciplineFromResultsBtn;

    private TableView<ClubDiscRow> discClubTable;
    private TableView<RebelRow> rebelTable;
    private Button discBackBtn;

    private Task<?> runningTask;

    private static final int DETAILS_THREADS = 8; 

    @Override
    public void start(Stage stage) {
        hostServices = getHostServices();

        Label header = new Label("Election Compass");
        header.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        status = new Label("Status: gotowy");
        status.setStyle("-fx-opacity: 0.85;");

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        VBox top = new VBox(10, header, status, progressBar, new Separator());
        top.setPadding(new Insets(20, 24, 10, 24));

        buildStartScreen();
        buildQuizScreen();
        buildResultsScreen();
        buildDisciplineScreen();

        screens = new StackPane(startScreen, quizScreen, resultsScreen, disciplineScreen);
        screens.setPadding(new Insets(10, 24, 24, 24));

        showStartScreen();

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(screens);

        Scene scene = new Scene(root, 900, 660);
        stage.setTitle("Election Compass");
        stage.setScene(scene);
        scene.setOnKeyPressed(e -> {
            if (!quizScreen.isVisible()) return;

            switch (e.getCode()) {
                case DIGIT1, NUMPAD1 -> { if (!yesBtn.isDisable()) yesBtn.fire(); }
                case DIGIT2, NUMPAD2 -> { if (!noBtn.isDisable()) noBtn.fire(); }
                case DIGIT3, NUMPAD3 -> { if (!abstainBtn.isDisable()) abstainBtn.fire(); }
                case BACK_SPACE -> { if (!backBtn.isDisable()) backBtn.fire(); }
            }
        });
        stage.show();
    }

  
    //screen stuff
    private void buildStartScreen() {
        Label info = new Label(
                "Quiz: wylosuj 5 głosowań i zobacz zgodność z klubami.\n" +
                "Dyscyplina: policz spójność klubów i 'buntowników' (wolniejsze, pobiera dużo danych)."
        );
        info.setWrapText(true);
        info.setMaxWidth(720);

        startQuizBtn = new Button("Start quiz");
        startQuizBtn.setDefaultButton(true);
        startQuizBtn.setOnAction(e -> startQuiz());

        disciplineBtn = new Button("Dyscyplina partyjna (ostatnie 10 posiedzeń)");
        disciplineBtn.setOnAction(e -> runDisciplineScan(10));

        VBox buttons = new VBox(10, startQuizBtn, disciplineBtn);
        buttons.setAlignment(Pos.CENTER);

        startScreen = new VBox(16, info, buttons);
        startScreen.setAlignment(Pos.CENTER);
        startScreen.setPadding(new Insets(20));
    }

    private void buildQuizScreen() {
        counter = new Label("");
        counter.setStyle("-fx-font-size: 14px; -fx-opacity: 0.8;");

        title = new Label("");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");
        title.setMaxWidth(820);

        billInfoBtn = new Button("O czym jest ustawa? (druki)");
        billInfoBtn.setOnAction(e -> showBillInfoForCurrentQuestion());
        billInfoBtn.setDisable(true);

        yesBtn = new Button("TAK");
        noBtn = new Button("NIE");
        abstainBtn = new Button("WSTRZYMUJĘ");

        yesBtn.setOnAction(e -> answer("YES"));
        noBtn.setOnAction(e -> answer("NO"));
        abstainBtn.setOnAction(e -> answer("ABSTAIN"));

        HBox voteButtons = new HBox(10, yesBtn, noBtn, abstainBtn);
        voteButtons.setAlignment(Pos.CENTER);

        backBtn = new Button("← Cofnij");
        backBtn.setOnAction(e -> goBack());
        backBtn.setDisable(true);

        Button quitBtn = new Button("Zakończ");
        quitBtn.setOnAction(e -> showStartScreen());

        HBox nav = new HBox(10, backBtn, quitBtn);
        nav.setAlignment(Pos.CENTER);

        VBox questionBox = new VBox(12, counter, title, billInfoBtn, voteButtons, nav);
        questionBox.setAlignment(Pos.CENTER);
        questionBox.setPadding(new Insets(20));
        questionBox.setMaxWidth(860);

        quizScreen = new VBox(questionBox);
        quizScreen.setAlignment(Pos.CENTER);

        setVotingButtonsEnabled(false);
    }

    private void buildResultsScreen() {
        Label resTitle = new Label("Wyniki");
        resTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700;");

        clubTable = new TableView<>();
        clubTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        clubTable.setPrefHeight(320);

        TableColumn<ClubRow, String> clubCol = new TableColumn<>("Klub");
        clubCol.setCellValueFactory(new PropertyValueFactory<>("club"));

        TableColumn<ClubRow, Double> pctCol = new TableColumn<>("Zgodność");
        pctCol.setCellValueFactory(new PropertyValueFactory<>("pct"));
        pctCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format(Locale.US, "%.2f%%", item));
            }
        });

        clubTable.getColumns().addAll(clubCol, pctCol);

        bestMpLabel = new Label("");
        bestMpLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600;");

        againBtn = new Button("Nowy quiz");
        againBtn.setOnAction(e -> startQuiz());

        goDisciplineFromResultsBtn = new Button("Dyscyplina partyjna");
        goDisciplineFromResultsBtn.setOnAction(e -> runDisciplineScan(10));

        Button backToStartBtn = new Button("Wróć do startu");
        backToStartBtn.setOnAction(e -> showStartScreen());

        HBox bottom = new HBox(10, againBtn, goDisciplineFromResultsBtn, backToStartBtn);
        bottom.setAlignment(Pos.CENTER);

        resultsScreen = new VBox(14, resTitle, clubTable, bestMpLabel, bottom);
        resultsScreen.setAlignment(Pos.CENTER);
        resultsScreen.setPadding(new Insets(20));
        resultsScreen.setMaxWidth(860);
    }

    private void buildDisciplineScreen() {
        Label t = new Label("Dyscyplina partyjna");
        t.setStyle("-fx-font-size: 20px; -fx-font-weight: 700;");

        Label hint = new Label("Spójność = jak często klub głosuje jednym głosem (średnio po głosowaniach).");
        hint.setStyle("-fx-opacity: 0.85;");
        hint.setWrapText(true);
        hint.setMaxWidth(820);

        discClubTable = new TableView<>();
        discClubTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        discClubTable.setPrefHeight(260);

        TableColumn<ClubDiscRow, String> c1 = new TableColumn<>("Klub");
        c1.setCellValueFactory(new PropertyValueFactory<>("club"));

        TableColumn<ClubDiscRow, Double> c2 = new TableColumn<>("Spójność średnio");
        c2.setCellValueFactory(new PropertyValueFactory<>("avg"));
        c2.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format(Locale.US, "%.2f%%", item));
            }
        });

        TableColumn<ClubDiscRow, Integer> c3 = new TableColumn<>("Głosowań");
        c3.setCellValueFactory(new PropertyValueFactory<>("count"));

        discClubTable.getColumns().addAll(c1, c2, c3);

        Label rebelsLabel = new Label("Top buntowników (liczymy tylko głosowania, gdzie klub był ≥75% zgodny):");
        rebelsLabel.setStyle("-fx-opacity: 0.85;");

        rebelTable = new TableView<>();
        rebelTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rebelTable.setPrefHeight(220);

        TableColumn<RebelRow, String> r1 = new TableColumn<>("Poseł");
        r1.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<RebelRow, String> r2 = new TableColumn<>("Klub");
        r2.setCellValueFactory(new PropertyValueFactory<>("club"));

        TableColumn<RebelRow, Integer> r3 = new TableColumn<>("Buntów");
        r3.setCellValueFactory(new PropertyValueFactory<>("rebels"));

        rebelTable.getColumns().addAll(r1, r2, r3);

        discBackBtn = new Button("← Wróć");
        discBackBtn.setOnAction(e -> showStartScreen());

        Button rerunBtn = new Button("Przelicz ponownie");
        rerunBtn.setOnAction(e -> runDisciplineScan(10));

        HBox bottom = new HBox(10, rerunBtn, discBackBtn);
        bottom.setAlignment(Pos.CENTER);

        disciplineScreen = new VBox(12, t, hint, discClubTable, rebelsLabel, rebelTable, bottom);
        disciplineScreen.setAlignment(Pos.CENTER);
        disciplineScreen.setPadding(new Insets(20));
        disciplineScreen.setMaxWidth(900);
    }



    private void showStartScreen() {
        cancelRunningTaskIfAny();

        startScreen.setVisible(true);  startScreen.setManaged(true);
        quizScreen.setVisible(false);  quizScreen.setManaged(false);
        resultsScreen.setVisible(false); resultsScreen.setManaged(false);
        disciplineScreen.setVisible(false); disciplineScreen.setManaged(false);

        setTopIdle("Status: gotowy");
        startQuizBtn.setDisable(false);
        disciplineBtn.setDisable(false);
    }

    private void showQuizScreen() {
        startScreen.setVisible(false); startScreen.setManaged(false);
        quizScreen.setVisible(true);   quizScreen.setManaged(true);
        resultsScreen.setVisible(false); resultsScreen.setManaged(false);
        disciplineScreen.setVisible(false); disciplineScreen.setManaged(false);
    }

    private void showResultsScreen() {
        startScreen.setVisible(false); startScreen.setManaged(false);
        quizScreen.setVisible(false);  quizScreen.setManaged(false);
        resultsScreen.setVisible(true); resultsScreen.setManaged(true);
        disciplineScreen.setVisible(false); disciplineScreen.setManaged(false);
    }

    private void showDisciplineScreen() {
        startScreen.setVisible(false); startScreen.setManaged(false);
        quizScreen.setVisible(false);  quizScreen.setManaged(false);
        resultsScreen.setVisible(false); resultsScreen.setManaged(false);
        disciplineScreen.setVisible(true); disciplineScreen.setManaged(true);
    }



    private void startQuiz() {
        cancelRunningTaskIfAny();

        quiz.clear();
        userVotes.clear();
        index = 0;

        showQuizScreen();
        setVotingButtonsEnabled(false);
        backBtn.setDisable(true);
        billInfoBtn.setDisable(true);

        counter.setText("");
        title.setText("Losuję pytania…");

        Task<List<Voting>> task = new Task<>() {
            @Override
            protected List<Voting> call() throws Exception {
                updateMessage("Status: pobieram i losuję pytania…");
                updateProgress(-1, 1);
                return dataService.pickQuizVotings(10, 5);
            }
        };

        bindAndRun(task,
                () -> {
                    quiz = task.getValue();
                    if (quiz == null || quiz.isEmpty()) {
                        setTopIdle("Błąd: nie udało się wylosować głosowań.");
                        showStartScreen();
                        return;
                    }
                    index = 0;
                    setTopIdle("Status: odpowiadaj na pytania.");
                    setVotingButtonsEnabled(true);
                    billInfoBtn.setDisable(false);
                    showQuestion();
                },
                () -> {
                    setTopIdle("Błąd: nie udało się pobrać/losować pytań.");
                    showError("Nie udało się pobrać/losować pytań.", task.getException());
                    showStartScreen();
                }
        );
    }

    private void showQuestion() {
        Voting v = quiz.get(index);
        counter.setText("Pytanie " + (index + 1) + " / " + quiz.size());
        title.setText(v.title);
        backBtn.setDisable(index <= 0);
        billInfoBtn.setDisable(false);
    }

    private void answer(String voteCode) {
        Voting v = quiz.get(index);
        userVotes.put(v.votingNumber, voteCode);

        index++;
        if (index >= quiz.size()) {
            finishAndComputeResults();
        } else {
            showQuestion();
        }
    }

    private void goBack() {
        if (index <= 0) return;
        index--;
        Voting v = quiz.get(index);
        userVotes.remove(v.votingNumber);
        showQuestion();
    }

    private void finishAndComputeResults() {
        setVotingButtonsEnabled(false);
        billInfoBtn.setDisable(true);
        counter.setText("");
        title.setText("Liczenie dopasowania…");

        Task<MatchService.MatchResult> task = new Task<>() {
            @Override
            protected MatchService.MatchResult call() throws Exception {
                updateMessage("Status: pobieram szczegóły i liczę wynik…");
                updateProgress(-1, 1);
                return dataService.computeMatchResult(quiz, userVotes);
            }
        };

        bindAndRun(task,
                () -> {
                    MatchService.MatchResult r = task.getValue();
                    setTopIdle("Status: gotowe ✅");

                    List<ClubRow> rows = new ArrayList<>();
                    for (var c : r.clubsSorted) rows.add(new ClubRow(c.club, c.pct));
                    clubTable.setItems(FXCollections.observableArrayList(rows));

                    bestMpLabel.setText(String.format(Locale.US,
                            "Twój poseł bliźniak: %s (%.2f%%)", r.bestMp, r.bestMpPct));

                    showResultsScreen();
                },
                () -> {
                    setTopIdle("Błąd: nie udało się policzyć wyniku.");
                    showError("Nie udało się policzyć wyniku.", task.getException());
                    showStartScreen();
                }
        );
    }

    private void setVotingButtonsEnabled(boolean enabled) {
        yesBtn.setDisable(!enabled);
        noBtn.setDisable(!enabled);
        abstainBtn.setDisable(!enabled);
    }


    private void showBillInfoForCurrentQuestion() {
        if (quiz == null || quiz.isEmpty()) return;
        if (index < 0 || index >= quiz.size()) return;

        Voting current = quiz.get(index);

        setVotingButtonsEnabled(false);
        backBtn.setDisable(true);
        billInfoBtn.setDisable(true);

        Task<BillInfo> task = new Task<>() {
            @Override
            protected BillInfo call() throws Exception {
                updateMessage("Status: pobieram opis głosowania…");
                updateProgress(-1, 1);

                Voting details = api.getVotingDetails(current.sitting, current.votingNumber);

                String vTitle = details != null && details.title != null ? details.title : current.title;
                String topic = details != null ? details.topic : null;
                if (topic == null || topic.isBlank()) topic = "(Brak pola topic w API dla tego głosowania)";

                List<String> druki = SejmUtils.extractDruki(vTitle);
                List<Print> prints = new ArrayList<>();

                if (!druki.isEmpty()) {
                    updateMessage("Status: pobieram tytuły druków…");
                    updateProgress(0, druki.size());

                    for (int i = 0; i < druki.size(); i++) {
                        if (isCancelled()) return null;

                        String nr = druki.get(i);
                        try {
                            Print p = api.getPrintDetails(nr);
                            if (p != null) prints.add(p);
                        } catch (Exception ignored) {
                            
                        }
                        updateProgress(i + 1, druki.size());
                    }
                }

                return new BillInfo(vTitle, topic, druki, prints);
            }
        };

        bindAndRun(task,
                () -> {
                    BillInfo info = task.getValue();
                    if (info == null) {
                        setTopIdle("Status: przerwano.");
                        restoreQuizControls();
                        return;
                    }

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Informacje o ustawie");
                    a.setHeaderText(info.title != null ? info.title : "Głosowanie");

                    Label topicLabel = new Label("OPIS / TOPIC:");
                    topicLabel.setStyle("-fx-font-weight: bold;");
                    Label topic = new Label(info.topic == null ? "(brak)" : info.topic);
                    topic.setWrapText(true);

                    Label drukiLabel = new Label("DRUKI:");
                    drukiLabel.setStyle("-fx-font-weight: bold;");

                    VBox drukiBox = new VBox(8);
                    if (info.druki.isEmpty()) {
                        drukiBox.getChildren().add(new Label("(nie znaleziono numerów druków w tytule)"));
                    } else {
                        for (String nr : info.druki) {
                            String webUrl = PRINT_WEB_PREFIX + nr;

                            Hyperlink web = new Hyperlink("Druk " + nr);
                            web.setOnAction(e -> hostServices.showDocument(webUrl));

                            String titleFromApi = info.prints.stream()
                                    .filter(p -> nr.equals(p.number))
                                    .map(p -> p.title)
                                    .findFirst()
                                    .orElse(null);

                            Label t = new Label(titleFromApi != null ? titleFromApi : "");
                            t.setWrapText(true);
                            t.setStyle("-fx-opacity: 0.85;");

                            VBox one = new VBox(3, web, t);
                            one.setPadding(new Insets(4, 0, 4, 0));
                            drukiBox.getChildren().add(one);
                        }
                    }

                    VBox content = new VBox(10, topicLabel, topic, new Separator(), drukiLabel, drukiBox);
                    content.setPrefWidth(760);

                    ScrollPane sp = new ScrollPane(content);
                    sp.setFitToWidth(true);
                    sp.setPrefViewportHeight(440);

                    a.getDialogPane().setContent(sp);
                    a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    a.showAndWait();

                    setTopIdle("Status: odpowiadaj na pytania.");
                    restoreQuizControls();
                },
                () -> {
                    setTopIdle("Błąd: nie udało się pobrać opisu ustawy.");
                    showError("Nie udało się pobrać opisu ustawy.", task.getException());
                    restoreQuizControls();
                }
        );
    }

    private void restoreQuizControls() {
        setVotingButtonsEnabled(true);
        backBtn.setDisable(index <= 0);
        billInfoBtn.setDisable(false);
    }

    private static class BillInfo {
        final String title;
        final String topic;
        final List<String> druki;
        final List<Print> prints;

        BillInfo(String title, String topic, List<String> druki, List<Print> prints) {
            this.title = title;
            this.topic = topic;
            this.druki = druki == null ? List.of() : druki;
            this.prints = prints == null ? List.of() : prints;
        }
    }

    private void runDisciplineScan(int lastSittings) {
        cancelRunningTaskIfAny();

        showDisciplineScreen();
        discClubTable.setItems(FXCollections.observableArrayList());
        rebelTable.setItems(FXCollections.observableArrayList());

        Task<DisciplineReport> task = new Task<>() {
            @Override
            protected DisciplineReport call() throws Exception {
                updateMessage("Status: pobieram listę posiedzeń…");
                updateProgress(-1, 1);

                List<Integer> sittings = api.getSittingNumbers();
                if (sittings.isEmpty()) return new DisciplineReport(List.of(), List.of());

                int from = Math.max(0, sittings.size() - lastSittings);
                List<Integer> recent = sittings.subList(from, sittings.size());

                updateMessage("Status: zbieram listę głosowań…");
                updateProgress(-1, 1);

                List<VotingRef> refs = new ArrayList<>();
                for (int i = 0; i < recent.size(); i++) {
                    if (isCancelled()) return null;

                    int sitting = recent.get(i);
                    updateMessage(String.format(Locale.US,
                            "Status: posiedzenie %d/%d — pobieram listę głosowań…",
                            (i + 1), recent.size()));

                    List<Voting> votings = api.getVotings(sitting);
                    for (Voting v : votings) refs.add(new VotingRef(sitting, v.votingNumber));
                }

                int total = refs.size();
                if (total == 0) return new DisciplineReport(List.of(), List.of());

                updateMessage(String.format(Locale.US,
                        "Status: pobieram detale głosowań równolegle (%d wątków)…", DETAILS_THREADS));
                updateProgress(0, total);

                ExecutorService pool = Executors.newFixedThreadPool(DETAILS_THREADS);
                CompletionService<VotingDetailsResult> cs = new ExecutorCompletionService<>(pool);

                for (VotingRef ref : refs) {
                    cs.submit(() -> {
                        try {
                            Voting details = api.getVotingDetails(ref.sitting, ref.votingNumber);
                            return VotingDetailsResult.ok(details);
                        } catch (Exception ex) {
                            return VotingDetailsResult.fail(ex);
                        }
                    });
                }

                List<Voting> downloaded = new ArrayList<>(total);
                int done = 0, failed = 0;

                try {
                    for (int i = 0; i < total; i++) {
                        if (isCancelled()) return null;

                        VotingDetailsResult r = cs.take().get();
                        if (r.details != null) downloaded.add(r.details);
                        else failed++;

                        done++;
                        updateProgress(done, total);

                        if (done % 25 == 0 || done == total) {
                            updateMessage(String.format(Locale.US,
                                    "Status: pobrano detale %d/%d (błędy: %d)…", done, total, failed));
                        }
                    }
                } finally {
                    pool.shutdownNow();
                }

                updateMessage("Status: liczę spójność klubów i buntowników…");
                updateProgress(-1, 1);

                Map<String, ClubUnityTracker> clubStats = new HashMap<>();
                Map<Integer, MPRebelTracker> mpStats = new HashMap<>();

                for (Voting v : downloaded) {
                    if (isCancelled()) return null;
                    processVotingForDiscipline(v, clubStats, mpStats);
                }

                List<ClubDisc> clubs = clubStats.entrySet().stream()
                        .map(e -> new ClubDisc(e.getKey(), e.getValue().getAvg(), e.getValue().votingCount))
                        .sorted((a, b) -> Double.compare(b.avgUnityPct, a.avgUnityPct))
                        .toList();

                List<Rebel> rebels = mpStats.values().stream()
                        .sorted((a, b) -> Integer.compare(b.rebellionCount, a.rebellionCount))
                        .limit(10)
                        .map(x -> new Rebel(
                                x.name != null && !x.name.isBlank() ? x.name : "Nieznany",
                                x.club != null ? x.club : "Brak klubu",
                                x.rebellionCount
                        ))
                        .toList();

                return new DisciplineReport(clubs, rebels);
            }
        };

        bindAndRun(task,
                () -> {
                    DisciplineReport rep = task.getValue();
                    if (rep == null) {
                        setTopIdle("Status: przerwano.");
                        return;
                    }

                    List<ClubDiscRow> rows = rep.clubsSorted.stream()
                            .map(x -> new ClubDiscRow(x.club, x.avgUnityPct, x.votingCount))
                            .toList();
                    discClubTable.setItems(FXCollections.observableArrayList(rows));

                    List<RebelRow> rebelRows = rep.topRebels.stream()
                            .map(x -> new RebelRow(x.name, x.club, x.rebellionCount))
                            .toList();
                    rebelTable.setItems(FXCollections.observableArrayList(rebelRows));

                    setTopIdle("Status: gotowe ✅");
                },
                () -> {
                    setTopIdle("Błąd: nie udało się policzyć dyscypliny.");
                    showError("Nie udało się policzyć dyscypliny.", task.getException());
                    showStartScreen();
                }
        );
    }
 //que?
    private void processVotingForDiscipline(
            Voting v,
            Map<String, ClubUnityTracker> clubStats,
            Map<Integer, MPRebelTracker> mpStats
    ) {
        if (v == null || v.votes == null) return;

        Map<String, List<VoteDetail>> byClub = new HashMap<>();
        for (VoteDetail vd : v.votes) {
            byClub.computeIfAbsent(vd.club, k -> new ArrayList<>()).add(vd);
        }

        for (Map.Entry<String, List<VoteDetail>> entry : byClub.entrySet()) {
            String club = entry.getKey();
            if (club == null || "niez.".equalsIgnoreCase(club)) continue;

            List<VoteDetail> votes = entry.getValue();
            if (votes.size() < 3) continue;

            Map<String, Integer> counts = new HashMap<>();
            for (VoteDetail vd : votes) {
                counts.put(vd.vote, counts.getOrDefault(vd.vote, 0) + 1);
            }

            String majorityVote = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("ABSENT");

            double unity = (double) counts.getOrDefault(majorityVote, 0) / votes.size() * 100.0;
            clubStats.computeIfAbsent(club, k -> new ClubUnityTracker()).add(unity);

            if (unity >= 75.0) {
                for (VoteDetail vd : votes) {
                    if (!majorityVote.equals(vd.vote) && !"ABSENT".equals(vd.vote)) {
                        MPRebelTracker tr = mpStats.computeIfAbsent(vd.MP, k -> new MPRebelTracker());
                        tr.name = ((vd.firstName != null ? vd.firstName : "") + " " + (vd.lastName != null ? vd.lastName : "")).trim();
                        tr.club = vd.club;
                        tr.rebellionCount++;
                    }
                }
            }
        }
    }



    private void bindAndRun(Task<?> task, Runnable onSuccess, Runnable onFail) {
        runningTask = task;

        if (startQuizBtn != null) startQuizBtn.setDisable(true);
        if (disciplineBtn != null) disciplineBtn.setDisable(true);
        if (goDisciplineFromResultsBtn != null) goDisciplineFromResultsBtn.setDisable(true);

        progressBar.setVisible(true);
        progressBar.setManaged(true);

        status.textProperty().unbind();
        progressBar.progressProperty().unbind();

        status.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            cleanupTaskBinding();
            onSuccess.run();
        });

        task.setOnFailed(e -> {
            cleanupTaskBinding();
            onFail.run();
        });

        task.setOnCancelled(e -> {
            cleanupTaskBinding();
            setTopIdle("Status: przerwano.");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void cleanupTaskBinding() {
        runningTask = null;

        status.textProperty().unbind();
        progressBar.progressProperty().unbind();

        progressBar.setVisible(false);
        progressBar.setManaged(false);

        if (startQuizBtn != null) startQuizBtn.setDisable(false);
        if (disciplineBtn != null) disciplineBtn.setDisable(false);
        if (goDisciplineFromResultsBtn != null) goDisciplineFromResultsBtn.setDisable(false);
    }

    private void cancelRunningTaskIfAny() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }

    private void setTopIdle(String text) {
        status.textProperty().unbind();
        status.setText(text);
        progressBar.progressProperty().unbind();
        progressBar.setVisible(false);
        progressBar.setManaged(false);
    }

    private void showError(String header, Throwable ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Błąd");
        a.setHeaderText(header);
        a.setContentText(ex == null ? "Nieznany błąd." : String.valueOf(ex.getMessage()));
        a.showAndWait();
    }



    public static class ClubRow {
        private final SimpleStringProperty club = new SimpleStringProperty();
        private final SimpleDoubleProperty pct = new SimpleDoubleProperty();

        public ClubRow(String club, double pct) {
            this.club.set(club);
            this.pct.set(pct);
        }
        public String getClub() { return club.get(); }
        public double getPct() { return pct.get(); }
    }

    public static class ClubDiscRow {
        private final SimpleStringProperty club = new SimpleStringProperty();
        private final SimpleDoubleProperty avg = new SimpleDoubleProperty();
        private final SimpleIntegerProperty count = new SimpleIntegerProperty();

        public ClubDiscRow(String club, double avg, int count) {
            this.club.set(club);
            this.avg.set(avg);
            this.count.set(count);
        }
        public String getClub() { return club.get(); }
        public double getAvg() { return avg.get(); }
        public int getCount() { return count.get(); }
    }

    public static class RebelRow {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty club = new SimpleStringProperty();
        private final SimpleIntegerProperty rebels = new SimpleIntegerProperty();

        public RebelRow(String name, String club, int rebels) {
            this.name.set(name);
            this.club.set(club);
            this.rebels.set(rebels);
        }
        public String getName() { return name.get(); }
        public String getClub() { return club.get(); }
        public int getRebels() { return rebels.get(); }
    }


    private static class VotingRef {
        final int sitting;
        final int votingNumber;
        VotingRef(int sitting, int votingNumber) {
            this.sitting = sitting;
            this.votingNumber = votingNumber;
        }
    }

    private static class ClubUnityTracker {
        double sumOfPercentages = 0;
        int votingCount = 0;
        void add(double pct) { sumOfPercentages += pct; votingCount++; }
        double getAvg() { return votingCount == 0 ? 0 : sumOfPercentages / votingCount; }
    }

    private static class MPRebelTracker {
        String name, club;
        int rebellionCount = 0;
    }

    private static class ClubDisc {
        final String club;
        final double avgUnityPct;
        final int votingCount;
        ClubDisc(String club, double avgUnityPct, int votingCount) {
            this.club = club;
            this.avgUnityPct = avgUnityPct;
            this.votingCount = votingCount;
        }
    }

    private static class Rebel {
        final String name;
        final String club;
        final int rebellionCount;
        Rebel(String name, String club, int rebellionCount) {
            this.name = name;
            this.club = club;
            this.rebellionCount = rebellionCount;
        }
    }

    private static class DisciplineReport {
        final List<ClubDisc> clubsSorted;
        final List<Rebel> topRebels;
        DisciplineReport(List<ClubDisc> clubsSorted, List<Rebel> topRebels) {
            this.clubsSorted = clubsSorted;
            this.topRebels = topRebels;
        }
    }

    private static class VotingDetailsResult {
        final Voting details;
        final Exception error;
        private VotingDetailsResult(Voting details, Exception error) {
            this.details = details;
            this.error = error;
        }
        static VotingDetailsResult ok(Voting v) { return new VotingDetailsResult(v, null); }
        static VotingDetailsResult fail(Exception ex) { return new VotingDetailsResult(null, ex); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
