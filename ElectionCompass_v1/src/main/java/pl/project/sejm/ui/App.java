package pl.project.sejm.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Election Compass");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label status = new Label("Status: gotowy");

        Button startBtn = new Button("Start");
        startBtn.setDefaultButton(true);

        startBtn.setOnAction(e -> {
            startBtn.setDisable(true);
            status.setText("Status: pobieram głosowania… (na razie demo)");
        });

        VBox root = new VBox(14, title, startBtn, status);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Election Compass");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}