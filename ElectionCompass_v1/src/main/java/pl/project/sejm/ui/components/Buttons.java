package pl.project.sejm.ui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

// Tworzenie stylowanych przycisków.
public final class Buttons {

    private Buttons() {
    }

    private static final String BASE = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
            "-fx-padding: 10px 24px; -fx-background-radius: 8px; -fx-cursor: hand;";

    public static Button createPrimaryButton(String text) {
        return createPrimaryButton(text, null);
    }

    public static Button createPrimaryButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setStyle(BASE + "-fx-background-color: #357abd;");
        if (tooltip != null && !tooltip.isEmpty()) {
            btn.setTooltip(new Tooltip(tooltip));
        }
        return btn;
    }

    public static Button createYesButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BASE + "-fx-background-color: #27ae60; -fx-font-size: 16px;");
        return btn;
    }

    public static Button createNoButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BASE + "-fx-background-color: #c0392b; -fx-font-size: 16px;");
        return btn;
    }

    public static Button createAbstainButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BASE + "-fx-background-color: #e67e22; -fx-font-size: 16px;");
        return btn;
    }

    public static Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BASE + "-fx-background-color: rgba(255,255,255,0.15); -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 8px;");
        return btn;
    }

    public static Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BASE + "-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 8px;");
        return btn;
    }
}
