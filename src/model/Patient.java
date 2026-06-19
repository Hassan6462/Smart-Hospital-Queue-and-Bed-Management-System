package model;

/**
 * Patient model class — represents a hospital patient.
 * Used across all data structures in the system.
 */
public class Patient {

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum Status {
        WAITING, ADMITTED, DISCHARGED
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    private String patientId;       // Auto-generated unique ID
    private String name;
    private int    age;
    private Gender gender;
    private String contact;
    private String complaint;       // Chief complaint / reason for visit
    private int    triageScore;     // 1 (critical) → 5 (non-urgent)
    private Status status;
    private String bedId;           // null if not yet assigned
    private long   arrivalTime;     // System.currentTimeMillis() at registration

    // ── Constructor ──────────────────────────────────────────────────────────

    public Patient(String patientId, String name, int age, Gender gender,
                   String contact, String complaint, int triageScore) {

        if (triageScore < 1 || triageScore > 5) {
            throw new IllegalArgumentException(
                "[ERROR E-001] Triage score must be between 1 and 5. Got: " + triageScore);
        }

        this.patientId   = patientId;
        this.name        = name;
        this.age         = age;
        this.gender      = gender;
        this.contact     = contact;
        this.complaint   = complaint;
        this.triageScore = triageScore;
        this.status      = Status.WAITING;
        this.bedId       = null;
        this.arrivalTime = System.currentTimeMillis();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getPatientId()  { return patientId; }
    public String getName()       { return name; }
    public int    getAge()        { return age; }
    public Gender getGender()     { return gender; }
    public String getContact()    { return contact; }
    public String getComplaint()  { return complaint; }
    public int    getTriageScore(){ return triageScore; }
    public Status getStatus()     { return status; }
    public String getBedId()      { return bedId; }
    public long   getArrivalTime(){ return arrivalTime; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setStatus(Status status)   { this.status = status; }
    public void setBedId(String bedId)     { this.bedId  = bedId; }
    public void setTriageScore(int score)  {
        if (score < 1 || score > 5)
            throw new IllegalArgumentException("Triage score must be 1–5.");
        this.triageScore = score;
    }

    // ── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "┌─ Patient ─────────────────────────────\n" +
            "│  ID       : %s\n" +
            "│  Name     : %s\n" +
            "│  Age      : %d  |  Gender: %s\n" +
            "│  Contact  : %s\n" +
            "│  Complaint: %s\n" +
            "│  Triage   : %d  |  Status: %s\n" +
            "│  Bed      : %s\n" +
            "└───────────────────────────────────────",
            patientId, name, age, gender,
            contact, complaint,
            triageScore, status,
            bedId != null ? bedId : "Not Assigned"
        );
    }

    // Short one-line summary used in lists/tables
    public String toSummary() {
        return String.format("%-10s %-20s Age:%-3d Triage:%-2d Status:%-10s Bed:%-8s",
            patientId, name, age, triageScore, status,
            bedId != null ? bedId : "—");
    }
}
