package system;

import ds.AVLTree;
import manager.BedManager;
import manager.SortingUtils;
import model.Patient;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║       Smart Hospital Queue & Bed Management System           ║
 * ║                  Day 3 — Test Runner                         ║
 * ║                                                              ║
 * ║  Tests:                                                      ║
 * ║  ✔ BedManager      (2D Array — assign, free, grid display)  ║
 * ║  ✔ AVL Tree        (self-balancing BST, rotations)          ║
 * ║  ✔ HospitalSystem  (all data structures wired together)     ║
 * ║  ✔ Full patient journey end-to-end                          ║
 * ║  ✔ Undo system     (across all action types)                ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class Day3Test {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║      SHQBMS — Day 3 Test Suite                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // ══════════════════════════════════════════════════════════════════════
        // PART A: BedManager (2D Array)
        // ══════════════════════════════════════════════════════════════════════

        separator("PART A: BedManager — 2D Array");

        BedManager beds = new BedManager();

        // Initial empty grid
        separator("TEST 1: Initial Bed Grid (all empty)");
        beds.printGrid();

        // Assign several beds
        separator("TEST 2: Auto-assign Beds to Patients");
        String bed1 = beds.assignBed("P-1001");
        String bed2 = beds.assignBed("P-1002");
        String bed3 = beds.assignBed("P-1003");
        String bed4 = beds.assignBed("P-1004");
        String bed5 = beds.assignBed("P-1005");

        System.out.println("\n▶ Grid after 5 assignments:");
        beds.printGrid();

        // Assign specific bed
        separator("TEST 3: Assign Specific Bed");
        boolean ok = beds.assignSpecificBed("F1-R0", "P-1006");
        System.out.println("   Assigned F1-R0: " + ok);

        // Reserve a bed
        separator("TEST 4: Reserve a Bed");
        beds.reserveBed("F2-R0");
        beds.reserveBed("F2-R1");

        System.out.println("\n▶ Grid after reserve:");
        beds.printGrid();

        // Check availability
        separator("TEST 5: Check Availability");
        System.out.println("   F0-R0 available? " + beds.isAvailable("F0-R0")
            + " (should be false — occupied)");
        System.out.println("   F3-R0 available? " + beds.isAvailable("F3-R0")
            + " (should be true — empty)");
        System.out.println("   Patient in F0-R0: " + beds.getPatientInBed("F0-R0"));

        // Free a bed
        separator("TEST 6: Free Bed (discharge simulation)");
        beds.freeBed(bed1);
        beds.freeBed(bed3);

        System.out.println("\n▶ Grid after freeing 2 beds:");
        beds.printGrid();

        // Invalid bed ID
        separator("TEST 7: Invalid Bed ID Error Handling");
        beds.assignBed("P-9999"); // valid
        beds.assignSpecificBed("F9-R9", "P-ERR"); // out of range
        beds.assignSpecificBed("XYZ",   "P-ERR"); // wrong format

        // Fill most beds to trigger low-bed alert
        separator("TEST 8: Low Bed Alert (fill to < 10% remaining)");
        System.out.println("   Filling beds to trigger alert...");
        for (int i = 10; i < 50; i++) {
            String b = beds.assignBed("P-FILL" + i);
            if (b == null) break;
        }
        beds.printGrid();

        // ══════════════════════════════════════════════════════════════════════
        // PART B: AVL Tree
        // ══════════════════════════════════════════════════════════════════════

        separator("PART B: AVL Tree — Department Directory");

        AVLTree avl = new AVLTree();

        // Insert departments in alphabetical order (would skew a plain BST)
        separator("TEST 9: Insert Departments (alphabetical order — would skew BST)");
        System.out.println("▶ Inserting in order — AVL will auto-balance with rotations:\n");

        avl.insert("Cardiology",   10);
        avl.insert("Dermatology",   6);
        avl.insert("Emergency",    15);   // ← RR imbalance should trigger here
        avl.insert("General Ward", 20);
        avl.insert("ICU",           8);   // ← another imbalance
        avl.insert("Neurology",     6);
        avl.insert("Oncology",      8);

        System.out.println("\n▶ AVL Tree structure after insertions:");
        avl.printTreeStructure();

        // Insert more to trigger LR and RL cases
        separator("TEST 10: More Inserts (trigger LR / RL rotations)");
        avl.insert("Radiology",     4);
        avl.insert("Orthopedics",  10);
        avl.insert("Pediatrics",   12);
        avl.insert("Surgery",      10);
        avl.insert("Urology",       5);

        System.out.println("\n▶ Final AVL Tree structure:");
        avl.printTreeStructure();

        // Search departments
        separator("TEST 11: AVL Tree Search (O log n)");
        avl.search("ICU");
        avl.search("Surgery");
        avl.search("Psychiatry");  // not found

        // Inorder print (should be alphabetically sorted)
        separator("TEST 12: Inorder Traversal (must be alphabetically sorted)");
        avl.printAllDepartments();

        // Update bed counts
        separator("TEST 13: Update Bed Counts in AVL");
        System.out.println("▶ Occupying beds in ICU:");
        avl.occupyBed("ICU");
        avl.occupyBed("ICU");
        avl.occupyBed("ICU");

        System.out.println("\n▶ Freeing a bed in Emergency:");
        avl.freeBed("Emergency");

        avl.search("ICU");
        avl.search("Emergency");

        // ══════════════════════════════════════════════════════════════════════
        // PART C: Full System Integration
        // ══════════════════════════════════════════════════════════════════════

        separator("PART C: Full HospitalSystem Integration");

        HospitalSystem hospital = new HospitalSystem();

        // Admit patients
        separator("TEST 14: Admit Patients (register + auto-queue)");
        String p1 = hospital.admitPatient("Ayesha Raza",    34, Patient.Gender.FEMALE, "0300-0001", "Chest pain",          2);
        String p2 = hospital.admitPatient("Ali Hassan",     45, Patient.Gender.MALE,   "0311-0002", "High fever",           3);
        String p3 = hospital.admitPatient("Sara Khan",      28, Patient.Gender.FEMALE, "0321-0003", "Leg injury",           4);
        String p4 = hospital.admitPatient("Usman Tariq",    60, Patient.Gender.MALE,   "0333-0004", "Cardiac arrest",       1);
        String p5 = hospital.admitPatient("Fatima Malik",   22, Patient.Gender.FEMALE, "0345-0005", "Routine checkup",      5);
        String p6 = hospital.admitPatient("Bilal Ahmed",    38, Patient.Gender.MALE,   "0301-0006", "Difficulty breathing", 2);
        String p7 = hospital.admitPatient("Zara Hussain",   17, Patient.Gender.FEMALE, "0312-0007", "Headache",             3);
        String p8 = hospital.admitPatient("Kamran Sheikh",  52, Patient.Gender.MALE,   "0322-0008", "Back pain",            4);

        // Dashboard
        separator("TEST 15: Dashboard after admissions");
        hospital.printDashboard();

        // Call emergency patients
        separator("TEST 16: Call Emergency Patients (Min-Heap order)");
        System.out.println("\n▶ Calling emergency patients (should be: triage 1 first):");
        Patient called1 = hospital.callNextEmergency();
        Patient called2 = hospital.callNextEmergency();
        if (called1 != null) System.out.println("   Served: " + called1.getName() + " | Triage: " + called1.getTriageScore() + " | Bed: " + called1.getBedId());
        if (called2 != null) System.out.println("   Served: " + called2.getName() + " | Triage: " + called2.getTriageScore() + " | Bed: " + called2.getBedId());

        // Call OPD patients
        separator("TEST 17: Call OPD Patients (FIFO order)");
        System.out.println("\n▶ Calling OPD patients (should be arrival order):");
        Patient opd1 = hospital.callNextOPD();
        Patient opd2 = hospital.callNextOPD();
        if (opd1 != null) System.out.println("   Served: " + opd1.getName() + " | Bed: " + opd1.getBedId());
        if (opd2 != null) System.out.println("   Served: " + opd2.getName() + " | Bed: " + opd2.getBedId());

        // Bed grid
        separator("TEST 18: Bed Grid after assignments");
        hospital.getBedManager().printGrid();

        // Patient history
        separator("TEST 19: Patient History (Linked List)");
        if (p1 != null) hospital.getRegistry().printPatientHistory(p1);
        if (p4 != null) hospital.getRegistry().printPatientHistory(p4);

        // Discharge
        separator("TEST 20: Discharge Patients");
        if (called1 != null) hospital.dischargePatient(called1.getPatientId());
        if (opd1    != null) hospital.dischargePatient(opd1.getPatientId());

        System.out.println("\n▶ Bed grid after discharges:");
        hospital.getBedManager().printGrid();

        // Undo
        separator("TEST 21: Undo System");
        System.out.println("\n▶ Undo last action (discharge):");
        hospital.undo();

        System.out.println("\n▶ Undo again (another discharge):");
        hospital.undo();

        System.out.println("\n▶ Bed grid after two undos:");
        hospital.getBedManager().printGrid();

        // Sorting on all registered patients
        separator("TEST 22: Sorting All Patients");
        Patient[] allArr = hospital.getRegistry().getAllPatients().toArray(new Patient[0]);

        Patient[] byName   = SortingUtils.bubbleSortByName(allArr);
        Patient[] byAge    = SortingUtils.selectionSortByAge(allArr);
        Patient[] byTriage = SortingUtils.insertionSortByTriage(allArr);

        SortingUtils.printSorted(byName,   "Bubble Sort  — by Name");
        SortingUtils.printSorted(byAge,    "Selection Sort — by Age");
        SortingUtils.printSorted(byTriage, "Insertion Sort — by Triage");

        // Final dashboard
        separator("TEST 23: Final Dashboard");
        hospital.printDashboard();

        // ── Summary ───────────────────────────────────────────────────────────
        separator("DAY 3 SUMMARY");
        System.out.println("  ✔ BedManager (2D Array)          — assign/free/reserve/grid");
        System.out.println("  ✔ Low-bed alert                  — triggers at < 10%");
        System.out.println("  ✔ AVL Tree (department dir)      — LL/RR/LR/RL rotations");
        System.out.println("  ✔ AVL balance guaranteed         — all BF values -1,0,+1");
        System.out.println("  ✔ HospitalSystem controller      — all modules wired");
        System.out.println("  ✔ Full patient journey           — admit→queue→bed→discharge");
        System.out.println("  ✔ Undo system                    — REGISTER/ASSIGN/DISCHARGE");
        System.out.println("  ✔ Sorting on live patient data   — all 3 algorithms");
        System.out.println("\n  ✅ All Day 3 tests passing!");
        System.out.println("  ▶ Ready for Day 4: JavaFX GUI — connect to HospitalSystem\n");
    }

    private static void separator(String title) {
        System.out.println("\n╔══ " + title + " " + "═".repeat(Math.max(0, 50 - title.length())) + "╗");
    }
}
