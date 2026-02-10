package pl.project.sejm.ui.components;

import javafx.scene.layout.VBox;

import java.util.List;

// Zarządzanie widocznością ekranów.
public final class ScreenManager {
    
    private final List<VBox> screens;
    
    public ScreenManager(List<VBox> screens) {
        this.screens = List.copyOf(screens);
    }
    
    // Pokazuje wybrany ekran, ukrywa pozostałe.
    public void showScreen(VBox screenToShow) {
        for (VBox screen : screens) {
            boolean visible = screen == screenToShow;
            screen.setVisible(visible);
            screen.setManaged(visible);
        }
    }
}
