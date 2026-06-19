package ui;

import ds.UndoStack;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import manager.SortingUtils;
import model.Patient;
import system.HospitalSystem;

import java.util.List;

// ══════════════════════════════════════════════════════════════════════════════
// UndoScreen
// ══════════════════════════════════════════════════════════════════════════════

/**
 * UndoScreen — Shows the undo stack and allows performing undo.
 */
class UndoScreen extends VBox {

    private final HospitalSystem h = MainApp.hospital;
    private final Label resultLbl  = new Label();

    public UndoScreen() {
        setSpacing(20);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        Label title = MainApp.sectionLabel("↩️  Undo Last Action — Stack (LIFO)");
        Label sub   = MainApp.mutedLabel(
            "Stack uses LIFO order. Most recent action is at the top. Max depth: 10.");

        // ── Stats ─────────────────────────────────────────────────────────────
        HBox stats = new HBox(14);
        int depth = h.getUndoStack().getSize();
        stats.getChildren().addAll(
            MainApp.statCard(String.valueOf(depth), "Actions in Stack", MainApp.ACCENT_BLUE),
            MainApp.statCard("10",                  "Max Stack Depth",  MainApp.TEXT_MUTED)
        );

        // ── Undo button ───────────────────────────────────────────────────────
        Button undoBtn    = MainApp.actionBtn("↩️  Perform Undo (Pop Stack)", MainApp.ACCENT_RED);
        Button refreshBtn = MainApp.actionBtn("⟳  Refresh", MainApp.ACCENT_BLUE);

        undoBtn.setOnAction(e -> {
            if (h.getUndoStack().isEmpty()) {
                resultLbl.setText("⚠  Stack is empty — nothing to undo.");
                resultLbl.setTextFill(Color.web(MainApp.ACCENT_RED));
                return;
            }
            UndoStack.Action top = h.getUndoStack().peek();
            h.undo();
            resultLbl.setText("✅  Undone: " + top.actionType + " for patient " + top.patientId);
            resultLbl.setTextFill(Color.web(MainApp.ACCENT_GREEN));
            getChildren().clear(); build();
        });

        refreshBtn.setOnAction(e -> { getChildren().clear(); build(); });

        resultLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        resultLbl.setWrapText(true);

        HBox btnRow = new HBox(12, undoBtn, refreshBtn);

        // ── Stack visualization ───────────────────────────────────────────────
        VBox stackCard = MainApp.card("📚  Stack Contents (top → bottom)");
        VBox.setVgrow(stackCard, Priority.ALWAYS);

        if (h.getUndoStack().isEmpty()) {
            Label empty = new Label("Stack is empty. Perform some actions first.");
            empty.setFont(Font.font("Arial", 13));
            empty.setTextFill(Color.web(MainApp.TEXT_MUTED));
            stackCard.getChildren().add(empty);
        } else {
            // Header
            HBox hdr = stackRowHeader();
            stackCard.getChildren().add(hdr);
        }

        // Info card
        VBox infoCard = MainApp.card("🧠  How the Stack Works");
        infoCard.setPrefWidth(280);

        String[][] info = {
            {"push()", "Add action on top → O(1)"},
            {"pop()",  "Remove from top (undo) → O(1)"},
            {"peek()", "View top without remove → O(1)"},
            {"Full",   "Oldest action dropped (LIFO)"},
        };

        for (String[] row : info) {
            HBox r = new HBox(8);
            r.setPadding(new Insets(5, 0, 5, 0));
            r.setStyle("-fx-border-color: transparent transparent #334155 transparent; -fx-border-width: 0 0 1 0;");
            Label k = new Label(row[0]);
            k.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
            k.setTextFill(Color.web(MainApp.ACCENT_GREEN));
            k.setMinWidth(70);
            Label v = new Label(row[1]);
            v.setFont(Font.font("Arial", 12));
            v.setTextFill(Color.web(MainApp.TEXT_MUTED));
            r.getChildren().addAll(k, v);
            infoCard.getChildren().add(r);
        }

        Label undoTypes = new Label("\n📋  Undoable Actions");
        undoTypes.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        undoTypes.setTextFill(Color.web(MainApp.ACCENT_BLUE));
        infoCard.getChildren().add(undoTypes);

        String[] types = {"REGISTER → removes patient from BST",
                          "ASSIGN_BED → frees the bed",
                          "DISCHARGE → re-admits patient"};
        for (String t : types) {
            Label lbl = new Label("• " + t);
            lbl.setFont(Font.font("Arial", 12));
            lbl.setTextFill(Color.web(MainApp.TEXT_MUTED));
            lbl.setWrapText(true);
            infoCard.getChildren().add(lbl);
        }

        HBox content = new HBox(16, stackCard, infoCard);
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(title, sub, stats, btnRow, resultLbl, content);
    }

    private HBox stackRowHeader() {
        HBox row = new HBox();
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 6;");
        String[] cols   = {"Position", "Action Type",  "Patient ID", "Details",   "Time"};
        double[] widths = {90,          150,            120,           250,          80};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            lbl.setTextFill(Color.WHITE);
            lbl.setMinWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SortScreen
// ══════════════════════════════════════════════════════════════════════════════

/**
 * SortScreen — Run all 3 sorting algorithms on live patient data and display results.
 */
class SortScreen extends VBox {

    private final HospitalSystem h = MainApp.hospital;

    public SortScreen() {
        setSpacing(20);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        Label title = MainApp.sectionLabel("📈  Sort Patients — 3 Algorithms");
        Label sub   = MainApp.mutedLabel(
            "All sorts operate on a COPY of the array — original data is never modified.");

        List<Patient> all = h.getRegistry().getAllPatients();
        Patient[] arr = all.toArray(new Patient[0]);

        if (arr.length == 0) {
            Label empty = new Label("No patients registered yet. Register some patients first.");
            empty.setFont(Font.font("Arial", 14));
            empty.setTextFill(Color.web(MainApp.TEXT_MUTED));
            getChildren().addAll(title, sub, empty);
            return;
        }

        // ── Algorithm cards ───────────────────────────────────────────────────
        HBox cards = new HBox(14);
        VBox.setVgrow(cards, Priority.ALWAYS);

        Patient[] byName   = SortingUtils.bubbleSortByName(arr);
        Patient[] byAge    = SortingUtils.selectionSortByAge(arr);
        Patient[] byTriage = SortingUtils.insertionSortByTriage(arr);

        cards.getChildren().addAll(
            sortCard("🫧  Bubble Sort", "By Name (A → Z)",
                "Compare adjacent elements.\nSwap if left > right.\nLargest bubbles to end each pass.\n\nBest: O(n)  Worst: O(n²)",
                MainApp.ACCENT_BLUE, byName, "name"),

            sortCard("🎯  Selection Sort", "By Age (youngest → oldest)",
                "Find minimum in unsorted part.\nSwap to front.\nSorted region grows each pass.\n\nAlways: O(n²)",
                MainApp.ACCENT_GREEN, byAge, "age"),

            sortCard("🃏  Insertion Sort", "By Triage (1 → 5)",
                "Pick element, shift larger ones right.\nInsert in correct gap.\nLike sorting playing cards.\n\nBest: O(n)  Worst: O(n²)",
                MainApp.ACCENT_AMBER, byTriage, "triage")
        );

        getChildren().addAll(title, sub, cards);
    }

    private VBox sortCard(String algoTitle, String sortKey,
                          String description, String color,
                          Patient[] sorted, String field) {
        VBox card = MainApp.card("");
        HBox.setHgrow(card, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Title
        Label titleLbl = new Label(algoTitle);
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLbl.setTextFill(Color.web(color));

        Label keyLbl = new Label("Sort key: " + sortKey);
        keyLbl.setFont(Font.font("Arial", 12));
        keyLbl.setTextFill(Color.web(MainApp.TEXT_MUTED));

        // Description
        Label desc = new Label(description);
        desc.setFont(Font.font("Courier New", 11));
        desc.setTextFill(Color.web(MainApp.TEXT_MUTED));
        desc.setWrapText(true);
        desc.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8;"
        );

        Separator sep = new Separator();

        // Sorted result list
        Label resultTitle = new Label("Sorted Result:");
        resultTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        resultTitle.setTextFill(Color.web(MainApp.TEXT_MAIN));

        VBox list = new VBox(3);
        for (int i = 0; i < sorted.length; i++) {
            Patient p  = sorted[i];
            String val = field.equals("name")   ? p.getName()
                       : field.equals("age")    ? String.valueOf(p.getAge())
                       : String.valueOf(p.getTriageScore());

            HBox row = new HBox(8);
            row.setPadding(new Insets(4, 8, 4, 8));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: " + (i % 2 == 0 ? "#1e293b" : "#162032")
                + "; -fx-background-radius: 4;");

            Label rank = new Label((i + 1) + ".");
            rank.setFont(Font.font("Arial", 11));
            rank.setTextFill(Color.web(MainApp.TEXT_MUTED));
            rank.setMinWidth(24);

            Label name = new Label(p.getName());
            name.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            name.setTextFill(Color.web(MainApp.TEXT_MAIN));

            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

            Label valLbl = new Label(val);
            valLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            valLbl.setTextFill(Color.web(color));

            row.getChildren().addAll(rank, name, sp, valLbl);
            list.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        card.getChildren().addAll(titleLbl, keyLbl, desc, sep, resultTitle, scroll);
        return card;
    }
}
