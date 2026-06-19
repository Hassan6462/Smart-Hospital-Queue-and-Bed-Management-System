package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import system.HospitalSystem;

/**
 * MainApp — JavaFX Entry Point
 * Launches the SHQBMS GUI and wires it to HospitalSystem backend.
 */
public class MainApp extends Application {

    // ── Shared backend — ONE instance used by ALL screens ────────────────────
    public static HospitalSystem hospital = new HospitalSystem();

    // ── Color Palette ─────────────────────────────────────────────────────────
    public static final String BG_DARK      = "#0f172a";
    public static final String BG_CARD      = "#1e293b";
    public static final String BG_SIDEBAR   = "#0f3460";
    public static final String ACCENT_BLUE  = "#2563eb";
    public static final String ACCENT_RED   = "#e94560";
    public static final String ACCENT_GREEN = "#10b981";
    public static final String ACCENT_AMBER = "#f59e0b";
    public static final String TEXT_MAIN    = "#f1f5f9";
    public static final String TEXT_MUTED   = "#94a3b8";

    @Override
    public void start(Stage primaryStage) {

        // ── Root layout: Sidebar + Content ───────────────────────────────────
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        // ── Content area (swappable screens) ─────────────────────────────────
        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + BG_DARK + ";");

        // ── Sidebar ───────────────────────────────────────────────────────────
        VBox sidebar = buildSidebar(contentArea);
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        // ── Show Dashboard by default ─────────────────────────────────────────
        showScreen(contentArea, new DashboardScreen());

        // ── Scene ─────────────────────────────────────────────────────────────
        Scene scene = new Scene(root, 1100, 720);
        primaryStage.setTitle("SHQBMS — Smart Hospital Queue & Bed Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    // ── Sidebar Builder ───────────────────────────────────────────────────────

    private VBox buildSidebar(StackPane contentArea) {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color: " + BG_SIDEBAR + ";");

        // Logo / Title
        VBox logoBox = new VBox(4);
        logoBox.setPadding(new Insets(24, 16, 20, 16));
        logoBox.setStyle("-fx-border-color: transparent transparent #ffffff22 transparent; -fx-border-width: 0 0 1 0;");
        Label icon  = new Label("🏥");
        icon.setFont(Font.font(28));
        Label title = new Label("SHQBMS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);
        Label sub = new Label("Hospital Management");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web("#94a3b8"));
        logoBox.getChildren().addAll(icon, title, sub);

        // Nav buttons
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(12, 8, 8, 8));

        Button[] navBtns = {
            navBtn("📊", "Dashboard",        () -> showScreen(contentArea, new DashboardScreen())),
            navBtn("👤", "Register Patient", () -> showScreen(contentArea, new RegisterScreen())),
            navBtn("🚨", "Emergency Queue",  () -> showScreen(contentArea, new QueueScreen("EMERGENCY"))),
            navBtn("🏥", "OPD Queue",        () -> showScreen(contentArea, new QueueScreen("OPD"))),
            navBtn("🛏",  "Bed Manager",      () -> showScreen(contentArea, new BedScreen())),
            navBtn("🌳", "Departments",       () -> showScreen(contentArea, new DeptScreen())),
            navBtn("📋", "Patient Records",  () -> showScreen(contentArea, new RecordsScreen())),
            navBtn("↩️", "Undo Action",      () -> showScreen(contentArea, new UndoScreen())),
            navBtn("📈", "Sort Patients",    () -> showScreen(contentArea, new SortScreen())),
        };

        for (Button b : navBtns) nav.getChildren().add(b);

        // Version footer
        Label ver = new Label("v1.0 · DSA Course Project");
        ver.setFont(Font.font("Arial", 10));
        ver.setTextFill(Color.web("#475569"));
        ver.setPadding(new Insets(8, 16, 12, 16));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoBox, nav, spacer, ver);
        return sidebar;
    }

    // ── Nav Button Factory ────────────────────────────────────────────────────

    private Button navBtn(String emoji, String label, Runnable action) {
        Button btn = new Button(emoji + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #cbd5e1;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #ffffff18;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #cbd5e1;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6;"
        ));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ── Screen Switcher ───────────────────────────────────────────────────────

    public static void showScreen(StackPane area, javafx.scene.Node screen) {
        area.getChildren().setAll(screen);
    }

    // ── Helpers (shared by all screens) ──────────────────────────────────────

    /** Card-style VBox panel */
    public static VBox card(String title) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, #00000055, 8, 0, 0, 2);"
        );
        if (title != null && !title.isEmpty()) {
            Label lbl = new Label(title);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lbl.setTextFill(Color.web(TEXT_MAIN));
            card.getChildren().add(lbl);
        }
        return card;
    }

    /** Styled action button */
    public static Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(9, 20, 9, 20));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
        return btn;
    }

    /** Styled text field */
    public static TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Arial", 13));
        tf.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-text-fill: #f1f5f9;" +
            "-fx-prompt-text-fill: #475569;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8 12 8 12;"
        );
        return tf;
    }

    /** Section label */
    public static Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lbl.setTextFill(Color.web(TEXT_MAIN));
        return lbl;
    }

    /** Muted small label */
    public static Label mutedLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        return lbl;
    }

    /** Stat card widget */
    public static VBox statCard(String value, String label, String color) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(16));
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(160);
        box.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 0 0 0 4;" +
            "-fx-border-radius: 10;"
        );
        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        val.setTextFill(Color.web(color));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        lbl.setWrapText(true);
        lbl.setAlignment(Pos.CENTER);
        box.getChildren().addAll(val, lbl);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
