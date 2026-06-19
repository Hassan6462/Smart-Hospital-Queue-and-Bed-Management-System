package ds;

import model.Patient;


public class EmergencyPriorityQueue {

    // ── Fields ────────────────────────────────────────────────────────────────

    private final Patient[] heap;
    private int             size;
    private final int       MAX_SIZE = 50;

    // ── Constructor ───────────────────────────────────────────────────────────

    public EmergencyPriorityQueue() {
        heap = new Patient[MAX_SIZE];
        size = 0;
    }

    // ── Index Helpers ─────────────────────────────────────────────────────────

    private int parent(int i)     { return (i - 1) / 2; }
    private int leftChild(int i)  { return (2 * i) + 1; }
    private int rightChild(int i) { return (2 * i) + 2; }

   
    private int compare(Patient a, Patient b) {
        if (a.getTriageScore() != b.getTriageScore()) {
            return a.getTriageScore() - b.getTriageScore(); 
        }
       
        return Long.compare(a.getArrivalTime(), b.getArrivalTime());
    }

    // ── Swap Helper ───────────────────────────────────────────────────────────

    private void swap(int i, int j) {
        Patient temp = heap[i];
        heap[i]      = heap[j];
        heap[j]      = temp;
    }

    
    public void enqueue(Patient patient) {
        if (isFull()) {
            System.out.println("[ERROR] Emergency Queue is full!");
            return;
        }

        if (patient.getTriageScore() > 2) {
            System.out.println("[WARN] Patient " + patient.getName()
                + " triage=" + patient.getTriageScore()
                + " should go to OPD Queue, not Emergency.");
        }

        // Step 1: Insert at end
        heap[size] = patient;
        int insertedIndex = size;
        size++;

        System.out.println("[EMERGENCY QUEUE] Inserted: " + patient.getName()
            + " at index " + insertedIndex
            + " (Triage: " + patient.getTriageScore() + ")");

        // Step 2: Bubble UP
        heapifyUp(insertedIndex);
    }

    
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIdx = parent(index);

            if (compare(heap[index], heap[parentIdx]) < 0) {
                // Child is more urgent than parent → swap
                System.out.println("   HeapifyUp: swapping index " + index
                    + " (" + heap[index].getName() + ") with parent " + parentIdx
                    + " (" + heap[parentIdx].getName() + ")");
                swap(index, parentIdx);
                index = parentIdx; // Move up
            } else {
                break; // Heap property satisfied
            }
        }
    }

   
    public Patient dequeue() {
        if (isEmpty()) {
            System.out.println("[ERROR E-004] Emergency Queue is empty. No critical patients.");
            return null;
        }

        // Step 1: Save most urgent patient (root)
        Patient mostUrgent = heap[0];

        // Step 2: Move last element to root
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;

        System.out.println("[EMERGENCY QUEUE] Dequeued (most urgent): "
            + mostUrgent.getName() + " | Triage: " + mostUrgent.getTriageScore());

        // Step 3: Bubble DOWN
        if (size > 0) heapifyDown(0);

        return mostUrgent;
    }

    
    private void heapifyDown(int index) {
        while (true) {
            int left     = leftChild(index);
            int right    = rightChild(index);
            int smallest = index; // Assume current is most urgent

            // Check if left child is more urgent
            if (left < size && compare(heap[left], heap[smallest]) < 0) {
                smallest = left;
            }

            // Check if right child is more urgent than current smallest
            if (right < size && compare(heap[right], heap[smallest]) < 0) {
                smallest = right;
            }

            if (smallest != index) {
                // A child is more urgent → swap and continue down
                System.out.println("   HeapifyDown: swapping index " + index
                    + " (" + heap[index].getName() + ") with child " + smallest
                    + " (" + heap[smallest].getName() + ")");
                swap(index, smallest);
                index = smallest;
            } else {
                break; // Heap property satisfied
            }
        }
    }

    
    public Patient peek() {
        if (isEmpty()) {
            System.out.println("[INFO] Emergency Queue is empty.");
            return null;
        }
        return heap[0];
    }

   

    public boolean isEmpty() { return size == 0; }
    public boolean isFull()  { return size == MAX_SIZE; }
    public int     getSize() { return size; }

    // ── Display ───────────────────────────────────────────────────────────────

    
    public void printQueue() {
        System.out.println("\n-- Emergency Queue (Min-Heap) ------------------------");
        System.out.println("   heap[0] is always the MOST URGENT patient.");
        System.out.println("   +-------+----------------------+--------+");
        System.out.printf("   %-7s %-22s %-8s%n", "| Index", "| Name", "| Triage |");
        System.out.println("   +-------+----------------------+--------+");

        if (isEmpty()) {
            System.out.println("   |          (Queue is empty)               |");
        } else {
            for (int i = 0; i < size; i++) {
                String marker = (i == 0) ? " <- MOST URGENT" : "";
                System.out.printf("   | [%2d]  | %-20s | %-6d |%s%n",
                    i, heap[i].getName(), heap[i].getTriageScore(), marker);
            }
        }
        System.out.println("   +-------+----------------------+--------+");
        System.out.println("   Critical patients waiting: " + size);
    }

    
    public void printHeapTree() {
        System.out.println("\n-- Min-Heap Tree Visualization -----------------------");
        if (isEmpty()) { System.out.println("   (empty)"); return; }

        printHeapNode(0, "", true);
    }

    private void printHeapNode(int index, String prefix, boolean isLeft) {
        if (index >= size) return;
        System.out.println(prefix + (isLeft ? "+-- " : "\\-- ")
            + "[" + index + "] " + heap[index].getName()
            + " (T:" + heap[index].getTriageScore() + ")");
        printHeapNode(leftChild(index),  prefix + (isLeft ? "|   " : "    "), true);
        printHeapNode(rightChild(index), prefix + (isLeft ? "|   " : "    "), false);
    }
}
