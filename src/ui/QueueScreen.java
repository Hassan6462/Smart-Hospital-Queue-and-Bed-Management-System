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

/**
 * QueueScreen — Shows Emergency or OPD queue and allows calling next patient.
 * "EMERGENCY" type shows Min-Heap queue.
 * "OPD"       type shows FIFO queue.
 */
public class QueueScreen extends VBox {

    private final HospitalSystem h    = MainApp.hospital;
    private final String         type; // "EMERGENCY" or "OPD"
    private final Label          resultLabel = new Label();

    public QueueScreen(String type) {
        this.type = type;
        setSpacing(20);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        boolean isEmg   = type.equals("EMERGENCY");
        String  color   = isEmg ? MainApp.ACCENT_RED   : MainApp.ACCENT_AMBER;
        String  emoji   = isEmg ? "🚨" : "🏥";
        String  name    = isEmg ? "Emergency Queue (Min-Heap Priority)" : "OPD Queue (FIFO)";
        int     count   = isEmg ? h.getEmergencyQueue().getSize() : h.getOpdQueue().getSize();
        Patient next    = isEmg ? h.getEmergencyQueue().peek()    : h.getOpdQueue().peek();

        // ── Header ────────────────────────────────────────────────────────────
        Label title = MainApp.sectionLabel(emoji + "  " + name);
        String ds = isEmg
            ? "Min-Heap: lowest triage score always served first. Ties broken by arrival time."
            : "FIFO Circular Array: first-come, first-served. Triage 3-5 only.";
        Label sub = MainApp.mutedLabel("Data Structure: " + ds);

        // ── Stat bar ──────────────────────────────────────────────────────────
        HBox statBar = new HBox(16);
        statBar.setAlignment(Pos.CENTER_LEFT);

        VBox countCard = MainApp.statCard(String.valueOf(count), "Patients Waiting", color);
        VBox nextCard  = MainApp.statCard(
            next != null ? "T:" + next.getTriageScore() : "—",
            next != null ? "Next: " + next.getName()    : "Queue Empty",
            color
        );
        statBar.getChildren().addAll(countCard, nextCard);

        // ── Call button ───────────────────────────────────────────────────────
        Button callBtn = MainApp.actionBtn(emoji + "  Call Next Patient", color);
        callBtn.setOnAction(e -> callNext());

        Button refreshBtn = MainApp.actionBtn("⟳  Refresh", MainApp.ACCENT_BLUE);
        refreshBtn.setOnAction(e -> { getChildren().clear(); build(); });

        HBox btnRow = new HBox(12, callBtn, refreshBtn);

        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        resultLabel.setWrapText(true);

        // ── Queue list card ───────────────────────────────────────────────────
        VBox qCard = MainApp.card(emoji + "  Patients in Queue");
        VBox.setVgrow(qCard, Priority.ALWAYS);

        if (count == 0) {
            Label empty = new Label("No patients currently waiting in this queue.");
            empty.setFont(Font.font("Arial", 13));
            empty.setTextFill(Color.web(MainApp.TEXT_MUTED));
            qCard.getChildren().add(empty);
        } else {
            // Header row
            HBox hdr = queueRowHeader();
            qCard.getChildren().add(hdr);

            // Build list — we can't iterate the internal heap/queue directly,
            // so we use the registry and filter by status + triage
            int pos = 1;
            for (Patient p : h.getRegistry().getAllPatients()) {
                if (p.getStatus() != Patient.Status.WAITING) continue;
                boolean isEmgPat = p.getTriageScore() <= 2;
                if (isEmg != isEmgPat) continue;

                HBox row = queueRow(pos++, p, color);
                qCard.getChildren().add(row);
            }
        }

        getChildren().addAll(title, sub, statBar, btnRow, resultLabel, qCard);
    }

    // ── Call Next Patient ─────────────────────────────────────────────────────

    private void callNext() {
        Patient called = type.equals("EMERGENCY")
            ? h.callNextEmergency()
            : h.callNextOPD();

        if (called == null) {
            resultLabel.setText("⚠  Queue is empty — no patients to call.");
            resultLabel.setTextFill(Color.web(MainApp.ACCENT_RED));
        } else {
            String bed = called.getBedId() != null ? called.getBedId() : "No bed available";
            resultLabel.setText("✅  Called: " + called.getName()
                + "  |  Triage: " + called.getTriageScore()
                + "  |  Assigned Bed: " + bed);
            resultLabel.setTextFill(Color.web(MainApp.ACCENT_GREEN));
        }

        // Rebuild screen
        getChildren().clear();
        build();
        // Re-add result (build() clears it)
    }

    // ── Row builders ──────────────────────────────────────────────────────────

    private HBox queueRowHeader() {
        HBox row = new HBox();
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 6;");

        String[] cols   = {"#", "Patient ID", "Name", "Age", "Triage", "Complaint"};
        double[] widths = {40,  100,           200,    60,    70,       300};

        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            lbl.setTextFill(Color.WHITE);
            lbl.setMinWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }

    private HBox queueRow(int pos, Patient p, String accentColor) {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(pos % 2 == 0
            ? "-fx-background-color: #1e293b; -fx-background-radius: 6;"
            : "-fx-background-color: #162032; -fx-background-radius: 6;");

        boolean isNext = pos == 1;

        // Position
        Label posLbl = new Label(isNext ? "▶" : String.valueOf(pos));
        posLbl.setMinWidth(40);
        posLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        posLbl.setTextFill(isNext ? Color.web(accentColor) : Color.web(MainApp.TEXT_MUTED));

        // ID
        Label idLbl = new Label(p.getPatientId());
        idLbl.setMinWidth(100);
        idLbl.setFont(Font.font("Arial", 12));
        idLbl.setTextFill(Color.web(MainApp.TEXT_MUTED));

        // Name
        Label nameLbl = new Label(p.getName());
        nameLbl.setMinWidth(200);
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        nameLbl.setTextFill(Color.web(MainApp.TEXT_MAIN));

        // Age
        Label ageLbl = new Label(String.valueOf(p.getAge()));
        ageLbl.setMinWidth(60);
        ageLbl.setFont(Font.font("Arial", 12));
        ageLbl.setTextFill(Color.web(MainApp.TEXT_MUTED));

        // Triage badge
        String tc = triageColor(p.getTriageScore());
        Label triageLbl = new Label("T:" + p.getTriageScore());
        triageLbl.setMinWidth(70);
        triageLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        triageLbl.setTextFill(Color.web(tc));
        triageLbl.setStyle("-fx-background-color: " + tc + "33; -fx-background-radius: 4; -fx-padding: 2 6 2 6;");

        // Complaint
        Label compLbl = new Label(p.getComplaint());
        compLbl.setFont(Font.font("Arial", 12));
        compLbl.setTextFill(Color.web(MainApp.TEXT_MUTED));

        row.getChildren().addAll(posLbl, idLbl, nameLbl, ageLbl, triageLbl, compLbl);
        return row;
    }

    private String triageColor(int t) {
        switch (t) {
            case 1: return "#e94560";
            case 2: return "#f97316";
            case 3: return "#f59e0b";
            case 4: return "#10b981";
            default: return "#60a5fa";
        }
    }
}
