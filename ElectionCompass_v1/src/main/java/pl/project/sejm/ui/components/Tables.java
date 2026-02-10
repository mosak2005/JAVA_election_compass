package pl.project.sejm.ui.components;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import pl.project.sejm.ui.model.ClubDiscRow;
import pl.project.sejm.ui.model.ClubRow;
import pl.project.sejm.ui.model.QuizHistoryEntry;
import pl.project.sejm.ui.model.RebelRow;

import java.util.Locale;

// ustalamy styl tabelek
public final class Tables {

    private static final String TABLE_STYLE =
            "-fx-background-color: rgba(255,255,255,0.92);" +
            "-fx-background-radius: 8px;";

    private static final String COLUMN_HEADER_STYLE =
            "-fx-background-color: #2c3e50;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 8px;";

    private Tables() {
    }

    private static <T> TableCell<T, Double> createPercentageCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format(Locale.US, "%.2f%%", item));
            }
        };
    }

    private static <T> void styleTable(TableView<T> table) {
        table.setStyle(TABLE_STYLE);
    }

    private static <S, T> void styleColumn(TableColumn<S, T> col) {
        col.setStyle(COLUMN_HEADER_STYLE);
        Label headerLabel = new Label(col.getText());
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        col.setGraphic(headerLabel);
        col.setText("");
    }

    // zgodnosc klubów tabela
    public static TableView<ClubRow> createClubResultsTable() {
        TableView<ClubRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        styleTable(table);

        TableColumn<ClubRow, String> clubCol = new TableColumn<>("Klub");
        clubCol.setCellValueFactory(new PropertyValueFactory<>("club"));
        styleColumn(clubCol);

        TableColumn<ClubRow, Double> pctCol = new TableColumn<>("Zgodność");
        pctCol.setCellValueFactory(new PropertyValueFactory<>("pct"));
        pctCol.setCellFactory(col -> createPercentageCell());
        styleColumn(pctCol);

        table.getColumns().addAll(clubCol, pctCol);
        return table;
    }

    // dyscyplina klubów tabela
    public static TableView<ClubDiscRow> createDisciplineClubsTable() {
        TableView<ClubDiscRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        styleTable(table);

        TableColumn<ClubDiscRow, String> clubCol = new TableColumn<>("Klub");
        clubCol.setCellValueFactory(new PropertyValueFactory<>("club"));
        styleColumn(clubCol);

        TableColumn<ClubDiscRow, Double> avgCol = new TableColumn<>("Spójność średnio");
        avgCol.setCellValueFactory(new PropertyValueFactory<>("avg"));
        avgCol.setCellFactory(col -> createPercentageCell());
        styleColumn(avgCol);

        TableColumn<ClubDiscRow, Integer> countCol = new TableColumn<>("Głosowań");
        countCol.setCellValueFactory(new PropertyValueFactory<>("count"));
        styleColumn(countCol);

        table.getColumns().addAll(clubCol, avgCol, countCol);
        return table;
    }

    // buntownicy tabela
    public static TableView<RebelRow> createRebelsTable() {
        TableView<RebelRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        styleTable(table);

        TableColumn<RebelRow, String> nameCol = new TableColumn<>("Poseł");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        styleColumn(nameCol);

        TableColumn<RebelRow, String> clubCol = new TableColumn<>("Klub");
        clubCol.setCellValueFactory(new PropertyValueFactory<>("club"));
        styleColumn(clubCol);

        TableColumn<RebelRow, Integer> rebelsCol = new TableColumn<>("Buntów");
        rebelsCol.setCellValueFactory(new PropertyValueFactory<>("rebels"));
        styleColumn(rebelsCol);

        table.getColumns().addAll(nameCol, clubCol, rebelsCol);
        return table;
    }

    // historia
    public static TableView<QuizHistoryEntry> createHistoryTable() {
        TableView<QuizHistoryEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefWidth(880);
        table.setMinWidth(880);
        styleTable(table);

        TableColumn<QuizHistoryEntry, String> timeCol = new TableColumn<>("Godzina");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setMinWidth(70);
        styleColumn(timeCol);

        TableColumn<QuizHistoryEntry, Integer> sittingsCol = new TableColumn<>("Pos.");
        sittingsCol.setCellValueFactory(new PropertyValueFactory<>("sittings"));
        sittingsCol.setMinWidth(45);
        sittingsCol.setMaxWidth(55);
        styleColumn(sittingsCol);

        TableColumn<QuizHistoryEntry, Integer> questionsCol = new TableColumn<>("Pyt.");
        questionsCol.setCellValueFactory(new PropertyValueFactory<>("questions"));
        questionsCol.setMinWidth(45);
        questionsCol.setMaxWidth(55);
        styleColumn(questionsCol);

        TableColumn<QuizHistoryEntry, String> clubCol = new TableColumn<>("Najlepszy klub");
        clubCol.setCellValueFactory(new PropertyValueFactory<>("bestClub"));
        clubCol.setMinWidth(110);
        styleColumn(clubCol);

        TableColumn<QuizHistoryEntry, Double> clubPctCol = new TableColumn<>("% klubu");
        clubPctCol.setCellValueFactory(new PropertyValueFactory<>("bestClubPct"));
        clubPctCol.setCellFactory(col -> createPercentageCell());
        clubPctCol.setMinWidth(75);
        styleColumn(clubPctCol);

        TableColumn<QuizHistoryEntry, String> mpCol = new TableColumn<>("Poseł bliźniak");
        mpCol.setCellValueFactory(new PropertyValueFactory<>("bestMp"));
        mpCol.setMinWidth(180);
        styleColumn(mpCol);

        TableColumn<QuizHistoryEntry, Double> mpPctCol = new TableColumn<>("% posła");
        mpPctCol.setCellValueFactory(new PropertyValueFactory<>("bestMpPct"));
        mpPctCol.setCellFactory(col -> createPercentageCell());
        mpPctCol.setMinWidth(75);
        styleColumn(mpPctCol);

        table.getColumns().addAll(timeCol, sittingsCol, questionsCol, clubCol, clubPctCol, mpCol, mpPctCol);
        return table;
    }
}
