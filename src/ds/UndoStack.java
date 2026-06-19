package ds;


public class UndoStack {

    // ── Inner class: Action ──────────────────────────────────────────────────

    public static class Action {
        public final String actionType;   // "REGISTER", "ASSIGN_BED", "DISCHARGE"
        public final String patientId;
        public final String details;      // Info needed to reverse the action
        public final String timestamp;

        public Action(String actionType, String patientId, String details) {
            this.actionType = actionType;
            this.patientId  = patientId;
            this.details    = details;
            this.timestamp  = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        @Override
        public String toString() {
            return String.format("[%s] %-15s | Patient: %-10s | Info: %s",
                timestamp, actionType, patientId, details);
        }
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    private final Action[] stack;      // Fixed-size array
    private int            top;        // Index of current top element (-1 = empty)
    private final int      MAX_SIZE = 10;

    // ── Constructor ──────────────────────────────────────────────────────────

    public UndoStack() {
        stack = new Action[MAX_SIZE];  // Allocate array of size 10
        top   = -1;                    // Empty stack
    }

    
    public void push(String actionType, String patientId, String details) {
        Action newAction = new Action(actionType, patientId, details);

        if (isFull()) {
           
            for (int i = 0; i < MAX_SIZE - 1; i++) {
                stack[i] = stack[i + 1];
            }
            // top stays at MAX_SIZE - 1, just overwrite last slot
            stack[top] = newAction;
            System.out.println("[UNDO STACK] Stack full — oldest action dropped.");
        } else {
            // Normal push: increment top, place action
            stack[++top] = newAction;
        }

        System.out.println("[UNDO STACK] Pushed -> " + newAction);
    }

    
    public Action pop() {
        if (isEmpty()) {
            System.out.println("[ERROR E-005] Undo stack is empty. Nothing to undo.");
            return null;
        }

        Action undoneAction = stack[top];  // Save top action
        stack[top] = null;                 // Clear slot (good practice)
        top--;                             // Decrement top

        System.out.println("[UNDO STACK] Popped (Undo) <- " + undoneAction);
        return undoneAction;
    }

    
    public Action peek() {
        if (isEmpty()) {
            System.out.println("[INFO] Undo stack is empty.");
            return null;
        }
        return stack[top];
    }

    
    public boolean isEmpty() {
        return top == -1;
    }

    
    public boolean isFull() {
        return top == MAX_SIZE - 1;
    }

    
    public int getSize() {
        return top + 1;
    }

    // ── Display ───────────────────────────────────────────────────────────────

    public void printStack() {
        System.out.println("\n-- Undo Stack (Array) ------------------------------------");
        System.out.println("   top index = " + top + "  |  size = " + getSize() + " / " + MAX_SIZE);
        System.out.println("   +---------+------------------------------------------+");
        System.out.println("   |   Idx   |  Action                                  |");
        System.out.println("   +---------+------------------------------------------+");

        if (isEmpty()) {
            System.out.println("   |         |  (Stack is empty)                        |");
        } else {
            for (int i = top; i >= 0; i--) {
                String marker = (i == top) ? " <- TOP" : "       ";
                System.out.printf("   |  [%2d]   |  %-30s %s|%n",
                    i, stack[i].actionType + " | " + stack[i].patientId, marker);
            }
        }
        System.out.println("   +---------+------------------------------------------+");
    }

    
    public void printRawArray() {
        System.out.println("\n-- Raw Array State (all " + MAX_SIZE + " slots) ----------------");
        for (int i = 0; i < MAX_SIZE; i++) {
            String marker = (i == top) ? " <- top" : "       ";
            if (stack[i] != null) {
                System.out.printf("   [%2d]  %s%s%n", i,
                    stack[i].actionType + " | " + stack[i].patientId, marker);
            } else {
                System.out.printf("   [%2d]  null%s%n", i, marker);
            }
        }
        System.out.println("---------------------------------------------------");
    }
}
