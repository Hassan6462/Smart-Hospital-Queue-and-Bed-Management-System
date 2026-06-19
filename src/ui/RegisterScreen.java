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
 * RegisterScreen — Form to register a new patient.
 * On submit: stores in BST registry + routes to correct queue.
 */
public class RegisterScreen extends VBox {

    private final HospitalSystem h = MainApp.hospital;

    // Form fields
    private final TextField nameField     = MainApp.field("Full name");
    private final TextField ageField      = MainApp.field("Age (1-120)");
    private final TextField contactField  = MainApp.field("Phone number");
    private final TextArea  complaintArea = new TextArea();
    private final ComboBox<String> genderBox  = new ComboBox<>();
    private final ComboBox<String> triageBox  = new ComboBox<>();
    private final Label resultLabel = new Label();

    public RegisterScreen() {
        setSpacing(20);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        Label title = MainApp.sectionLabel("👤  Register New Patient");
        Label sub   = MainApp.mutedLabel("Fill in the form below. Triage 1-2 → Emergency Queue | Triage 3-5 → OPD Queue");

        // ── Form Card ─────────────────────────────────────────────────────────
        VBox formCard = MainApp.card("");
        formCard.setMaxWidth(620);

        // Gender
        genderBox.getItems().addAll("MALE", "FEMALE", "OTHER");
        genderBox.setValue("MALE");
        styleCombo(genderBox);

        // Triage
        triageBox.getItems().addAll(
            "1 — Resuscitation (Critical)",
            "2 — Emergent",
            "3 — Urgent",
            "4 — Less Urgent",
            "5 — Non-Urgent"
        );
        triageBox.setValue("3 — Urgent");
        styleCombo(triageBox);

        // Complaint area
        complaintArea.setPromptText("Describe the chief complaint...");
        complaintArea.setPrefRowCount(3);
        complaintArea.setFont(Font.font("Arial", 13));
        complaintArea.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-text-fill: #f1f5f9;" +
            "-fx-prompt-text-fill: #475569;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-control-inner-background: #0f172a;"
        );

        // Grid layout
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);

        addRow(grid, 0, "Full Name *",    nameField);
        addRow(grid, 1, "Age *",          ageField);
        addRow(grid, 2, "Gender",         genderBox);
        addRow(grid, 3, "Contact",        contactField);
        addRow(grid, 4, "Triage Score *", triageBox);

        // Complaint spans full width
        Label cLabel = formLabel("Chief Complaint *");
        grid.add(cLabel,        0, 5);
        grid.add(complaintArea, 1, 5);
        GridPane.setColumnSpan(complaintArea, 2);

        ColumnConstraints col1 = new ColumnConstraints(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // ── Triage reference card ─────────────────────────────────────────────
        VBox triageRef = MainApp.card("📋  Triage Reference");
        triageRef.setMaxWidth(280);
        String[][] ref = {
            {"1", "Resuscitation", "#e94560", "→ Emergency"},
            {"2", "Emergent",      "#f97316", "→ Emergency"},
            {"3", "Urgent",        "#f59e0b", "→ OPD Queue"},
            {"4", "Less Urgent",   "#10b981", "→ OPD Queue"},
            {"5", "Non-Urgent",    "#60a5fa", "→ OPD Queue"},
        };
        for (String[] r : ref) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label score = new Label(r[0]);
            score.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            score.setTextFill(Color.web(r[2]));
            score.setMinWidth(16);
            Label desc = new Label(r[1]);
            desc.setFont(Font.font("Arial", 12));
            desc.setTextFill(Color.web(MainApp.TEXT_MAIN));
            Label queue = new Label(r[3]);
            queue.setFont(Font.font("Arial", 11));
            queue.setTextFill(Color.web(MainApp.TEXT_MUTED));
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            row.getChildren().addAll(score, desc, sp, queue);
            triageRef.getChildren().add(row);
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        Button submitBtn = MainApp.actionBtn("✅  Register Patient", MainApp.ACCENT_GREEN);
        Button clearBtn  = MainApp.actionBtn("🗑  Clear Form",       "#475569");

        submitBtn.setOnAction(e -> registerPatient());
        clearBtn.setOnAction(e  -> clearForm());

        HBox btnRow = new HBox(12, submitBtn, clearBtn);

        // Result label
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        resultLabel.setWrapText(true);

        formCard.getChildren().addAll(grid, btnRow, resultLabel);

        // ── Side by side layout ───────────────────────────────────────────────
        HBox content = new HBox(20, formCard, triageRef);
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(title, sub, content);
    }

    // ── Register Logic ────────────────────────────────────────────────────────

    private void registerPatient() {
        // Validate
        if (nameField.getText().trim().isEmpty()) {
            showError("Name is required.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
            if (age < 1 || age > 120) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Enter a valid age (1-120).");
            return;
        }

        if (complaintArea.getText().trim().isEmpty()) {
            showError("Chief complaint is required.");
            return;
        }

        // Parse triage score (e.g. "3 — Urgent" → 3)
        int triage = Integer.parseInt(triageBox.getValue().substring(0, 1));

        // Parse gender
        Patient.Gender gender = Patient.Gender.valueOf(genderBox.getValue());

        // Register via HospitalSystem
        String id = h.admitPatient(
            nameField.getText().trim(),
            age,
            gender,
            contactField.getText().trim().isEmpty() ? "N/A" : contactField.getText().trim(),
            complaintArea.getText().trim(),
            triage
        );

        if (id != null) {
            String queue = triage <= 2 ? "Emergency Queue 🚨" : "OPD Queue 🏥";
            showSuccess("✅  Patient registered!\n" +
                "ID: " + id + "\n" +
                "Routed to: " + queue);
            clearForm();
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    private void clearForm() {
        nameField.clear();
        ageField.clear();
        contactField.clear();
        complaintArea.clear();
        genderBox.setValue("MALE");
        triageBox.setValue("3 — Urgent");
        resultLabel.setText("");
    }

    private void showSuccess(String msg) {
        resultLabel.setText(msg);
        resultLabel.setTextFill(Color.web(MainApp.ACCENT_GREEN));
    }

    private void showError(String msg) {
        resultLabel.setText("⚠  " + msg);
        resultLabel.setTextFill(Color.web(MainApp.ACCENT_RED));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        grid.add(formLabel(labelText), 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private Label formLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", 13));
        lbl.setTextFill(Color.web(MainApp.TEXT_MUTED));
        return lbl;
    }

    private void styleCombo(ComboBox<?> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-text-fill: #f1f5f9;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
    }
}
