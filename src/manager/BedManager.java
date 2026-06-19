package manager;


public class BedManager {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final int ROWS       = 5;   // 5 floors
    private static final int COLS       = 10;  // 10 rooms per floor
    private static final int EMPTY      = 0;
    private static final int OCCUPIED   = 1;
    private static final int RESERVED   = 2;

    // ── Fields ────────────────────────────────────────────────────────────────

    private final int[][]    grid;          // 2D array: grid[floor][room]
    private final String[][] patientIds;    // Who is in each bed
    private int              totalOccupied;
    private int              totalReserved;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BedManager() {
        grid       = new int[ROWS][COLS];
        patientIds = new String[ROWS][COLS];
        // Java initializes int arrays to 0 (EMPTY) automatically
        totalOccupied = 0;
        totalReserved = 0;
    }

    // ── Bed ID Helpers ────────────────────────────────────────────────────────

    
    private int[] parseBedId(String bedId) {
        try {
            // Format: "F2-R4" → split on "-"
            String[] parts = bedId.split("-");
            int row = Integer.parseInt(parts[0].substring(1)); // "F2" → 2
            int col = Integer.parseInt(parts[1].substring(1)); // "R4" → 4
            if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
                System.out.println("[ERROR] Bed ID out of range: " + bedId);
                return null;
            }
            return new int[]{row, col};
        } catch (Exception e) {
            System.out.println("[ERROR] Invalid bed ID format: " + bedId
                + " (expected format: F0-R0 to F" + (ROWS-1) + "-R" + (COLS-1) + ")");
            return null;
        }
    }

    /** Generate bed ID string from row and column. */
    private String makeBedId(int row, int col) {
        return "F" + row + "-R" + col;
    }

    
    public String assignBed(String patientId) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] == EMPTY) {
                    // Found first empty bed — assign it
                    grid[row][col]       = OCCUPIED;
                    patientIds[row][col] = patientId;
                    totalOccupied++;

                    String bedId = makeBedId(row, col);
                    System.out.println("[BED MANAGER] Bed " + bedId
                        + " assigned to patient " + patientId);

                    checkLowBedAlert();
                    return bedId;
                }
            }
        }

        // No empty bed found
        System.out.println("[ERROR E-002] No beds available! All " + (ROWS * COLS) + " beds are occupied.");
        return null;
    }

   
    public boolean assignSpecificBed(String bedId, String patientId) {
        int[] pos = parseBedId(bedId);
        if (pos == null) return false;

        int row = pos[0], col = pos[1];

        if (grid[row][col] != EMPTY) {
            System.out.println("[ERROR] Bed " + bedId + " is not empty. Current state: "
                + stateLabel(grid[row][col]));
            return false;
        }

        grid[row][col]       = OCCUPIED;
        patientIds[row][col] = patientId;
        totalOccupied++;

        System.out.println("[BED MANAGER] Specific bed " + bedId
            + " assigned to patient " + patientId);
        checkLowBedAlert();
        return true;
    }

   
    public boolean freeBed(String bedId) {
        int[] pos = parseBedId(bedId);
        if (pos == null) return false;

        int row = pos[0], col = pos[1];

        if (grid[row][col] == EMPTY) {
            System.out.println("[WARN] Bed " + bedId + " is already empty.");
            return false;
        }

        String previousPatient = patientIds[row][col];
        grid[row][col]       = EMPTY;
        patientIds[row][col] = null;

        if (grid[row][col] == OCCUPIED) totalOccupied--;
        totalOccupied = Math.max(0, totalOccupied - 1);

        System.out.println("[BED MANAGER] Bed " + bedId + " freed. "
            + "(was: patient " + previousPatient + ")");
        return true;
    }

   
    public boolean reserveBed(String bedId) {
        int[] pos = parseBedId(bedId);
        if (pos == null) return false;

        int row = pos[0], col = pos[1];

        if (grid[row][col] != EMPTY) {
            System.out.println("[ERROR] Cannot reserve bed " + bedId
                + " — already " + stateLabel(grid[row][col]));
            return false;
        }

        grid[row][col] = RESERVED;
        totalReserved++;
        System.out.println("[BED MANAGER] Bed " + bedId + " reserved.");
        return true;
    }

   
    public boolean isAvailable(String bedId) {
        int[] pos = parseBedId(bedId);
        if (pos == null) return false;
        return grid[pos[0]][pos[1]] == EMPTY;
    }

  
    public String getPatientInBed(String bedId) {
        int[] pos = parseBedId(bedId);
        if (pos == null) return null;
        return patientIds[pos[0]][pos[1]];
    }

    // ── Statistics ────────────────────────────────────────────────────────────

    public int getTotalBeds()     { return ROWS * COLS; }
    public int getOccupiedCount() { return totalOccupied; }
    public int getEmptyCount()    { return getTotalBeds() - totalOccupied - totalReserved; }
    public int getReservedCount() { return totalReserved; }

    public double getOccupancyPercent() {
        return (totalOccupied * 100.0) / getTotalBeds();
    }

    // ── Low Bed Alert ─────────────────────────────────────────────────────────

    
    private void checkLowBedAlert() {
        int available = getEmptyCount();
        double percent = (available * 100.0) / getTotalBeds();
        if (percent < 10.0) {
            System.out.println("\n  *** WARNING E-006: Only " + available
                + " beds remaining (" + String.format("%.1f", percent)
                + "% available)! ***\n");
        }
    }

    // ── Display ───────────────────────────────────────────────────────────────

   
    public void printGrid() {
        System.out.println("\n-- Hospital Bed Grid (" + ROWS + " floors x " + COLS + " rooms) --");
        System.out.println("   [ ] = Empty   [X] = Occupied   [R] = Reserved");
        System.out.println();

        // Column headers
        System.out.print("         ");
        for (int col = 0; col < COLS; col++) {
            System.out.printf("R%-2d ", col);
        }
        System.out.println();
        System.out.print("         ");
        System.out.println("-".repeat(COLS * 4));

        // Each row = one floor
        for (int row = 0; row < ROWS; row++) {
            System.out.printf("Floor %d | ", row);
            for (int col = 0; col < COLS; col++) {
                switch (grid[row][col]) {
                    case EMPTY:    System.out.print("[ ] "); break;
                    case OCCUPIED: System.out.print("[X] "); break;
                    case RESERVED: System.out.print("[R] "); break;
                }
            }
            // Floor summary
            int floorOccupied = 0;
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] == OCCUPIED) floorOccupied++;
            }
            System.out.println("  | " + floorOccupied + "/" + COLS + " occupied");
        }

        System.out.println();
        System.out.println("   Total beds  : " + getTotalBeds());
        System.out.println("   Occupied    : " + getOccupiedCount());
        System.out.println("   Empty       : " + getEmptyCount());
        System.out.println("   Reserved    : " + getReservedCount());
        System.out.printf( "   Occupancy   : %.1f%%%n", getOccupancyPercent());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String stateLabel(int state) {
        switch (state) {
            case EMPTY:    return "EMPTY";
            case OCCUPIED: return "OCCUPIED";
            case RESERVED: return "RESERVED";
            default:       return "UNKNOWN";
        }
    }
}
