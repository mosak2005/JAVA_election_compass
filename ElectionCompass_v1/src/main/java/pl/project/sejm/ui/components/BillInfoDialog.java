package pl.project.sejm.ui.components;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import pl.project.sejm.ui.model.BillInfo;
import pl.project.sejm.ui.styles.UiConstants;
import pl.project.sejm.ui.styles.UiStyles;

// Dialog z informacjami o ustawie (druki sejmowe).
public final class BillInfoDialog {

    private static final String PRINT_WEB_PREFIX = "https://www.sejm.gov.pl/sejm10.nsf/druk.xsp?nr=";

    private BillInfoDialog() {
    }

    // Wyświetla modalny dialog z informacjami o ustawie.
    public static void show(BillInfo info, HostServices hostServices) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Informacje o ustawie");

        VBox headerBox = buildHeader(info);
        VBox contentBox = buildContent(info, hostServices);
        ScrollPane scrollPane = wrapInScrollPane(contentBox);
        HBox bottomBar = buildBottomBar(dialog);

        VBox dialogRoot = new VBox(headerBox, scrollPane, bottomBar);
        dialogRoot.setStyle(UiStyles.DIALOG_BACKGROUND);

        Scene scene = new Scene(dialogRoot,
                UiConstants.BILL_INFO_DIALOG_WIDTH,
                UiConstants.BILL_INFO_DIALOG_HEIGHT + 100);
        scene.setFill(Color.TRANSPARENT);

        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    private static VBox buildHeader(BillInfo info) {
        Label headerLabel = new Label(info.getTitle() != null ? info.getTitle() : "Głosowanie");
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(UiConstants.BILL_INFO_DIALOG_WIDTH - 60);
        headerLabel.setStyle(UiStyles.DIALOG_HEADER);

        VBox box = new VBox(headerLabel);
        box.setPadding(new Insets(20, 24, 12, 24));
        return box;
    }

    private static VBox buildContent(BillInfo info, HostServices hostServices) {
        Label topicLabel = new Label("OPIS / TOPIC:");
        topicLabel.setStyle(UiStyles.DIALOG_SECTION_LABEL);

        Label topic = new Label(info.getTopic() == null ? "(brak)" : info.getTopic());
        topic.setWrapText(true);
        topic.setStyle(UiStyles.DIALOG_BODY_TEXT);

        Separator sep = new Separator();
        sep.setStyle(UiStyles.DIALOG_SEPARATOR);

        Label drukiLabel = new Label("DRUKI:");
        drukiLabel.setStyle(UiStyles.DIALOG_SECTION_LABEL);

        VBox drukiBox = buildDrukiSection(info, hostServices);

        VBox content = new VBox(UiConstants.SPACING_MEDIUM, topicLabel, topic, sep, drukiLabel, drukiBox);
        content.setPadding(new Insets(0, 24, 12, 24));
        return content;
    }

    private static VBox buildDrukiSection(BillInfo info, HostServices hostServices) {
        VBox drukiBox = new VBox(UiConstants.SPACING_DRUKI);

        if (info.getDruki().isEmpty()) {
            Label none = new Label("(nie znaleziono numerów druków w tytule)");
            none.setStyle(UiStyles.DIALOG_EMPTY_TEXT);
            drukiBox.getChildren().add(none);
        } else {
            for (String nr : info.getDruki()) {
                String webUrl = PRINT_WEB_PREFIX + nr;

                Hyperlink web = new Hyperlink("📄 Druk " + nr);
                web.setStyle(UiStyles.DIALOG_LINK);
                web.setOnAction(e -> hostServices.showDocument(webUrl));

                String titleFromApi = info.getPrints().stream()
                        .filter(p -> nr.equals(p.number))
                        .map(p -> p.title)
                        .findFirst()
                        .orElse(null);

                Label tLabel = new Label(titleFromApi != null ? titleFromApi : "");
                tLabel.setWrapText(true);
                tLabel.setStyle(UiStyles.DIALOG_BODY_SMALL);

                VBox one = new VBox(UiConstants.SPACING_DRUKI_ITEM, web, tLabel);
                one.setPadding(new Insets(UiConstants.PADDING_DRUKI_ITEM, 0, UiConstants.PADDING_DRUKI_ITEM, 0));
                drukiBox.getChildren().add(one);
            }
        }

        return drukiBox;
    }

    private static ScrollPane wrapInScrollPane(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setPrefViewportHeight(UiConstants.BILL_INFO_DIALOG_HEIGHT);
        sp.setStyle(UiStyles.TRANSPARENT_SCROLL);
        return sp;
    }

    private static HBox buildBottomBar(Stage dialog) {
        Button closeBtn = Buttons.createPrimaryButton("Zamknij");
        closeBtn.setOnAction(e -> dialog.close());

        HBox bar = new HBox(closeBtn);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(8, 24, 16, 24));
        return bar;
    }
}
