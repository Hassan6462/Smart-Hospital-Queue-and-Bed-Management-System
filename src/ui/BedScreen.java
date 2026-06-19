package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import manager.BedManager;
import system.HospitalSystem;

/**
 * BedScreen — Visual 2D grid of all hospital beds.
 * Shows EMPTY / OCCUPIED / RESERVED with color coding.
 * Allows manual free bed action.
 */
public class BedScreen extends VBox {

    private final HospitalSystem h  = MainApp.hospital;
    private final BedManager     bm = h.getBedManager();
    private final Label resultLabel = new Label();

    private static final int ROWS = 5;
    private static final int COLS = 10;

    public BedScreen() {
        setSpacing(20);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        Label title = MainApp.sectionLabel("🛏  Bed Manager — 2D Array Grid");
        Label sub   = MainApp.mutedLabel(
            "5 floors × 10 rooms = 50 beds total. " +
            "Data structure: 2D Array — grid[floor][room]");

        // ── Stat row ──────────────────────────────────────────────────────────
        HBox stats = new HBox(14);
        stats.getChildren().addAll(
            MainApp.statCard(String.valueOf(bm.getTotalBeds()),    "Total Beds",   MainApp.ACCENT_BLUE),
            MainApp.statCard(String.valueOf(bm.getOccupiedCount()), "Occupied",    MainApp.ACCENT_RED),
            MainApp.statCard(String.valueOf(bm.getEmptyCount()),    "Available",   MainApp.ACCENT_GREEN),
            MainApp.statCard(String.valueOf(bm.getReservedCount()), "Reserved",    MainApp.ACCENT_AMBER),
            MainApp.statCard(String.format("%.0f%%", bm.getOccupancyPercent()), "Occupancy", "#7c3aed")
        );

        // ── Bed Grid ──────────────────────────────────────────────────────────
        VBox gridCard = MainApp.card("🗺  Hospital Floor Map");
        VBox.setVgrow(gridCard, Priority.ALWAYS);

        // Legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.getChildren().addAll(
            legendItem("🟩", "Empty (available)"),
            legendItem("🟥", "Occupied"),
            legendItem("🟨", "Reserved"),
            legendItem("", "Click occupied bed to free it")
        );

        // Column headers
        HBox colHeaders = new HBox(4);
        colHeaders.setPadding(new Insets(0, 0, 0, 90));
        for (int c = 0; c < COLS; c++) {
            Label h = new Label("R" + c);
            h.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            h.setTextFill(Color.web(MainApp.TEXT_MUTED));
            h.setMinWidth(52);
            h.setAlignment(Pos.CENTER);
            colHeaders.getChildren().add(h);
        }

        // Bed grid rows
        VBox gridRows = new VBox(4);
        for (int row = 0; row < ROWS; row++) {
            HBox rowBox = new HBox(4);
            rowBox.setAlignment(Pos.CENTER_LEFT);

            // Floor label
            Label floorLbl = new Label("Floor " + row);
            floorLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            floorLbl.setTextFill(Color.web(MainApp.TEXT_MUTED));
            floorLbl.setMinWidth(80);

            rowBox.getChildren().add(floorLbl);

            for (int col = 0; col < COLS; col++) {
                String bedId = "F" + row + "-R" + col;
                Button cell  = makeBedCell(bedId, row, col);
                rowBox.getChildren().add(cell);
            }
            gridRows.getChildren().add(rowBox);
        }

        // ── Manual controls ───────────────────────────────────────────────────
        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);

        TextField bedIdField  = MainApp.field("Bed ID (e.g. F2-R5)");
        TextField patIdField  = MainApp.field("Patient ID (e.g. P-1001)");
        bedIdField.setPrefWidth(160);
        patIdField.setPrefWidth(160);

        Button freeBtn    = MainApp.actionBtn("🔓 Free Bed",    MainApp.ACCENT_GREEN);
        Button reserveBtn = MainApp.actionBtn("🔒 Reserve Bed", MainApp.ACCENT_AMBER);
        Button refreshBtn = MainApp.actionBtn("⟳ Refresh",     MainApp.ACCENT_BLUE);

        freeBtn.setOnAction(e -> {
            String bid = bedIdField.getText().trim();
            if (bid.isEmpty()) { showMsg("Enter a Bed ID.", false); return; }
            boolean ok = bm.freeBed(bid);
            showMsg(ok ? "Bed " + bid + " freed." : "Could not free bed " + bid, ok);
            getChildren().clear(); build();
        });

        reserveBtn.setOnAction(e -> {
            String bid = bedIdField.getText().trim();
            if (bid.isEmpty()) { showMsg("Enter a Bed ID.", false); return; }
            boolean ok = bm.reserveBed(bid);
            showMsg(ok ? "Bed " + bid + " reserved." : "Could not reserve " + bid, ok);
            getChildren().clear(); build();
        });

        refreshBtn.setOnAction(e -> { getChildren().clear(); build(); });

        controls.getChildren().addAll(bedIdField, patIdField, freeBtn, reserveBtn, refreshBtn);

        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        resultLabel.setWrapText(true);

        gridCard.getChildren().addAll(legend, colHeaders, gridRows, controls, resultLabel);

        getChildren().addAll(title, sub, stats, gridCard);
    }

    // ── Build individual bed cell button ──────────────────────────────────────

    private Button makeBedCell(String bedId, int row, int col) {
        // Check state by trying to read what bed manager knows
        String patient = bm.getPatientInBed(bedId);
        boolean empty  = bm.isAvailable(bedId);

        String bg, text, tooltip;
        if (patient != null) {
            bg      = "#7f1d1d";  // dark red = occupied
            text    = "✕";
            tooltip = bedId + "\n" + patient;
        } else if (!empty) {
            bg      = "#78350f";  // dark amber = reserved
            text    = "R";
            tooltip = bedId + "\nRESERVED";
        } else {
            bg      = "#14532d";  // dark green = empty
            text    = "✓";
            tooltip = bedId + "\nEMPTY";
        }

        Button cell = new Button(text);
        cell.setMinWidth(48);
        cell.setMinHeight(36);
        cell.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        cell.setTooltip(new Tooltip(tooltip));
        cell.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );

        // Click occupied bed → offer to free it
        if (patient != null) {
            cell.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Free Bed?");
                alert.setHeaderText("Bed " + bedId);
                alert.setContentText("Currently occupied by: " + patient + "\n\nFree this bed?");
                styleAlert(alert);
                alert.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        bm.freeBed(bedId);
                        getChildren().clear(); build();
                    }
                });
            });
        }

        return cell;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HBox legendItem(String icon, String label) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        Label ic  = new Label(icon);
        ic.setFont(Font.font(14));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web(MainApp.TEXT_MUTED));
        box.getChildren().addAll(ic, lbl);
        return box;
    }

    private void showMsg(String msg, boolean success) {
        resultLabel.setText(success ? "✅  " + msg : "⚠  " + msg);
        resultLabel.setTextFill(Color.web(success ? MainApp.ACCENT_GREEN : MainApp.ACCENT_RED));
    }

    private void styleAlert(Alert alert) {
        alert.getDialogPane().setStyle(
            "-fx-background-color: #1e293b;" +
            "-fx-text-fill: white;"
        );
    }
}
