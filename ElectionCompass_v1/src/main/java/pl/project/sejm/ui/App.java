package pl.project.sejm.ui;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import pl.project.sejm.MatchService;
import pl.project.sejm.SejmApiClient;
import pl.project.sejm.Voting;
import pl.project.sejm.ui.components.BackgroundManager;
import pl.project.sejm.ui.components.BillInfoDialog;
import pl.project.sejm.ui.components.Buttons;
import pl.project.sejm.ui.components.ErrorHandler;
import pl.project.sejm.ui.components.ScreenManager;
import pl.project.sejm.ui.components.Tables;
import pl.project.sejm.ui.components.TaskManager;
import pl.project.sejm.ui.model.BillInfo;
import pl.project.sejm.ui.model.ClubDiscRow;
import pl.project.sejm.ui.model.ClubRow;
import pl.project.sejm.ui.model.DisciplineReport;
import pl.project.sejm.ui.model.QuizHistoryEntry;
import pl.project.sejm.ui.model.RebelRow;
import pl.project.sejm.ui.styles.UiConstants;
import pl.project.sejm.ui.styles.UiStyles;
import pl.project.sejm.ui.tasks.BillInfoLoadingTask;
import pl.project.sejm.ui.tasks.DisciplineScanTask;
import pl.project.sejm.ui.tasks.QuizLoadingTask;
import pl.project.sejm.ui.tasks.ResultsComputingTask;


public class App extends Application {

    private final ElectionDataService dataService = new ElectionDataService();
    private final SejmApiClient api = new SejmApiClient();
    private final DisciplineAnalyzer disciplineAnalyzer = new DisciplineAnalyzer();

    private HostServices hostServices;
    private TaskManager taskManager;
    private BackgroundManager backgroundManager;
    private ScreenManager screenManager;

    private List<Voting> quiz = new ArrayList<>();
    private int index = 0;
    private final Map<Integer, String> userVotes = new HashMap<>();

    private Label status;
    private ProgressBar progressBar;

    private BorderPane root;
    private ImageView backgroundImageView;

    private StackPane screens;
    private VBox startScreen;
    private VBox quizScreen;
    private VBox resultsScreen;
    private VBox disciplineScreen;

    private Button startQuizBtn;
    private Button disciplineBtn;
    private Spinner<Integer> sittingsSpinner;

    private Label counter;
    private Label title;
    private Button backBtn;
    private Button yesBtn;
    private Button noBtn;
    private Button abstainBtn;
    private Button billInfoBtn;

    private TableView<ClubRow> clubTable;
    private Label bestMpLabel;
    private Button againBtn;
    private Button goDisciplineFromResultsBtn;

    private TableView<ClubDiscRow> discClubTable;
    private TableView<RebelRow> rebelTable;
    private Button discBackBtn;

    private final List<QuizHistoryEntry> quizHistory = new ArrayList<>();
    private VBox historyScreen;
    private TableView<QuizHistoryEntry> historyTable;

    @Override
    public void start(Stage stage) {
        hostServices = getHostServices();
        initializeUI();
        setupStage(stage);
    }


    private void initializeUI() {
        Label header = new Label(UiConstants.APP_TITLE);
        header.setStyle(UiStyles.WHITE_TEXT_BOLD);

        status = new Label(UiConstants.STATUS_READY);
        status.setStyle(UiStyles.WHITE_TEXT_BOLD);

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);
        progressBar.setMinHeight(6);
        progressBar.setStyle(UiStyles.PROGRESS_BAR);
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        Separator separator = new Separator();
        separator.setStyle(UiStyles.SEPARATOR);
        
        VBox top = new VBox(UiConstants.SPACING_MEDIUM, header, status, progressBar, separator);
        top.setPadding(new Insets(UiConstants.PADDING_TOP, UiConstants.PADDING_SIDES, 
                UiConstants.PADDING_BOTTOM, UiConstants.PADDING_SIDES));

        List<Button> buttonsToDisable = new ArrayList<>();
        taskManager = new TaskManager(status, progressBar, buttonsToDisable);

        buildStartScreen();
        buildQuizScreen();
        buildResultsScreen();
        buildDisciplineScreen();
        buildHistoryScreen();

        buttonsToDisable.add(startQuizBtn);
        buttonsToDisable.add(disciplineBtn);
        buttonsToDisable.add(goDisciplineFromResultsBtn);

        screens = new StackPane(startScreen, quizScreen, resultsScreen, disciplineScreen, historyScreen);
        screens.setPadding(new Insets(UiConstants.SPACING_MEDIUM, UiConstants.PADDING_SIDES, 
                UiConstants.PADDING_SIDES, UiConstants.PADDING_SIDES));

        screenManager = new ScreenManager(List.of(startScreen, quizScreen, resultsScreen, disciplineScreen, historyScreen));
        showStartScreen();

        root = new BorderPane();
        root.setTop(top);
        root.setCenter(screens);

        setupBackground();
    }

    // tlo
    private void setupBackground() {
        backgroundImageView = new ImageView();
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setSmooth(true);
        backgroundImageView.setMouseTransparent(true);

        backgroundManager = new BackgroundManager(backgroundImageView, root, getClass());

        try {
            URL defaultBg = getClass().getResource("/background.jpg");
            if (defaultBg != null) {
                backgroundManager.setBackgroundImage("classpath:/background.jpg");
            } else {
                URL defaultPng = getClass().getResource("/background.png");
                if (defaultPng != null) {
                    backgroundManager.setBackgroundImage("classpath:/background.png");
                }
            }
        } catch (Exception ignored) {

        }
    }


    private void setupStage(Stage stage) {
        StackPane container = new StackPane(backgroundImageView, root);
        Scene scene = new Scene(container, UiConstants.WINDOW_WIDTH, UiConstants.WINDOW_HEIGHT);

        backgroundImageView.fitWidthProperty().bind(scene.widthProperty());
        backgroundImageView.fitHeightProperty().bind(scene.heightProperty());

        stage.setTitle(UiConstants.APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    // Buduje ekran startowy
    private void buildStartScreen() {
        Label info = new Label(
                "Quiz: wylosuj 10 głosowań i zobacz zgodność z klubami.\n" +
                        "Dyscyplina: policz spójność klubów i 'buntowników' (wolniejsze, pobiera dużo danych)."
        );
        info.setWrapText(true);
        info.setMaxWidth(UiConstants.MAX_WIDTH_INFO);
        info.setStyle(UiStyles.WHITE_TEXT_BOLD);

        Label sittingsLabel = new Label("Ostatnie posiedzeń:");
        sittingsLabel.setStyle(UiStyles.WHITE_TEXT_BOLD);

        sittingsSpinner = new Spinner<>(1, 50, UiConstants.QUIZ_LAST_SITTINGS);
        sittingsSpinner.setEditable(true);
        sittingsSpinner.setPrefWidth(80);
        sittingsSpinner.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-background-radius: 6px;"
        );
        sittingsSpinner.getEditor().setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: transparent;"
        );

        sittingsSpinner.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            if (!newText.matches("\\d{1,2}")) {
                return null;
            }
            int val = Integer.parseInt(newText);
            if (val < 1 || val > 50) {
                return null;
            }
            return change;
        }));

        sittingsSpinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && sittingsSpinner.getEditor().getText().isEmpty()) {
                sittingsSpinner.getValueFactory().setValue(UiConstants.QUIZ_LAST_SITTINGS);
            }
        });

        HBox sittingsRow = new HBox(UiConstants.SPACING_MEDIUM, sittingsLabel, sittingsSpinner);
        sittingsRow.setAlignment(Pos.CENTER);

        startQuizBtn = Buttons.createPrimaryButton("🚀 Start quiz", "Rozpocznij quiz z losowymi głosowaniami");
        startQuizBtn.setDefaultButton(true);
        startQuizBtn.setOnAction(e -> startQuiz());

        disciplineBtn = Buttons.createPrimaryButton("📊 Dyscyplina partyjna",
                "Analizuje spójność głosowań klubów w wybranej liczbie posiedzeń");
        disciplineBtn.setOnAction(e -> runDisciplineScan(getSelectedSittings()));

        Button historyBtn = Buttons.createSecondaryButton("📋 Historia wyników");
        historyBtn.setOnAction(e -> showHistoryScreen());

        VBox buttons = new VBox(UiConstants.SPACING_BUTTONS, startQuizBtn, disciplineBtn, historyBtn);
        buttons.setAlignment(Pos.CENTER);

        startScreen = new VBox(UiConstants.SPACING_XXLARGE, info, sittingsRow, buttons);
        startScreen.setAlignment(Pos.CENTER);
        startScreen.setPadding(new Insets(30));
    }

    // Zwraca liczbę posiedzeń wybraną przez uzytkownika
    private int getSelectedSittings() {
        try {
            sittingsSpinner.commitValue();
            return sittingsSpinner.getValue();
        } catch (Exception e) {
            return UiConstants.QUIZ_LAST_SITTINGS;
        }
    }

    // Buduje ekran quizu
    private void buildQuizScreen() {
        counter = new Label("");
        counter.setStyle(UiStyles.WHITE_TEXT_BOLD);

        title = new Label("");
        title.setWrapText(true);
        title.setMaxWidth(UiConstants.MAX_WIDTH_TITLE);
        title.setStyle(UiStyles.WHITE_TEXT_BOLD);

        billInfoBtn = Buttons.createSecondaryButton("📄 O czym jest ustawa? (druki)");
        billInfoBtn.setOnAction(e -> showBillInfoForCurrentQuestion());
        billInfoBtn.setDisable(true);

        yesBtn = Buttons.createYesButton("✓ TAK");
        noBtn = Buttons.createNoButton("✗ NIE");
        abstainBtn = Buttons.createAbstainButton("⊘ WSTRZYMUJĘ");

        yesBtn.setOnAction(e -> answer("YES"));
        noBtn.setOnAction(e -> answer("NO"));
        abstainBtn.setOnAction(e -> answer("ABSTAIN"));

        HBox voteButtons = new HBox(UiConstants.SPACING_VOTE_BUTTONS, yesBtn, noBtn, abstainBtn);
        voteButtons.setAlignment(Pos.CENTER);

        backBtn = Buttons.createNavButton("← Cofnij");
        backBtn.setOnAction(e -> goBack());
        backBtn.setDisable(true);

        Button quitBtn = Buttons.createNavButton("🏠 Zakończ");
        quitBtn.setOnAction(e -> showStartScreen());

        HBox nav = new HBox(UiConstants.SPACING_NAV, backBtn, quitBtn);
        nav.setAlignment(Pos.CENTER);

        VBox questionBox = new VBox(UiConstants.SPACING_LARGE, counter, title, billInfoBtn, voteButtons, nav);
        questionBox.setAlignment(Pos.CENTER);
        questionBox.setMaxWidth(UiConstants.MAX_WIDTH_QUESTION_BOX);
        questionBox.setPadding(new Insets(30));

        quizScreen = new VBox(questionBox);
        quizScreen.setAlignment(Pos.CENTER);

        setVotingButtonsEnabled(false);
    }

    // Buduje ekran wyników
    private void buildResultsScreen() {
        Label resTitle = new Label("Wyniki");
        resTitle.setStyle(UiStyles.WHITE_TEXT_BOLD);

        clubTable = Tables.createClubResultsTable();
        clubTable.setPrefHeight(UiConstants.TABLE_HEIGHT_CLUBS);

        bestMpLabel = new Label("");
        bestMpLabel.setStyle(UiStyles.WHITE_TEXT_BOLD);

        againBtn = Buttons.createPrimaryButton("🔄 Nowy quiz", "Rozpocznij nowy quiz z innymi pytaniami");
        againBtn.setOnAction(e -> startQuiz());

        goDisciplineFromResultsBtn = Buttons.createPrimaryButton("📊 Dyscyplina partyjna", 
                "Przejdź do analizy dyscypliny partyjnej");
        goDisciplineFromResultsBtn.setOnAction(e -> runDisciplineScan(getSelectedSittings()));

        Button historyFromResultsBtn = Buttons.createSecondaryButton("📋 Historia");
        historyFromResultsBtn.setOnAction(e -> showHistoryScreen());

        Button backToStartBtn = Buttons.createNavButton("🏠 Wróć do startu");
        backToStartBtn.setOnAction(e -> showStartScreen());

        HBox bottom = new HBox(UiConstants.SPACING_BUTTONS, againBtn, goDisciplineFromResultsBtn, historyFromResultsBtn, backToStartBtn);
        bottom.setAlignment(Pos.CENTER);

        resultsScreen = new VBox(UiConstants.SPACING_XLARGE, resTitle, clubTable, bestMpLabel, bottom);
        resultsScreen.setAlignment(Pos.CENTER);
        resultsScreen.setMaxWidth(UiConstants.MAX_WIDTH_RESULTS);
        resultsScreen.setPadding(new Insets(30));
    }

    // Buduje ekran dyscypliny 
    private void buildDisciplineScreen() {
        Label title = new Label("Dyscyplina partyjna");
        title.setStyle(UiStyles.WHITE_TEXT_BOLD);

        Label hint = new Label("Spójność = jak często klub głosuje jednym głosem (średnio po głosowaniach).");
        hint.setWrapText(true);
        hint.setMaxWidth(UiConstants.MAX_WIDTH_TITLE);
        hint.setStyle(UiStyles.WHITE_TEXT_BOLD);

        discClubTable = Tables.createDisciplineClubsTable();
        discClubTable.setPrefHeight(UiConstants.TABLE_HEIGHT_DISCIPLINE_CLUBS);

        Label rebelsLabel = new Label("Top buntowników (liczymy tylko głosowania, gdzie klub był ≥75% zgodny):");
        rebelsLabel.setStyle(UiStyles.WHITE_TEXT_BOLD);

        rebelTable = Tables.createRebelsTable();
        rebelTable.setPrefHeight(UiConstants.TABLE_HEIGHT_REBELS);

        discBackBtn = Buttons.createNavButton("← Wróć");
        discBackBtn.setOnAction(e -> showStartScreen());

        Button rerunBtn = Buttons.createPrimaryButton("🔄 Przelicz ponownie", 
                "Ponownie oblicz dyscyplinę partyjną");
        rerunBtn.setOnAction(e -> runDisciplineScan(getSelectedSittings()));

        HBox bottom = new HBox(UiConstants.SPACING_BUTTONS, rerunBtn, discBackBtn);
        bottom.setAlignment(Pos.CENTER);

        disciplineScreen = new VBox(UiConstants.SPACING_LARGE, title, hint, discClubTable, rebelsLabel, rebelTable, bottom);
        disciplineScreen.setAlignment(Pos.CENTER);
        disciplineScreen.setMaxWidth(UiConstants.MAX_WIDTH_DISCIPLINE);
        disciplineScreen.setPadding(new Insets(30));
    }

    // Buduje ekran historii 
    private void buildHistoryScreen() {
        Label histTitle = new Label("Historia wyników");
        histTitle.setStyle(UiStyles.WHITE_TEXT_BOLD);

        historyTable = Tables.createHistoryTable();
        historyTable.setPrefHeight(350);

        Label emptyHint = new Label("Brak wyników — ukończ quiz, żeby zobaczyć historię.");
        emptyHint.setStyle(UiStyles.WHITE_TEXT_BOLD);
        historyTable.setPlaceholder(emptyHint);

        Button backBtn = Buttons.createNavButton("← Wróć do startu");
        backBtn.setOnAction(e -> showStartScreen());

        HBox bottomBar = new HBox(backBtn);
        bottomBar.setAlignment(Pos.CENTER);

        historyScreen = new VBox(UiConstants.SPACING_LARGE, histTitle, historyTable, bottomBar);
        historyScreen.setAlignment(Pos.CENTER);
        historyScreen.setMaxWidth(UiConstants.MAX_WIDTH_DISCIPLINE);
        historyScreen.setPadding(new Insets(30));
    }

    // Pokazuje historie
    private void showHistoryScreen() {
        historyTable.setItems(FXCollections.observableArrayList(quizHistory));
        screenManager.showScreen(historyScreen);
    }

    // Pokazuje start
    private void showStartScreen() {
        taskManager.cancelRunningTaskIfAny();
        screenManager.showScreen(startScreen);
        taskManager.setIdle(UiConstants.STATUS_READY);
        startQuizBtn.setDisable(false);
        disciplineBtn.setDisable(false);
    }

    // Pokazuje quiz
    private void showQuizScreen() {
        screenManager.showScreen(quizScreen);
    }

    // Pokazuje wyniki
    private void showResultsScreen() {
        screenManager.showScreen(resultsScreen);
    }

    // Pokazuje dyscypline
    private void showDisciplineScreen() {
        screenManager.showScreen(disciplineScreen);
    }

    // zaczyna nowy quiz
    private void startQuiz() {
        taskManager.cancelRunningTaskIfAny();
        quiz.clear();
        userVotes.clear();
        index = 0;
        showQuizScreen();
        setVotingButtonsEnabled(false);
        backBtn.setDisable(true);
        billInfoBtn.setDisable(true);
        counter.setText("");
        title.setText("Losuję pytania");

        QuizLoadingTask task = new QuizLoadingTask(dataService, getSelectedSittings(), UiConstants.QUIZ_QUESTIONS_COUNT);
        taskManager.bindAndRun(task,
                () -> {
                    quiz = task.getValue();
                    if (quiz == null || quiz.isEmpty()) {
                        taskManager.setIdle("Błąd: nie udało się wylosować głosowań. Możliwe, że brak głosowań z drukami w ostatnich posiedzeniach.");
                        ErrorHandler.showError("Brak głosowań", 
                                new Exception("Nie znaleziono wystarczającej liczby głosowań z drukami. Spróbuj ponownie później."));
                        showStartScreen();
                        return;
                    }
                    index = 0;
                    taskManager.setIdle(UiConstants.STATUS_ANSWERING);
                    setVotingButtonsEnabled(true);
                    billInfoBtn.setDisable(false);
                    showQuestion();
                },
                () -> {
                    taskManager.setIdle("Błąd: nie udało się pobrać/losować pytań.");
                    ErrorHandler.showNetworkFriendlyError(task.getException(), "Nie udało się pobrać/losować pytań.");
                    showStartScreen();
                }
        );
    }

    // Wyświetla aktualne pytanie quizu
    private void showQuestion() {
        Voting v = quiz.get(index);
        counter.setText("Pytanie " + (index + 1) + " / " + quiz.size());
        title.setText(v.title);
        backBtn.setDisable(index <= 0);
        billInfoBtn.setDisable(false);
    }

    // Przetwarza odpowiedź użytkownika.
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

    // Cofa się do poprzedniego pytania
    private void goBack() {
        if (index <= 0) {
            return;
        }
        index--;
        Voting v = quiz.get(index);
        userVotes.remove(v.votingNumber);
        showQuestion();
    }

    // Kończy quiz 
    private void finishAndComputeResults() {
        setVotingButtonsEnabled(false);
        billInfoBtn.setDisable(true);
        counter.setText("");
        title.setText("Liczenie dopasowania");

        ResultsComputingTask task = new ResultsComputingTask(dataService, quiz, userVotes);
        taskManager.bindAndRun(task,
                () -> {
                    MatchService.MatchResult r = task.getValue();
                    taskManager.setIdle(UiConstants.STATUS_DONE);
                    List<ClubRow> rows = new ArrayList<>();
                    for (var c : r.getClubsSorted()) {
                        rows.add(new ClubRow(c.getClub(), c.getPct()));
                    }
                    clubTable.setItems(FXCollections.observableArrayList(rows));
                    bestMpLabel.setText(String.format(Locale.US,
                            "Twój poseł bliźniak: %s (%.2f%%)", r.getBestMp(), r.getBestMpPct()));

                    // Zapisz wynik do historii 
                    String topClub = r.getClubsSorted().isEmpty() ? "—" : r.getClubsSorted().get(0).getClub();
                    double topClubPct = r.getClubsSorted().isEmpty() ? 0 : r.getClubsSorted().get(0).getPct();
                    quizHistory.add(new QuizHistoryEntry(
                            LocalDateTime.now(),
                            getSelectedSittings(),
                            quiz.size(),
                            topClub, topClubPct,
                            r.getBestMp(), r.getBestMpPct()
                    ));

                    showResultsScreen();
                },
                () -> {
                    taskManager.setIdle("Błąd: nie udało się policzyć wyniku.");
                    ErrorHandler.showNetworkFriendlyError(task.getException(), "Nie udało się policzyć wyniku.");
                    showStartScreen();
                }
        );
    }

    // Włącza/wyłącza przyciski głosowania
    private void setVotingButtonsEnabled(boolean enabled) {
        yesBtn.setDisable(!enabled);
        noBtn.setDisable(!enabled);
        abstainBtn.setDisable(!enabled);
    }

    // Wyświetla informacje o ustawie dla danego pytania
    private void showBillInfoForCurrentQuestion() {
        if (quiz == null || quiz.isEmpty()) {
            return;
        }
        if (index < 0 || index >= quiz.size()) {
            return;
        }
        Voting current = quiz.get(index);
        setVotingButtonsEnabled(false);
        backBtn.setDisable(true);
        billInfoBtn.setDisable(true);

        BillInfoLoadingTask task = new BillInfoLoadingTask(api, current);
        taskManager.bindAndRun(task,
                () -> {
                    BillInfo info = task.getValue();
                    if (info == null) {
                        taskManager.setIdle(UiConstants.STATUS_CANCELLED);
                        restoreQuizControls();
                        return;
                    }
                    BillInfoDialog.show(info, hostServices);
                    taskManager.setIdle(UiConstants.STATUS_ANSWERING);
                    restoreQuizControls();
                },
                () -> {
                    taskManager.setIdle("Błąd: nie udało się pobrać opisu ustawy.");
                    ErrorHandler.showNetworkFriendlyError(task.getException(), "Nie udało się pobrać opisu ustawy.");
                    restoreQuizControls();
                }
        );
    }

    private void restoreQuizControls() {
        setVotingButtonsEnabled(true);
        backBtn.setDisable(index <= 0);
        billInfoBtn.setDisable(false);
    }

    // Uruchamia analizę dyscypliny 
    private void runDisciplineScan(int lastSittings) {
        taskManager.cancelRunningTaskIfAny();
        showDisciplineScreen();
        discClubTable.setItems(FXCollections.observableArrayList());
        rebelTable.setItems(FXCollections.observableArrayList());

        DisciplineScanTask task = new DisciplineScanTask(api, lastSittings, disciplineAnalyzer);
        taskManager.bindAndRun(task,
                () -> {
                    DisciplineReport rep = task.getValue();
                    if (rep == null) {
                        taskManager.setIdle(UiConstants.STATUS_CANCELLED);
                        return;
                    }
                    List<ClubDiscRow> rows = rep.getClubsSorted().stream()
                            .map(x -> new ClubDiscRow(x.club(), x.avgUnityPct(), x.votingCount()))
                            .toList();
                    discClubTable.setItems(FXCollections.observableArrayList(rows));

                    List<RebelRow> rebelRows = rep.getTopRebels().stream()
                            .map(x -> new RebelRow(x.name(), x.club(), x.rebellionCount()))
                            .toList();
                    rebelTable.setItems(FXCollections.observableArrayList(rebelRows));
                    taskManager.setIdle(UiConstants.STATUS_DONE);
                },
                () -> {
                    taskManager.setIdle("Błąd: nie udało się policzyć dyscypliny.");
                    ErrorHandler.showNetworkFriendlyError(task.getException(), "Nie udało się policzyć dyscypliny.");
                    showStartScreen();
                }
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
