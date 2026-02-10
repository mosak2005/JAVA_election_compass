package pl.project.sejm.ui.components;

import java.net.URL;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;

public final class BackgroundManager {
    private static final double BACKGROUND_BRIGHTNESS = -0.6;
    
    private final ImageView backgroundImageView;
    private final BorderPane root;
    private final Class<?> resourceClass;

    public BackgroundManager(ImageView backgroundImageView, BorderPane root, Class<?> resourceClass) {
        this.backgroundImageView = backgroundImageView;
        this.root = root;
        this.resourceClass = resourceClass;
    }

    public void setBackgroundImage(String path) {
        if (path == null || path.isBlank() || root == null) {
            return;
        }
        
        try {
            Image img;
            if (path.startsWith("classpath:")) {
                String res = path.substring("classpath:".length());
                URL url = resourceClass.getResource(res);
                if (url == null) {
                    throw new IllegalArgumentException("Resource not found: " + res);
                }
                img = new Image(url.toExternalForm());
            } else {
                String urlStr = path;
                if (!urlStr.startsWith("file:") && !urlStr.startsWith("http:") && !urlStr.startsWith("https:")) {
                    urlStr = "file:" + urlStr;
                }
                img = new Image(urlStr);
            }
            
            if (backgroundImageView != null) {
                backgroundImageView.setImage(img);
                ColorAdjust ca = new ColorAdjust();
                ca.setBrightness(BACKGROUND_BRIGHTNESS);
                backgroundImageView.setEffect(ca);
            } else {
                BackgroundSize bsize = new BackgroundSize(100, 100, true, true, false, true);
                BackgroundImage bimg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, 
                        BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bsize);
                root.setBackground(new Background(bimg));
            }
        } catch (Exception ex) {
            System.err.println("Nie udało się ustawić tła: " + ex.getMessage());
        }
    }
}
