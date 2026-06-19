package ds;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class PatientHistoryLinkedList {

    // ── Inner Node class ─────────────────────────────────────────────────────

    private static class Node {
        String eventType;       // e.g. "REGISTERED", "BED_ASSIGNED", "DISCHARGED"
        String description;     // Details of the event
        String timestamp;       // Human-readable timestamp
        Node   next;            // Pointer to next node

        Node(String eventType, String description) {
            this.eventType   = eventType;
            this.description = description;
            this.timestamp   = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.next        = null;
        }
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    private Node head;   // First node
    private Node tail;   // Last node (for O(1) append)
    private int  size;
    private final String patientId;

    // ── Constructor ──────────────────────────────────────────────────────────

    public PatientHistoryLinkedList(String patientId) {
        this.patientId = patientId;
        this.head      = null;
        this.tail      = null;
        this.size      = 0;
    }

    // ── Core Operations ──────────────────────────────────────────────────────

    
    public void addEvent(String eventType, String description) {
        Node newNode = new Node(eventType, description);

        if (tail == null) {
           
            head = newNode;
            tail = newNode;
        } else {
            // Append to tail
            tail.next = newNode;
            tail       = newNode;
        }
        size++;
    }

   
    public void removeLastEvent() {
        if (head == null) {
            System.out.println("[WARN] History is already empty. Nothing to remove.");
            return;
        }

        if (head == tail) {
            // Only one node
            head = null;
            tail = null;
            size--;
            return;
        }

        // Traverse to second-to-last node
        Node current = head;
        while (current.next != tail) {
            current = current.next;
        }
        current.next = null;
        tail         = current;
        size--;
    }

   
   
    public String getLatestEvent() {
        if (tail == null) return "No history available.";
        return "[" + tail.timestamp + "] " + tail.eventType + ": " + tail.description;
    }

    
    public boolean isEmpty() {
        return size == 0;
    }

   
    public int getSize() {
        return size;
    }

    // ── Display ──────────────────────────────────────────────────────────────

   
    public void printHistory() {
        System.out.println("\n── Patient History: " + patientId + " ──────────────────");
        if (head == null) {
            System.out.println("  (No events recorded yet)");
            return;
        }

        Node   current = head;
        int    index   = 1;
        while (current != null) {
            System.out.printf("  %2d. [%s] %-18s %s%n",
                index++, current.timestamp, current.eventType + ":", current.description);
            current = current.next;
        }
        System.out.println("──────────────────────────────────────────────");
        System.out.println("  Total events: " + size);
    }

    
    public String getHistoryAsString() {
        if (head == null) return "No history available.";

        StringBuilder sb    = new StringBuilder();
        Node          curr  = head;
        int           index = 1;

        while (curr != null) {
            sb.append(String.format("%2d. [%s] %s: %s%n",
                index++, curr.timestamp, curr.eventType, curr.description));
            curr = curr.next;
        }
        return sb.toString();
    }
}
