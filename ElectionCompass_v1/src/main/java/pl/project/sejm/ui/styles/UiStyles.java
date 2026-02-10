package pl.project.sejm.ui.styles;


public final class UiStyles {
    
    private UiStyles() {
    }
    
    public static final String HEADER = "-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;";
    public static final String STATUS = "-fx-opacity: 0.85; -fx-text-fill: white;";
    public static final String WHITE_TEXT = "-fx-text-fill: white;";
    public static final String WHITE_TEXT_BOLD = "-fx-font-weight: bold; -fx-text-fill: white;";
    public static final String WHITE_TEXT_SEMIBOLD = "-fx-font-weight: 600; -fx-text-fill: white;";
    public static final String WHITE_TEXT_OPACITY = "-fx-opacity: 0.85; -fx-text-fill: white;";
    public static final String WHITE_TEXT_SMALL = "-fx-font-size: 14px; -fx-opacity: 0.8; -fx-text-fill: white;";
    public static final String WHITE_TEXT_MEDIUM = "-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: white;";
    public static final String WHITE_TEXT_LARGE = "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: white;";
    public static final String WHITE_TEXT_TITLE = "-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: white;";
    
    //alerty
    public static final String BLACK_TEXT = "-fx-text-fill: black;";
    public static final String BLACK_TEXT_BOLD = "-fx-font-weight: bold; -fx-text-fill: black;";
    public static final String BLACK_TEXT_OPACITY = "-fx-opacity: 0.85; -fx-text-fill: black;";
    
    // komponenty
    public static final String CARD = 
        "-fx-background-color: rgba(255,255,255,0.1);" +
        "-fx-background-radius: 12px;" +
        "-fx-border-color: rgba(255,255,255,0.2);" +
        "-fx-border-width: 1px;" +
        "-fx-border-radius: 12px;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 3);";
    
    // progress bar
    public static final String PROGRESS_BAR = 
        "-fx-accent: #4a90e2;" +
        "-fx-background-color: rgba(255,255,255,0.2);" +
        "-fx-background-radius: 4px;";
    
    
    public static final String SEPARATOR = 
        "-fx-background-color: rgba(255,255,255,0.2);";
    
    // druk info
    public static final String DIALOG_HEADER = 
        "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;";
    public static final String DIALOG_SECTION_LABEL = 
        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.6);";
    public static final String DIALOG_BODY_TEXT = 
        "-fx-text-fill: white; -fx-font-size: 13px;";
    public static final String DIALOG_BODY_SMALL = 
        "-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px;";
    public static final String DIALOG_EMPTY_TEXT = 
        "-fx-text-fill: rgba(255,255,255,0.5); -fx-font-style: italic;";
    public static final String DIALOG_LINK = 
        "-fx-text-fill: #5dade2; -fx-font-size: 13px; -fx-font-weight: bold;";
    public static final String DIALOG_SEPARATOR = 
        "-fx-background-color: rgba(255,255,255,0.15);";
    public static final String DIALOG_BACKGROUND = 
        "-fx-background-color: linear-gradient(to bottom, #1a2a3a, #0d1b2a);" +
        "-fx-background-radius: 0;";
    public static final String TRANSPARENT_SCROLL = 
        "-fx-background: transparent; -fx-background-color: transparent;";
}
