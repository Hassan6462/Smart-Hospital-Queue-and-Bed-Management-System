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
 * RecordsScreen — Search patients, view linked list history, discharge.
 */
public class RecordsScreen extends VBox {

    private final HospitalSystem h = MainApp.hospital;
    private final VBox detailPane  = MainApp.card("📋  Patient Details");
    private final Label resultLbl  = new Label();

    public RecordsScreen() {
        setSpacing(20);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        Label title = MainApp.sectionLabel("📋  Patient Records");
        Label sub   = MainApp.mutedLabel("Search by ID (BST O(log n)) or by Name (O(n) scan). View linked list history.");

        // ── Search controls ───────────────────────────────────────────────────
        VBox searchCard = MainApp.card("🔍  Search Patient");
        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

        TextField idField   = MainApp.field("Search by Patient ID (e.g. P-1001)");
        TextField nameField = MainApp.field("Search by Name");
        idField.setPrefWidth(240);
        nameField.setPrefWidth(240);

        Button byIdBtn   = MainApp.actionBtn("Search by ID",   MainApp.ACCENT_BLUE);
        Button byNameBtn = MainApp.actionBtn("Search by Name", MainApp.ACCENT_BLUE);

        byIdBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) return;
            Patient p = h.getRegistry().searchById(id);
            showDetail(p != null ? List.of(p) : List.of());
        });

        byNameBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) return;
            showDetail(h.getRegistry().searchByName(name));
        });

        row1.getChildren().addAll(idField, byIdBtn, nameField, byNameBtn);

        Button showAllBtn = MainApp.actionBtn("📋 Show All Patients", "#475569");
        showAllBtn.setOnAction(e -> showDetail(h.getRegistry().getAllPatients()));

        resultLbl.setFont(Font.font("Arial", 12));
        resultLbl.setTextFill(Color.web(MainApp.TEXT_MUTED));

        searchCard.getChildren().addAll(row1, showAllBtn, resultLbl);

        // ── Content layout ────────────────────────────────────────────────────
        HBox content = new HBox(16);
        VBox.setVgrow(content, Priority.ALWAYS);

        // Left: patient list
        VBox listCard = MainApp.card("👥  Results");
        listCard.setPrefWidth(420);
        VBox.setVgrow(listCard, Priority.ALWAYS);
        listCard.setId("listCard");

        // Right: detail pane
        VBox.setVgrow(detailPane, Priority.ALWAYS);
        HBox.setHgrow(detailPane, Priority.ALWAYS);
        Label placeholder = new Label("← Select a patient to view details");
        placeholder.setFont(Font.font("Arial", 13));
        placeholder.setTextFill(Color.web(MainApp.TEXT_MUTED));
        detailPane.getChildren().add(placeholder);

        content.getChildren().addAll(listCard, detailPane);

        getChildren().addAll(title, sub, searchCard, content);

        // Show all patients by default
        showDetail(h.getRegistry().getAllPatients());
    }

    // ── Show patient list ─────────────────────────────────────────────────────

    private void showDetail(List<Patient> patients) {
        // Find list card
        VBox listCard = null;
        for (var node : ((HBox)getChildren().get(3)).getChildren()) {
            if (node instanceof VBox && ((VBox) node).getId() != null
                && ((VBox) node).getId().equals("listCard")) {
                listCard = (VBox) node;
            }
        }
        if (listCard == null) return;

        listCard.getChildren().clear();

        Label hdr = new Label("👥  Results (" + patients.size() + ")");
        hdr.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hdr.setTextFill(Color.web(MainApp.TEXT_MAIN));
        listCard.getChildren().add(hdr);

        if (patients.isEmpty()) {
            Label empty = new Label("No patients found.");
            empty.setFont(Font.font("Arial", 13));
            empty.setTextFill(Color.web(MainApp.TEXT_MUTED));
            listCard.getChildren().add(empty);
            return;
        }

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox list = new VBox(6);
        for (Patient p : patients) {
            list.getChildren().add(patientRow(p));
        }
        scroll.setContent(list);
        listCard.getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    private VBox patientRow(Patient p) {
        VBox row = new VBox(4);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle(
            "-fx-background-color: #1e293b;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        String tc = triageColor(p.getTriageScore());

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(p.getName());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        name.setTextFill(Color.web(MainApp.TEXT_MAIN));
        Label triage = new Label("T:" + p.getTriageScore());
        triage.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        triage.setTextFill(Color.web(tc));
        triage.setStyle("-fx-background-color: " + tc + "33; -fx-background-radius: 4; -fx-padding: 2 6 2 6;");
        Label status = new Label(p.getStatus().toString());
        status.setFont(Font.font("Arial", 11));
        status.setTextFill(Color.web(statusColor(p.getStatus())));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        top.getChildren().addAll(name, triage, sp, status);

        Label id = new Label(p.getPatientId() + "  |  Age: " + p.getAge()
            + "  |  Bed: " + (p.getBedId() != null ? p.getBedId() : "—"));
        id.setFont(Font.font("Arial", 11));
        id.setTextFill(Color.web(MainApp.TEXT_MUTED));

        row.getChildren().addAll(top, id);

        row.setOnMouseEntered(e -> row.setStyle(
            "-fx-background-color: #263348; -fx-background-radius: 8; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle(
            "-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-cursor: hand;"));
        row.setOnMouseClicked(e -> showPatientDetail(p));

        return row;
    }

    // ── Patient detail view ───────────────────────────────────────────────────

    private void showPatientDetail(Patient p) {
        detailPane.getChildren().clear();

        Label titleLbl = new Label("👤  " + p.getName());
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.web(MainApp.TEXT_MAIN));

        String tc = triageColor(p.getTriageScore());

        // Info grid
        GridPane info = new GridPane();
        info.setHgap(16); info.setVgap(8);

        String[][] fields = {
            {"Patient ID",    p.getPatientId()},
            {"Name",          p.getName()},
            {"Age",           String.valueOf(p.getAge())},
            {"Gender",        p.getGender().toString()},
            {"Contact",       p.getContact()},
            {"Triage",        p.getTriageScore() + " — " + triageName(p.getTriageScore())},
            {"Status",        p.getStatus().toString()},
            {"Bed Assigned",  p.getBedId() != null ? p.getBedId() : "Not assigned"},
            {"Complaint",     p.getComplaint()},
        };

        for (int i = 0; i < fields.length; i++) {
            Label key = new Label(fields[i][0] + ":");
            key.setFont(Font.font("Arial", 12));
            key.setTextFill(Color.web(MainApp.TEXT_MUTED));
            key.setMinWidth(110);

            Label val = new Label(fields[i][1]);
            val.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            val.setTextFill(i == 5 ? Color.web(tc)
                : i == 6 ? Color.web(statusColor(p.getStatus()))
                : Color.web(MainApp.TEXT_MAIN));
            val.setWrapText(true);

            info.add(key, 0, i);
            info.add(val, 1, i);
        }

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155;");

        // Patient history (Linked List)
        Label histTitle = new Label("📜  Patient History (Linked List)");
        histTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        histTitle.setTextFill(Color.web(MainApp.ACCENT_BLUE));

        String histText = h.getRegistry().getPatientHistoryString(p.getPatientId());
        TextArea histArea = new TextArea(histText.isEmpty() ? "No events recorded." : histText);
        histArea.setEditable(false);
        histArea.setPrefRowCount(5);
        histArea.setFont(Font.font("Courier New", 11));
        histArea.setStyle(
            "-fx-control-inner-background: #0f172a;" +
            "-fx-text-fill: #94a3b8;" +
            "-fx-background-color: #0f172a;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 6;"
        );

        // Action buttons
        HBox actions = new HBox(10);

        if (p.getStatus() != Patient.Status.DISCHARGED) {
            Button dischargeBtn = MainApp.actionBtn("🏠 Discharge Patient", MainApp.ACCENT_RED);
            dischargeBtn.setOnAction(e -> {
                h.dischargePatient(p.getPatientId());
                resultLbl.setText("Patient " + p.getName() + " discharged.");
                resultLbl.setTextFill(Color.web(MainApp.ACCENT_GREEN));
                showPatientDetail(h.getRegistry().searchById(p.getPatientId()));
            });
            actions.getChildren().add(dischargeBtn);
        }

        Button refreshBtn = MainApp.actionBtn("⟳ Refresh", MainApp.ACCENT_BLUE);
        refreshBtn.setOnAction(e -> {
            Patient updated = h.getRegistry().searchById(p.getPatientId());
            if (updated != null) showPatientDetail(updated);
        });
        actions.getChildren().add(refreshBtn);

        detailPane.getChildren().addAll(
            titleLbl, info, sep, histTitle, histArea, actions);
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

    private String triageName(int t) {
        String[] names = {"", "Resuscitation", "Emergent", "Urgent", "Less Urgent", "Non-Urgent"};
        return t >= 1 && t <= 5 ? names[t] : "Unknown";
    }

    private String statusColor(Patient.Status s) {
        switch (s) {
            case WAITING:    return MainApp.ACCENT_AMBER;
            case ADMITTED:   return MainApp.ACCENT_GREEN;
            case DISCHARGED: return MainApp.TEXT_MUTED;
            default:         return MainApp.TEXT_MAIN;
        }
    }
}
