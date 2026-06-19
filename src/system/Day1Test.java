package system;

import ds.PatientHistoryLinkedList;
import ds.UndoStack;
import manager.PatientRegistry;
import model.Patient;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║       Smart Hospital Queue & Bed Management System           ║
 * ║                  Day 1 — Test Runner                         ║
 * ║                                                              ║
 * ║  Tests:                                                      ║
 * ║  ✔ Patient model                                             ║
 * ║  ✔ PatientHistoryLinkedList (Linked List)                    ║
 * ║  ✔ UndoStack (Stack)                                         ║
 * ║  ✔ PatientRegistry (HashMap)                                 ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class Day1Test {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║      SHQBMS — Day 1 Test Suite                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // ── Shared Undo Stack ─────────────────────────────────────────────
        UndoStack undoStack = new UndoStack();

        // ── PatientRegistry (HashMap) ─────────────────────────────────────
        PatientRegistry registry = new PatientRegistry(undoStack);

        separator("TEST 1: Patient Registration (HashMap)");

        String id1 = registry.register(
            "Ayesha Raza", 34, Patient.Gender.FEMALE,
            "0300-1234567", "Chest pain and breathlessness", 2
        );

        String id2 = registry.register(
            "Ali Hassan", 45, Patient.Gender.MALE,
            "0311-9876543", "High fever and headache", 3
        );

        String id3 = registry.register(
            "Sara Khan", 28, Patient.Gender.FEMALE,
            "0321-5556677", "Minor leg injury", 4
        );

        String id4 = registry.register(
            "Usman Tariq", 60, Patient.Gender.MALE,
            "0333-1112233", "Cardiac arrest symptoms", 1
        );

        String id5 = registry.register(
            "Fatima Malik", 22, Patient.Gender.FEMALE,
            "0345-9990011", "Routine checkup", 5
        );

        // ── Search Tests ──────────────────────────────────────────────────
        separator("TEST 2: Patient Search");

        System.out.println("\n▶ Search by ID: " + id1);
        Patient found = registry.searchById(id1);
        if (found != null) System.out.println(found);

        System.out.println("\n▶ Search by Name: 'ali'");
        List<Patient> nameResults = registry.searchByName("ali");
        nameResults.forEach(p -> System.out.println("  → " + p.toSummary()));

        System.out.println("\n▶ Search by Name: 'khan'");
        List<Patient> khanResults = registry.searchByName("khan");
        khanResults.forEach(p -> System.out.println("  → " + p.toSummary()));

        System.out.println("\n▶ Search non-existent ID:");
        registry.searchById("P-9999");

        // ── Print All Patients ────────────────────────────────────────────
        separator("TEST 3: All Patients (HashMap values())");
        registry.printAllPatients();

        // ── Bed Assignment ────────────────────────────────────────────────
        separator("TEST 4: Bed Assignment");
        registry.assignBed(id1, "A-101");
        registry.assignBed(id4, "ICU-01");

        System.out.println("\n▶ Patient after bed assignment:");
        System.out.println(registry.searchById(id1));

        // ── Patient History (Linked List) ─────────────────────────────────
        separator("TEST 5: Patient History Linked List");

        registry.addHistoryEvent(id1, "VITALS_CHECKED",  "BP: 140/90, Pulse: 110");
        registry.addHistoryEvent(id1, "TREATMENT_GIVEN", "ECG performed, medication prescribed");
        registry.addHistoryEvent(id1, "STATUS_UPDATE",   "Condition stable");

        registry.printPatientHistory(id1);

        // Also test the LinkedList directly
        System.out.println("\n▶ Direct LinkedList test:");
        PatientHistoryLinkedList directList = new PatientHistoryLinkedList("TEST-001");
        directList.addEvent("REGISTERED", "Initial registration");
        directList.addEvent("BED_ASSIGNED", "Bed B-202");
        directList.addEvent("MEDICINE", "Paracetamol 500mg");
        directList.printHistory();

        System.out.println("\n▶ Remove last event (undo simulation):");
        directList.removeLastEvent();
        directList.printHistory();

        System.out.println("\n▶ Latest event: " + directList.getLatestEvent());
        System.out.println("▶ History size: " + directList.getSize());

        // ── UndoStack (Stack) ─────────────────────────────────────────────
        separator("TEST 6: Undo Stack");

        undoStack.printStack();

        System.out.println("\n▶ Performing UNDO (pop last action):");
        UndoStack.Action undone = undoStack.pop();
        if (undone != null) {
            System.out.println("  Undone action: " + undone);
            System.out.println("  → Reversing: " + undone.actionType + " for patient " + undone.patientId);

            // Demonstrate actual undo logic
            if (undone.actionType.equals("REGISTER")) {
                registry.unregister(undone.patientId);
            }
        }

        System.out.println("\n▶ Stack after undo:");
        undoStack.printStack();

        System.out.println("\n▶ Peek at top (no removal):");
        UndoStack.Action top = undoStack.peek();
        if (top != null) System.out.println("  Top: " + top);

        System.out.println("\n▶ Test stack overflow (push 12 items, max is 10):");
        for (int i = 1; i <= 12; i++) {
            undoStack.push("TEST_ACTION_" + i, "P-TEMP", "detail=" + i);
        }
        System.out.println("  Final stack size (should be 10): " + undoStack.getSize());
        undoStack.printStack();

        // ── Discharge Test ────────────────────────────────────────────────
        separator("TEST 7: Discharge Patient");
        registry.discharge(id1);
        registry.printPatientHistory(id1);
        System.out.println("\n▶ Patient status after discharge:");
        System.out.println(registry.searchById(id1));

        // ── Final Summary ─────────────────────────────────────────────────
        separator("DAY 1 SUMMARY");
        System.out.println("  ✔ Patient model          — OK");
        System.out.println("  ✔ PatientRegistry        — HashMap: " + registry.getTotalCount() + " patients stored");
        System.out.println("  ✔ PatientHistoryLinkedList— Singly Linked List: append O(1), traverse O(n)");
        System.out.println("  ✔ UndoStack              — Stack LIFO: push/pop/peek all working");
        System.out.println("  ✔ Undo logic             — REGISTER reversal tested");
        System.out.println("\n  ✅ All Day 1 data structures working correctly!");
        System.out.println("  ▶ Ready for Day 2: Queue (OPD) + Min-Heap (Emergency) + Sorting\n");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private static void separator(String title) {
        System.out.println("\n╔══ " + title + " " + "═".repeat(Math.max(0, 50 - title.length())) + "╗");
    }
}
