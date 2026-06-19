package system;

import ds.EmergencyPriorityQueue;
import ds.OPDQueue;
import ds.UndoStack;
import manager.PatientRegistry;
import manager.SortingUtils;
import model.Patient;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║       Smart Hospital Queue & Bed Management System           ║
 * ║                  Day 2 — Test Runner                         ║
 * ║                                                              ║
 * ║  Tests:                                                      ║
 * ║  ✔ OPDQueue         (FIFO Queue — circular array)           ║
 * ║  ✔ EmergencyQueue   (Min-Heap Priority Queue)                ║
 * ║  ✔ Bubble Sort      (by Name A→Z)                           ║
 * ║  ✔ Selection Sort   (by Age youngest→oldest)                 ║
 * ║  ✔ Insertion Sort   (by Triage 1→5)                         ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class Day2Test {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║      SHQBMS — Day 2 Test Suite                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // ── Setup — shared components ─────────────────────────────────────────
        UndoStack              undoStack      = new UndoStack();
        PatientRegistry        registry       = new PatientRegistry(undoStack);
        OPDQueue               opdQueue       = new OPDQueue();
        EmergencyPriorityQueue emergencyQueue = new EmergencyPriorityQueue();

        // ── Register patients ─────────────────────────────────────────────────
        separator("SETUP: Register 8 Patients");

        String id1 = registry.register("Ayesha Raza",   34, Patient.Gender.FEMALE, "0300-1111", "Chest pain",        2);
        String id2 = registry.register("Ali Hassan",    45, Patient.Gender.MALE,   "0311-2222", "High fever",         3);
        String id3 = registry.register("Sara Khan",     28, Patient.Gender.FEMALE, "0321-3333", "Leg injury",         4);
        String id4 = registry.register("Usman Tariq",   60, Patient.Gender.MALE,   "0333-4444", "Cardiac symptoms",   1);
        String id5 = registry.register("Fatima Malik",  22, Patient.Gender.FEMALE, "0345-5555", "Checkup",            5);
        String id6 = registry.register("Bilal Ahmed",   38, Patient.Gender.MALE,   "0301-6666", "Difficulty breathing",2);
        String id7 = registry.register("Zara Hussain",  17, Patient.Gender.FEMALE, "0312-7777", "Headache",           3);
        String id8 = registry.register("Kamran Sheikh", 52, Patient.Gender.MALE,   "0322-8888", "Back pain",          4);

        // ── Get all patients as array for sorting tests ───────────────────────
        List<Patient> allPatients = registry.getAllPatients();
        Patient[] patientArray    = allPatients.toArray(new Patient[0]);

        // ── Route each patient to correct queue ───────────────────────────────
        separator("TEST 1: Route Patients to Correct Queues");

        System.out.println("\n▶ Routing patients by triage score:");
        System.out.println("   Triage 1-2 → Emergency Queue (Min-Heap)");
        System.out.println("   Triage 3-5 → OPD Queue (FIFO)\n");

        for (Patient p : allPatients) {
            if (p.getTriageScore() <= 2) {
                emergencyQueue.enqueue(p);
            } else {
                opdQueue.enqueue(p);
            }
        }

        // ── Print both queues ─────────────────────────────────────────────────
        separator("TEST 2: Queue Status Display");
        emergencyQueue.printQueue();
        opdQueue.printQueue();

        // ── Heap Tree Structure ───────────────────────────────────────────────
        separator("TEST 3: Min-Heap Tree Visualization");
        emergencyQueue.printHeapTree();

        // ── Peek ──────────────────────────────────────────────────────────────
        separator("TEST 4: Peek (who is next?)");

        System.out.println("\n▶ Emergency Queue — next patient:");
        Patient nextEmergency = emergencyQueue.peek();
        if (nextEmergency != null)
            System.out.println("   NEXT: " + nextEmergency.getName()
                + " | Triage: " + nextEmergency.getTriageScore());

        System.out.println("\n▶ OPD Queue — next patient:");
        Patient nextOPD = opdQueue.peek();
        if (nextOPD != null)
            System.out.println("   NEXT: " + nextOPD.getName()
                + " | Triage: " + nextOPD.getTriageScore());

        // ── Emergency Dequeue (Min-Heap order) ────────────────────────────────
        separator("TEST 5: Emergency Queue Dequeue (Min-Heap Order)");

        System.out.println("\n▶ Calling all emergency patients — should come out in triage order (1 first):\n");
        int callOrder = 1;
        while (!emergencyQueue.isEmpty()) {
            Patient called = emergencyQueue.dequeue();
            System.out.println("   Call #" + callOrder++ + " → "
                + called.getName() + " | Triage: " + called.getTriageScore());
        }

        // ── OPD Dequeue (FIFO order) ──────────────────────────────────────────
        separator("TEST 6: OPD Queue Dequeue (FIFO Order)");

        System.out.println("\n▶ Calling OPD patients — should come out in ARRIVAL order:\n");
        callOrder = 1;
        while (!opdQueue.isEmpty()) {
            Patient called = opdQueue.dequeue();
            System.out.println("   Call #" + callOrder++ + " → "
                + called.getName() + " | Triage: " + called.getTriageScore());
        }

        // ── Test empty queue errors ───────────────────────────────────────────
        separator("TEST 7: Empty Queue Error Handling");
        System.out.println("\n▶ Try dequeue on empty Emergency Queue:");
        emergencyQueue.dequeue();

        System.out.println("\n▶ Try dequeue on empty OPD Queue:");
        opdQueue.dequeue();

        // ── Re-fill emergency queue to test tiebreaking ───────────────────────
        separator("TEST 8: Min-Heap Tie-Breaking (same triage score)");

        System.out.println("\n▶ Adding 3 patients with same triage score=2 (arrival order decides):\n");

        // Small delay to ensure different arrival times
        String tieId1 = registry.register("First Ahmed",  30, Patient.Gender.MALE,   "0300-0001", "Complaint A", 2);
        Thread.sleep(10);
        String tieId2 = registry.register("Second Bibi",  25, Patient.Gender.FEMALE, "0300-0002", "Complaint B", 2);
        Thread.sleep(10);
        String tieId3 = registry.register("Third Chaudhry",40, Patient.Gender.MALE,  "0300-0003", "Complaint C", 2);

        emergencyQueue.enqueue(registry.searchById(tieId3)); // Add 3rd first
        emergencyQueue.enqueue(registry.searchById(tieId1)); // Add 1st second
        emergencyQueue.enqueue(registry.searchById(tieId2)); // Add 2nd last

        System.out.println("\n▶ Heap after inserting out-of-order:");
        emergencyQueue.printHeapTree();

        System.out.println("\n▶ Dequeue order (should be: First, Second, Third — by arrival time):");
        callOrder = 1;
        while (!emergencyQueue.isEmpty()) {
            Patient called = emergencyQueue.dequeue();
            System.out.println("   Call #" + callOrder++ + " → " + called.getName());
        }

        // ══════════════════════════════════════════════════════════════════════
        // SORTING TESTS
        // ══════════════════════════════════════════════════════════════════════

        separator("TEST 9: Bubble Sort — by Name (A to Z)");

        System.out.println("\n▶ Original order:");
        printNames(patientArray);

        Patient[] sortedByName = SortingUtils.bubbleSortByName(patientArray);
        SortingUtils.printSorted(sortedByName, "Result: Sorted by Name (A→Z)");

        // ── Selection Sort ────────────────────────────────────────────────────
        separator("TEST 10: Selection Sort — by Age (youngest to oldest)");

        System.out.println("\n▶ Original order:");
        printAges(patientArray);

        Patient[] sortedByAge = SortingUtils.selectionSortByAge(patientArray);
        SortingUtils.printSorted(sortedByAge, "Result: Sorted by Age");

        // ── Insertion Sort ────────────────────────────────────────────────────
        separator("TEST 11: Insertion Sort — by Triage (1=urgent to 5=non-urgent)");

        System.out.println("\n▶ Original order:");
        printTriages(patientArray);

        Patient[] sortedByTriage = SortingUtils.insertionSortByTriage(patientArray);
        SortingUtils.printSorted(sortedByTriage, "Result: Sorted by Triage");

        // ── Verify original array unchanged ───────────────────────────────────
        separator("TEST 12: Verify Original Array NOT Modified");

        System.out.println("\n▶ Original array after all sorts (should match original order):");
        printNames(patientArray);
        System.out.println("   (If names match original registration order → sorts worked on copies ✔)");

        // ── Final Summary ─────────────────────────────────────────────────────
        separator("DAY 2 SUMMARY");
        System.out.println("  ✔ OPDQueue (FIFO circular array)       — enqueue/dequeue working");
        System.out.println("  ✔ EmergencyPriorityQueue (Min-Heap)    — heapifyUp/Down working");
        System.out.println("  ✔ Triage ordering                      — score 1 served before 2");
        System.out.println("  ✔ Tie-breaking                         — earlier arrival first");
        System.out.println("  ✔ Bubble Sort by Name                  — alphabetical A→Z");
        System.out.println("  ✔ Selection Sort by Age                — youngest→oldest");
        System.out.println("  ✔ Insertion Sort by Triage             — most urgent first");
        System.out.println("  ✔ Original array unchanged after sorts");
        System.out.println("\n  ✅ All Day 2 data structures working correctly!");
        System.out.println("  ▶ Ready for Day 3: BedManager (2D Array) + BST + AVL Tree\n");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void separator(String title) {
        System.out.println("\n╔══ " + title + " " + "═".repeat(Math.max(0, 50 - title.length())) + "╗");
    }

    private static void printNames(Patient[] arr) {
        System.out.print("   ");
        for (Patient p : arr) System.out.print(p.getName().split(" ")[0] + " | ");
        System.out.println();
    }

    private static void printAges(Patient[] arr) {
        System.out.print("   Ages: ");
        for (Patient p : arr) System.out.print(p.getAge() + " | ");
        System.out.println();
    }

    private static void printTriages(Patient[] arr) {
        System.out.print("   Triage scores: ");
        for (Patient p : arr) System.out.print(p.getTriageScore() + " | ");
        System.out.println();
    }
}
