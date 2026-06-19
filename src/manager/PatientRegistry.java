package manager;

import ds.PatientHistoryLinkedList;
import ds.UndoStack;
import model.Patient;

import java.util.ArrayList;
import java.util.List;


public class PatientRegistry {

    // ── BST Node ─────────────────────────────────────────────────────────────

    private static class Node {
        Patient                  patient;   // The actual patient data
        PatientHistoryLinkedList history;   // This patient's event log
        Node                     left;      // Left child  (smaller ID)
        Node                     right;     // Right child (larger ID)

        Node(Patient patient) {
            this.patient = patient;
            this.history = new PatientHistoryLinkedList(patient.getPatientId());
            this.left    = null;
            this.right   = null;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private Node      root;        // Root of the BST
    private int       size;        // Total number of patients
    private int       idCounter;   // For generating unique IDs
    private UndoStack undoStack;

    // ── Constructor ───────────────────────────────────────────────────────────

    public PatientRegistry(UndoStack undoStack) {
        this.root       = null;
        this.size       = 0;
        this.idCounter  = 1000;
        this.undoStack  = undoStack;
    }

    // ── ID Generation ─────────────────────────────────────────────────────────

    
    private String generateId() {
        return "P-" + (++idCounter);
    }

    // ── BST INSERT ────────────────────────────────────────────────────────────

   
    public String register(String name, int age, Patient.Gender gender,
                           String contact, String complaint, int triageScore) {

        String  id      = generateId();
        Patient patient = new Patient(id, name, age, gender, contact, complaint, triageScore);

        // Insert into BST
        root = insertNode(root, patient);
        size++;

        // Add first history event
        Node node = searchNode(root, id);
        if (node != null) {
            node.history.addEvent("REGISTERED",
                "Name: " + name + " | Triage: " + triageScore + " | Complaint: " + complaint);
        }

        // Push to undo stack
        undoStack.push("REGISTER", id, "name=" + name + ",triage=" + triageScore);

        System.out.println("\n[REGISTRY] Patient registered successfully!");
        System.out.println(patient);
        return id;
    }

   
    private Node insertNode(Node current, Patient patient) {
        // Base case: empty spot found — place node here
        if (current == null) {
            return new Node(patient);
        }

        int cmp = patient.getPatientId().compareTo(current.patient.getPatientId());

        if (cmp < 0) {
            // New ID is smaller → go LEFT
            current.left = insertNode(current.left, patient);
        } else if (cmp > 0) {
            // New ID is larger → go RIGHT
            current.right = insertNode(current.right, patient);
        } else {
            // Duplicate ID — should not happen with our generator
            System.out.println("[WARN] Duplicate ID detected: " + patient.getPatientId());
        }

        return current;
    }

    // ── BST SEARCH ────────────────────────────────────────────────────────────

   
    public Patient searchById(String patientId) {
        Node result = searchNode(root, patientId);
        if (result == null) {
            System.out.println("[ERROR E-003] Patient ID not found: " + patientId);
            return null;
        }
        return result.patient;
    }

    
    private Node searchNode(Node current, String patientId) {
        // Base case: not found
        if (current == null) return null;

        int cmp = patientId.compareTo(current.patient.getPatientId());

        if (cmp == 0) {
            // Found it
            return current;
        } else if (cmp < 0) {
            // Target is smaller → search LEFT
            return searchNode(current.left, patientId);
        } else {
            // Target is larger → search RIGHT
            return searchNode(current.right, patientId);
        }
    }

   
    public List<Patient> searchByName(String name) {
        List<Patient> results = new ArrayList<>();
        searchByNameHelper(root, name.toLowerCase(), results);

        if (results.isEmpty()) {
            System.out.println("[INFO] No patients found with name: " + name);
        }
        return results;
    }

   
    private void searchByNameHelper(Node current, String lowerName, List<Patient> results) {
        if (current == null) return;
        searchByNameHelper(current.left,  lowerName, results);
        if (current.patient.getName().toLowerCase().contains(lowerName)) {
            results.add(current.patient);
        }
        searchByNameHelper(current.right, lowerName, results);
    }

    // ── BST DELETE ────────────────────────────────────────────────────────────

    
    public void unregister(String patientId) {
        if (searchNode(root, patientId) == null) {
            System.out.println("[ERROR] Cannot unregister — not found: " + patientId);
            return;
        }
        root = deleteNode(root, patientId);
        size--;
        System.out.println("[UNDO] Patient " + patientId + " removed from BST registry.");
    }

    
    private Node deleteNode(Node current, String patientId) {
        if (current == null) return null;

        int cmp = patientId.compareTo(current.patient.getPatientId());

        if (cmp < 0) {
            current.left  = deleteNode(current.left,  patientId);
        } else if (cmp > 0) {
            current.right = deleteNode(current.right, patientId);
        } else {
            // Found the node to delete

            // Case 1: No children (leaf node)
            if (current.left == null && current.right == null) {
                return null;
            }

            // Case 2a: Only right child
            if (current.left == null) {
                return current.right;
            }

            // Case 2b: Only left child
            if (current.right == null) {
                return current.left;
            }

            // Case 3: Two children
            // Find inorder successor = smallest node in RIGHT subtree
            Node successor = findMin(current.right);
            // Copy successor's data into current node
            current.patient = successor.patient;
            current.history = successor.history;
            // Delete the successor from right subtree
            current.right = deleteNode(current.right, successor.patient.getPatientId());
        }

        return current;
    }

    /** Find the minimum node (leftmost) in a subtree. */
    private Node findMin(Node current) {
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    // ── UPDATE OPERATIONS ─────────────────────────────────────────────────────

    /** Assign a bed to a patient. */
    public void assignBed(String patientId, String bedId) {
        Node node = searchNode(root, patientId);
        if (node == null) { searchById(patientId); return; }

        node.patient.setBedId(bedId);
        node.patient.setStatus(Patient.Status.ADMITTED);
        node.history.addEvent("BED_ASSIGNED", "Bed: " + bedId);
        undoStack.push("ASSIGN_BED", patientId, "bedId=" + bedId);

        System.out.println("[REGISTRY] Bed " + bedId + " assigned to " + patientId);
    }

    /** Discharge a patient. */
    public void discharge(String patientId) {
        Node node = searchNode(root, patientId);
        if (node == null) { searchById(patientId); return; }

        String oldBed = node.patient.getBedId();
        node.patient.setStatus(Patient.Status.DISCHARGED);
        node.patient.setBedId(null);
        node.history.addEvent("DISCHARGED", "Released from bed: " + (oldBed != null ? oldBed : "-"));
        undoStack.push("DISCHARGE", patientId, "previousBed=" + oldBed);

        System.out.println("[REGISTRY] Patient " + patientId + " discharged.");
    }

    /** Add an event to a patient's history linked list. */
    public void addHistoryEvent(String patientId, String eventType, String description) {
        Node node = searchNode(root, patientId);
        if (node != null) node.history.addEvent(eventType, description);
    }

    // ── DISPLAY ───────────────────────────────────────────────────────────────

    /** Print patient history (linked list traversal). */
    public void printPatientHistory(String patientId) {
        Node node = searchNode(root, patientId);
        if (node == null) {
            System.out.println("[ERROR] No history found for: " + patientId);
            return;
        }
        node.history.printHistory();
    }

    /** Get history string for GUI. */
    public String getPatientHistoryString(String patientId) {
        Node node = searchNode(root, patientId);
        if (node == null) return "No history found.";
        return node.history.getHistoryAsString();
    }

    /**
     * Print ALL patients in SORTED order by Patient ID.
     * Uses Inorder Traversal: LEFT → ROOT → RIGHT  →  O(n)
     * This gives ascending alphabetical order of IDs automatically.
     */
    public void printAllPatients() {
        System.out.println("\n-- All Registered Patients (BST Inorder = Sorted by ID) --");
        System.out.printf("%-10s %-20s %-5s %-8s %-12s %-10s%n",
            "ID", "Name", "Age", "Triage", "Status", "Bed");
        System.out.println("-".repeat(70));

        if (root == null) {
            System.out.println("  (No patients registered)");
        } else {
            inorderPrint(root);
        }

        System.out.println("-".repeat(70));
        System.out.println("  Total: " + size + " patient(s)");
    }

    /** Inorder traversal: visits LEFT → current → RIGHT (sorted order). */
    private void inorderPrint(Node current) {
        if (current == null) return;
        inorderPrint(current.left);   // Visit left subtree first
        System.out.printf("%-10s %-20s %-5d %-8d %-12s %-10s%n",
            current.patient.getPatientId(),
            current.patient.getName(),
            current.patient.getAge(),
            current.patient.getTriageScore(),
            current.patient.getStatus(),
            current.patient.getBedId() != null ? current.patient.getBedId() : "-");
        inorderPrint(current.right);  // Visit right subtree after
    }

  
    public void printBSTStructure() {
        System.out.println("\n-- BST Structure (Patient ID tree) --");
        if (root == null) {
            System.out.println("  (Empty tree)");
            return;
        }
        printTree(root, "", true);
        System.out.println("  Total nodes: " + size);
    }

    private void printTree(Node node, String prefix, boolean isLeft) {
        if (node == null) return;
        System.out.println(prefix + (isLeft ? "+-- " : "\\-- ") + node.patient.getPatientId()
            + " (" + node.patient.getName() + ")");
        printTree(node.left,  prefix + (isLeft ? "|   " : "    "), true);
        printTree(node.right, prefix + (isLeft ? "|   " : "    "), false);
    }

    // ── UTILITY ───────────────────────────────────────────────────────────────

    public int getTotalCount()    { return size; }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        collectAll(root, list);
        return list;
    }

    private void collectAll(Node current, List<Patient> list) {
        if (current == null) return;
        collectAll(current.left, list);
        list.add(current.patient);
        collectAll(current.right, list);
    }

    public int getAdmittedCount() {
        int[] count = {0};
        getAllPatients().forEach(p -> {
            if (p.getStatus() == Patient.Status.ADMITTED) count[0]++;
        });
        return count[0];
    }
}
