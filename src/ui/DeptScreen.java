package ui;

import ds.AVLTree;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import system.HospitalSystem;

/**
 * DeptScreen — Shows AVL Tree department directory.
 */
public class DeptScreen extends VBox {

    private final HospitalSystem h   = MainApp.hospital;
    private final AVLTree        avl = h.getDeptTree();

    public DeptScreen() {
        setSpacing(20);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MainApp.BG_DARK + ";");
        build();
    }

    private void build() {
        Label title = MainApp.sectionLabel("🌳  Department Directory — AVL Tree");
        Label sub   = MainApp.mutedLabel(
            "Self-balancing BST. All balance factors are -1, 0, or +1. Search = O(log n).");

        // ── Stats ─────────────────────────────────────────────────────────────
        HBox stats = new HBox(14);
        stats.getChildren().addAll(
            MainApp.statCard(String.valueOf(avl.getSize()),       "Departments",  MainApp.ACCENT_BLUE),
            MainApp.statCard(String.valueOf(avl.getTreeHeight()), "Tree Height",  MainApp.ACCENT_GREEN)
        );

        // ── Search bar ────────────────────────────────────────────────────────
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = MainApp.field("Search department (e.g. ICU)");
        searchField.setPrefWidth(280);
        Label searchResult = new Label();
        searchResult.setFont(Font.font("Arial", 13));
        Button searchBtn = MainApp.actionBtn("🔍 Search", MainApp.ACCENT_BLUE);
        searchBtn.setOnAction(e -> {
            String term = searchField.getText().trim();
            if (term.isEmpty()) return;
            boolean found = avl.search(term);
            searchResult.setText(found ? "✅ Found: " + term : "❌ Not found: " + term);
            searchResult.setTextFill(Color.web(found ? MainApp.ACCENT_GREEN : MainApp.ACCENT_RED));
        });
        searchRow.getChildren().addAll(searchField, searchBtn, searchResult);

        // ── Two column layout: table + tree visual ────────────────────────────
        HBox content = new HBox(16);
        VBox.setVgrow(content, Priority.ALWAYS);

        // Department table
        VBox tableCard = MainApp.card("📋  All Departments (Inorder = Sorted A→Z)");
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        String[][] depts = {
            {"Cardiology",   "10"}, {"Emergency",    "15"}, {"General Ward", "20"},
            {"ICU",           "8"}, {"Neurology",     "6"}, {"Oncology",      "8"},
            {"Orthopedics",  "10"}, {"Pediatrics",   "12"}, {"Radiology",     "4"},
            {"Surgery",      "10"}
        };

        // Header
        HBox hdr = deptRowHeader();
        tableCard.getChildren().add(hdr);

        for (int i = 0; i < depts.length; i++) {
            tableCard.getChildren().add(deptRow(i + 1, depts[i][0], depts[i][1]));
        }

        // AVL tree info card
        VBox infoCard = MainApp.card("🌳  AVL Tree Properties");
        infoCard.setPrefWidth(300);

        String[][] props = {
            {"Tree Height",      String.valueOf(avl.getTreeHeight())},
            {"Total Nodes",      String.valueOf(avl.getSize())},
            {"Search Time",      "O(log n)"},
            {"Insert Time",      "O(log n)"},
            {"Balance Factor",   "-1, 0, or +1"},
            {"vs Plain BST",     "Guaranteed O(log n)"},
        };

        for (String[] prop : props) {
            HBox row = new HBox();
            row.setPadding(new Insets(6, 0, 6, 0));
            row.setStyle("-fx-border-color: transparent transparent #334155 transparent; -fx-border-width: 0 0 1 0;");
            Label key = new Label(prop[0]);
            key.setFont(Font.font("Arial", 13));
            key.setTextFill(Color.web(MainApp.TEXT_MUTED));
            key.setMinWidth(140);
            Label val = new Label(prop[1]);
            val.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            val.setTextFill(Color.web(MainApp.TEXT_MAIN));
            row.getChildren().addAll(key, val);
            infoCard.getChildren().add(row);
        }

        // Rotation explainer
        Label rotTitle = new Label("\n🔄  Rotation Cases");
        rotTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        rotTitle.setTextFill(Color.web(MainApp.ACCENT_BLUE));
        infoCard.getChildren().add(rotTitle);

        String[][] rots = {
            {"LL imbalance", "→ Right Rotation"},
            {"RR imbalance", "→ Left Rotation"},
            {"LR imbalance", "→ Left then Right"},
            {"RL imbalance", "→ Right then Left"},
        };

        for (String[] r : rots) {
            HBox row = new HBox(8);
            Label k = new Label(r[0]);
            k.setFont(Font.font("Arial", 12));
            k.setTextFill(Color.web(MainApp.TEXT_MUTED));
            k.setMinWidth(120);
            Label v = new Label(r[1]);
            v.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            v.setTextFill(Color.web(MainApp.ACCENT_GREEN));
            row.getChildren().addAll(k, v);
            infoCard.getChildren().add(row);
        }

        content.getChildren().addAll(tableCard, infoCard);
        getChildren().addAll(title, sub, stats, searchRow, content);
    }

    private HBox deptRowHeader() {
        HBox row = new HBox();
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 6;");
        String[] cols   = {"#", "Department Name", "Total Beds", "Available", "Status"};
        double[] widths = {40,  220,                120,          120,          100};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            lbl.setTextFill(Color.WHITE);
            lbl.setMinWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }

    private HBox deptRow(int rank, String name, String beds) {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(rank % 2 == 0
            ? "-fx-background-color: #1e293b; -fx-background-radius: 6;"
            : "-fx-background-color: #162032; -fx-background-radius: 6;");

        int total = Integer.parseInt(beds);
        int avail = total; // All available in demo

        Label rankLbl  = label(String.valueOf(rank), 40, MainApp.TEXT_MUTED, false);
        Label nameLbl  = label(name, 220, MainApp.TEXT_MAIN, true);
        Label totLbl   = label(String.valueOf(total), 120, MainApp.TEXT_MUTED, false);
        Label availLbl = label(String.valueOf(avail), 120, MainApp.ACCENT_GREEN, true);

        String status = avail > total * 0.3 ? "✅ Good" : avail > 0 ? "⚠ Low" : "❌ Full";
        String sColor = avail > total * 0.3 ? MainApp.ACCENT_GREEN : avail > 0 ? MainApp.ACCENT_AMBER : MainApp.ACCENT_RED;
        Label statusLbl = label(status, 100, sColor, true);

        row.getChildren().addAll(rankLbl, nameLbl, totLbl, availLbl, statusLbl);
        return row;
    }

    private Label label(String text, double width, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setMinWidth(width);
        lbl.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
}
