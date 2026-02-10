package pl.project.sejm.ui.components;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.List;

public final class TaskManager {
    private Task<?> runningTask;
    private final Label statusLabel;
    private final ProgressBar progressBar;
    private final List<Button> buttonsToDisable;

    public TaskManager(Label statusLabel, ProgressBar progressBar, List<Button> buttonsToDisable) {
        this.statusLabel = statusLabel;
        this.progressBar = progressBar;
        this.buttonsToDisable = buttonsToDisable;
    }

    public void bindAndRun(Task<?> task, Runnable onSuccess, Runnable onFail) {
        runningTask = task;
        
        setButtonsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
        
        statusLabel.textProperty().unbind();
        progressBar.progressProperty().unbind();
        statusLabel.textProperty().bind(task.messageProperty());
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
            setIdle("Status: przerwano.");
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void cleanupTaskBinding() {
        runningTask = null;
        statusLabel.textProperty().unbind();
        progressBar.progressProperty().unbind();
        progressBar.setVisible(false);
        progressBar.setManaged(false);
        setButtonsEnabled(true);
    }

    public void cancelRunningTaskIfAny() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }

    public void setIdle(String text) {
        statusLabel.textProperty().unbind();
        statusLabel.setText(text);
        progressBar.progressProperty().unbind();
        progressBar.setVisible(false);
        progressBar.setManaged(false);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (buttonsToDisable != null) {
            for (Button btn : buttonsToDisable) {
                if (btn != null) {
                    btn.setDisable(!enabled);
                }
            }
        }
    }
}
