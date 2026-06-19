package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Patient;
import system.HospitalSystem;

import java.util.List;

/**
 * DashboardScreen — Live overview of the entire hospital system.
 * Shows stat cards, recent patients table, and queue status.
 */
public class DashboardScreen extends VBox {

    private final HospitalSystem h = MainApp.hospital;

    public DashboardScreen() {
        setSpacing(20);
        setPadding(new Insets(28, 28, 28, 28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = MainApp.sectionLabel("📊  Live Dashboard");
        Button refresh = MainApp.actionBtn("⟳  Refresh", MainApp.ACCENT_BLUE);
        refresh.setOnAction(e -> { getChildren().clear(); build(); });
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp, refresh);

        // ── Stat Cards ────────────────────────────────────────────────────────
        HBox stats = new HBox(14);
        stats.setAlignment(Pos.CENTER_LEFT);

        int total     = h.getRegistry().getTotalCount();
        int admitted  = h.getRegistry().getAdmittedCount();
        int emgQ      = h.getEmergencyQueue().getSize();
        int opdQ      = h.getOpdQueue().getSize();
        int occupied  = h.getBedManager().getOccupiedCount();
        int available = h.getBedManager().getEmptyCount();

        stats.getChildren().addAll(
            MainApp.statCard(String.valueOf(total),     "Total Patients",     MainApp.ACCENT_BLUE),
            MainApp.statCard(String.valueOf(admitted),  "Currently Admitted", MainApp.ACCENT_GREEN),
            MainApp.statCard(String.valueOf(emgQ),      "Emergency Waiting",  MainApp.ACCENT_RED),
            MainApp.statCard(String.valueOf(opdQ),      "OPD Waiting",        MainApp.ACCENT_AMBER),
            MainApp.statCard(String.valueOf(occupied),  "Beds Occupied",      "#7c3aed"),
            MainApp.statCard(String.valueOf(available), "Beds Available",     MainApp.ACCENT_GREEN)
        );

        // ── Bottom row: recent patients + queue peek ──────────────────────────
        HBox bottom = new HBox(16);
        HBox.setHgrow(bottom, Priority.ALWAYS);

        // Recent patients table
        VBox patCard = MainApp.card("👥  All Registered Patients");
        HBox.setHgrow(patCard, Priority.ALWAYS);
        VBox.setVgrow(patCard, Priority.ALWAYS);

        TableView<Patient> table = buildPatientTable();
        List<Patient> all = h.getRegistry().getAllPatients();
        table.getItems().addAll(all);
        VBox.setVgrow(table, Priority.ALWAYS);
        patCard.getChildren().add(table);

        // Queue peek card
        VBox qCard = MainApp.card("🚨  Queue Status");
        qCard.setPrefWidth(260);

        Label emgTitle = new Label("Emergency Queue");
        emgTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emgTitle.setTextFill(Color.web(MainApp.ACCENT_RED));

        Patient emgNext = h.getEmergencyQueue().peek();
        Label emgNext_lbl = new Label(emgNext != null
            ? "Next: " + emgNext.getName() + " (T:" + emgNext.getTriageScore() + ")"
            : "Queue is empty");
        emgNext_lbl.setFont(Font.font("Arial", 12));
        emgNext_lbl.setTextFill(Color.web(MainApp.TEXT_MUTED));

        Label emgCount = new Label("Waiting: " + emgQ + " patient(s)");
        emgCount.setFont(Font.font("Arial", 11));
        emgCount.setTextFill(Color.web(MainApp.ACCENT_RED));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155;");

        Label opdTitle = new Label("OPD Queue");
        opdTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        opdTitle.setTextFill(Color.web(MainApp.ACCENT_AMBER));

        Patient opdNext = h.getOpdQueue().peek();
        Label opdNext_lbl = new Label(opdNext != null
            ? "Next: " + opdNext.getName() + " (T:" + opdNext.getTriageScore() + ")"
            : "Queue is empty");
        opdNext_lbl.setFont(Font.font("Arial", 12));
        opdNext_lbl.setTextFill(Color.web(MainApp.TEXT_MUTED));

        Label opdCount = new Label("Waiting: " + opdQ + " patient(s)");
        opdCount.setFont(Font.font("Arial", 11));
        opdCount.setTextFill(Color.web(MainApp.ACCENT_AMBER));

        // Occupancy bar
        Separator sep2 = new Separator();
        Label occTitle = new Label("Bed Occupancy");
        occTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        occTitle.setTextFill(Color.web(MainApp.TEXT_MAIN));

        double pct = h.getBedManager().getOccupancyPercent();
        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle(pct > 90
            ? "-fx-accent: #e94560;"
            : pct > 70 ? "-fx-accent: #f59e0b;" : "-fx-accent: #10b981;");

        Label occPct = new Label(String.format("%.1f%% occupied (%d / %d beds)",
            pct, occupied, h.getBedManager().getTotalBeds()));
        occPct.setFont(Font.font("Arial", 11));
        occPct.setTextFill(Color.web(MainApp.TEXT_MUTED));

        qCard.getChildren().addAll(
            emgTitle, emgNext_lbl, emgCount,
            sep,
            opdTitle, opdNext_lbl, opdCount,
            sep2,
            occTitle, bar, occPct
        );

        bottom.getChildren().addAll(patCard, qCard);
        VBox.setVgrow(bottom, Priority.ALWAYS);

        getChildren().addAll(header, stats, bottom);
    }

    @SuppressWarnings("unchecked")
    private TableView<Patient> buildPatientTable() {
        TableView<Patient> tv = new TableView<>();
        tv.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-table-cell-border-color: #1e293b;" +
            "-fx-text-fill: #f1f5f9;"
        );
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("No patients registered yet."));

        TableColumn<Patient, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPatientId()));
        colId.setPrefWidth(90);

        TableColumn<Patient, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<Patient, String> colAge = new TableColumn<>("Age");
        colAge.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getAge())));
        colAge.setPrefWidth(50);

        TableColumn<Patient, String> colTriage = new TableColumn<>("Triage");
        colTriage.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getTriageScore())));
        colTriage.setPrefWidth(60);

        TableColumn<Patient, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus().toString()));

        TableColumn<Patient, String> colBed = new TableColumn<>("Bed");
        colBed.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getBedId() != null ? d.getValue().getBedId() : "—"));
        colBed.setPrefWidth(70);

        tv.getColumns().addAll(colId, colName, colAge, colTriage, colStatus, colBed);
        return tv;
    }
}
