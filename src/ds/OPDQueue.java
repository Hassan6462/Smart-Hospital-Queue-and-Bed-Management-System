package ds;

import model.Patient;


public class OPDQueue {

    // ── Fields ────────────────────────────────────────────────────────────────

    private final Patient[] queue;
    private int             front;      // Index of first patient
    private int             rear;       // Index of last patient
    private int             size;       // Current number of patients
    private final int       MAX_SIZE = 50;

    // ── Constructor ───────────────────────────────────────────────────────────

    public OPDQueue() {
        queue = new Patient[MAX_SIZE];
        front = 0;
        rear  = -1;
        size  = 0;
    }

    
    public void enqueue(Patient patient) {
        if (isFull()) {
            System.out.println("[ERROR] OPD Queue is full. Cannot add: " + patient.getName());
            return;
        }

        if (patient.getTriageScore() < 3) {
            System.out.println("[WARN] Patient " + patient.getName()
                + " has triage score " + patient.getTriageScore()
                + " — should go to Emergency Queue, not OPD.");
        }

        rear = (rear + 1) % MAX_SIZE;
        queue[rear] = patient;
        size++;

        System.out.println("[OPD QUEUE] Enqueued: " + patient.getName()
            + " (Triage: " + patient.getTriageScore()
            + ") | Position: " + size);
    }

    
    public Patient dequeue() {
        if (isEmpty()) {
            System.out.println("[ERROR E-004] OPD Queue is empty. No patients waiting.");
            return null;
        }

        Patient served = queue[front];
        queue[front]   = null;                  // Clear slot
        front          = (front + 1) % MAX_SIZE; // Move front forward
        size--;

        System.out.println("[OPD QUEUE] Dequeued (calling patient): "
            + served.getName() + " | Triage: " + served.getTriageScore());
        return served;
    }

   
    public Patient peek() {
        if (isEmpty()) {
            System.out.println("[INFO] OPD Queue is empty.");
            return null;
        }
        return queue[front];
    }


    public boolean isEmpty() { return size == 0; }
    public boolean isFull()  { return size == MAX_SIZE; }
    public int     getSize() { return size; }

    // ── Display ───────────────────────────────────────────────────────────────

   
    public void printQueue() {
        System.out.println("\n-- OPD Queue (FIFO) ----------------------------------");
        System.out.printf("   front=%d  rear=%d  size=%d%n", front, rear, size);
        System.out.println("   +-----+----------------------+--------+-----------+");
        System.out.printf("   %-5s %-22s %-8s %-11s%n",
            "| Pos", "| Name", "| Triage", "| Status    |");
        System.out.println("   +-----+----------------------+--------+-----------+");

        if (isEmpty()) {
            System.out.println("   |           (Queue is empty)                     |");
        } else {
            for (int i = 0; i < size; i++) {
                int idx = (front + i) % MAX_SIZE;
                Patient p = queue[idx];
                String marker = (i == 0) ? " <- NEXT" : "        ";
                System.out.printf("   | %-3d | %-20s | %-6d | %-9s|%s%n",
                    i + 1, p.getName(), p.getTriageScore(), p.getStatus(), marker);
            }
        }
        System.out.println("   +-----+----------------------+--------+-----------+");
        System.out.println("   Waiting: " + size + " patient(s)");
    }
}
