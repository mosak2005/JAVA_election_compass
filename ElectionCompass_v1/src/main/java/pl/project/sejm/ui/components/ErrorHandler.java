package pl.project.sejm.ui.components;

import javafx.scene.control.Alert;
import pl.project.sejm.SejmApiException;

public final class ErrorHandler {
    
    private ErrorHandler() {
        // Klasa utility - nie instancjonować
    }
    
    public static void showError(String header, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(header);
        alert.setContentText(ex == null ? "Nieznany błąd." : String.valueOf(ex.getMessage()));
        alert.showAndWait();
    }

    public static void showNetworkFriendlyError(Throwable ex, String fallbackMessage) {
        if (ex == null) {
            showError("Błąd sieci", new Exception(fallbackMessage));
            return;
        }
        
        Throwable rootEx = ex;
        while (rootEx.getCause() != null) {
            rootEx = rootEx.getCause();
        }
        
        if (rootEx instanceof SejmApiException) {
            SejmApiException sae = (SejmApiException) rootEx;
            if (sae.isNetworkError()) {
                showError("Problem z połączeniem sieciowym", 
                        new Exception("Nie można połączyć się z API. Sprawdź połączenie internetowe i spróbuj ponownie."));
                return;
            }
        }
        
        showError(fallbackMessage, ex instanceof Exception ? ex : new Exception(String.valueOf(ex)));
    }
}
