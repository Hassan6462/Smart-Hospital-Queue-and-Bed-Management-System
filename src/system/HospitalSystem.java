package system;

import ds.*;
import manager.*;
import model.Patient;

import java.util.List;

/**
 * HospitalSystem — Central Controller
 *
 * Wires ALL data structures together into one working system.
 * This is the class your JavaFX GUI will call in Day 4.
 *
 * Data Structures used:
 *   BST              → PatientRegistry  (search by ID)
 *   LinkedList       → Patient history  (event log)
 *   Stack (Array)    → UndoStack        (undo last action)
 *   Queue (Array)    → OPDQueue         (FIFO triage 3-5)
 *   Min-Heap (Array) → EmergencyQueue   (priority triage 1-2)
 *   2D Array         → BedManager       (bed grid)
 *   AVL Tree         → DeptDirectory    (department lookup)
 *   Sorting          → SortingUtils     (bubble/selection/insertion)
 */
public class HospitalSystem {

    // ── All data structures ───────────────────────────────────────────────────

    private final UndoStack              undoStack;
    private final PatientRegistry        registry;
    private final OPDQueue               opdQueue;
    private final EmergencyPriorityQueue emergencyQueue;
    private final BedManager             bedManager;
    private final AVLTree                deptTree;

    // ── Constructor ───────────────────────────────────────────────────────────

    public HospitalSystem() {
        undoStack      = new UndoStack();
        registry       = new PatientRegistry(undoStack);
        opdQueue       = new OPDQueue();
        emergencyQueue = new EmergencyPriorityQueue();
        bedManager     = new BedManager();
        deptTree       = new AVLTree();

        // Pre-load hospital departments into AVL Tree
        initDepartments();
    }

    /** Insert all hospital departments into the AVL Tree. */
    private void initDepartments() {
        System.out.println("[SYSTEM] Initializing department directory (AVL Tree)...");
        deptTree.insert("Cardiology",       10);
        deptTree.insert("Emergency",        15);
        deptTree.insert("General Ward",     20);
        deptTree.insert("ICU",               8);
        deptTree.insert("Neurology",         6);
        deptTree.insert("Oncology",          8);
        deptTree.insert("Orthopedics",      10);
        deptTree.insert("Pediatrics",       12);
        deptTree.insert("Surgery",          10);
        deptTree.insert("Radiology",         4);
        System.out.println("[SYSTEM] Department directory ready.\n");
    }

    // ── Patient Registration ──────────────────────────────────────────────────

    /**
     * Register patient → store in BST → route to correct queue.
     * Full pipeline in one call.
     */
    public String admitPatient(String name, int age, Patient.Gender gender,
                               String contact, String complaint, int triageScore) {

        // 1. Register in BST registry
        String patientId = registry.register(name, age, gender, contact, complaint, triageScore);

        // 2. Get the patient object back
        Patient patient = registry.searchById(patientId);
        if (patient == null) return null;

        // 3. Route to correct queue based on triage
        if (triageScore <= 2) {
            emergencyQueue.enqueue(patient);
            System.out.println("[SYSTEM] " + name + " → Emergency Queue (critical)");
        } else {
            opdQueue.enqueue(patient);
            System.out.println("[SYSTEM] " + name + " → OPD Queue");
        }

        return patientId;
    }

    // ── Call Next Patient ─────────────────────────────────────────────────────

    /**
     * Call next EMERGENCY patient → assign bed automatically.
     */
    public Patient callNextEmergency() {
        Patient patient = emergencyQueue.dequeue();
        if (patient == null) return null;

        String bedId = bedManager.assignBed(patient.getPatientId());
        if (bedId != null) {
            registry.assignBed(patient.getPatientId(), bedId);
        }
        return patient;
    }

    /**
     * Call next OPD patient → assign bed automatically.
     */
    public Patient callNextOPD() {
        Patient patient = opdQueue.dequeue();
        if (patient == null) return null;

        String bedId = bedManager.assignBed(patient.getPatientId());
        if (bedId != null) {
            registry.assignBed(patient.getPatientId(), bedId);
        }
        return patient;
    }

    // ── Discharge ─────────────────────────────────────────────────────────────

    /**
     * Discharge patient → free their bed → update BST record.
     */
    public void dischargePatient(String patientId) {
        Patient patient = registry.searchById(patientId);
        if (patient == null) return;

        String bedId = patient.getBedId();
        if (bedId != null) {
            bedManager.freeBed(bedId);
        }
        registry.discharge(patientId);
    }

    // ── Undo ──────────────────────────────────────────────────────────────────

    /**
     * Undo the last action performed.
     */
    public void undo() {
        UndoStack.Action action = undoStack.pop();
        if (action == null) return;

        System.out.println("[SYSTEM] Undoing: " + action.actionType
            + " for patient " + action.patientId);

        switch (action.actionType) {
            case "REGISTER":
                registry.unregister(action.patientId);
                break;

            case "ASSIGN_BED":
                // Parse bedId from details: "bedId=F0-R3"
                String bedId = action.details.replace("bedId=", "");
                bedManager.freeBed(bedId);
                Patient p = registry.searchById(action.patientId);
                if (p != null) {
                    p.setBedId(null);
                    p.setStatus(Patient.Status.WAITING);
                }
                System.out.println("[UNDO] Bed " + bedId + " freed.");
                break;

            case "DISCHARGE":
                // Re-admit patient
                Patient readmit = registry.searchById(action.patientId);
                if (readmit != null) {
                    readmit.setStatus(Patient.Status.ADMITTED);
                    String prevBed = action.details.replace("previousBed=", "");
                    if (!prevBed.equals("null")) {
                        bedManager.assignSpecificBed(prevBed, action.patientId);
                        readmit.setBedId(prevBed);
                    }
                    System.out.println("[UNDO] Patient " + action.patientId + " re-admitted.");
                }
                break;

            default:
                System.out.println("[WARN] Unknown action type to undo: " + action.actionType);
        }
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    public void printDashboard() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║            SHQBMS — LIVE DASHBOARD               ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf( "║  Total Patients Registered : %-19d ║%n", registry.getTotalCount());
        System.out.printf( "║  Currently Admitted        : %-19d ║%n", registry.getAdmittedCount());
        System.out.printf( "║  Emergency Queue (waiting) : %-19d ║%n", emergencyQueue.getSize());
        System.out.printf( "║  OPD Queue (waiting)       : %-19d ║%n", opdQueue.getSize());
        System.out.printf( "║  Beds Occupied             : %-19d ║%n", bedManager.getOccupiedCount());
        System.out.printf( "║  Beds Available            : %-19d ║%n", bedManager.getEmptyCount());
        System.out.printf( "║  Bed Occupancy %%           : %-18s  ║%n",
            String.format("%.1f%%", bedManager.getOccupancyPercent()));
        System.out.printf( "║  Undo Stack depth          : %-19d ║%n", undoStack.getSize());
        System.out.printf( "║  Departments in AVL Tree   : %-19d ║%n", deptTree.getSize());
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // ── Getters (for JavaFX GUI in Day 4) ────────────────────────────────────

    public PatientRegistry        getRegistry()       { return registry; }
    public OPDQueue               getOpdQueue()       { return opdQueue; }
    public EmergencyPriorityQueue getEmergencyQueue() { return emergencyQueue; }
    public BedManager             getBedManager()     { return bedManager; }
    public AVLTree                getDeptTree()       { return deptTree; }
    public UndoStack              getUndoStack()      { return undoStack; }
}
