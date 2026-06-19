package manager;

import model.Patient;

public class SortingUtils {

    // ── Bubble Sort ───────────────────────────────────────────────────────────

   
    public static Patient[] bubbleSortByName(Patient[] patients) {
        Patient[] arr   = copyArray(patients);
        int       n     = arr.length;
        int       swaps = 0;

        System.out.println("\n-- Bubble Sort by Name (A to Z) ----------------------");

        for (int pass = 0; pass < n - 1; pass++) {
            boolean swapped = false;

            
            for (int j = 0; j < n - pass - 1; j++) {
                String nameA = arr[j].getName();
                String nameB = arr[j + 1].getName();

                if (nameA.compareToIgnoreCase(nameB) > 0) {
                    // nameA comes after nameB alphabetically → swap
                    System.out.println("   Pass " + (pass+1) + ": Swapping \""
                        + nameA + "\" <-> \"" + nameB + "\"");
                    swap(arr, j, j + 1);
                    swapped = true;
                    swaps++;
                }
            }

            // Early exit: if no swaps happened, array is sorted
            if (!swapped) {
                System.out.println("   Pass " + (pass+1) + ": No swaps — sorted early! Stopping.");
                break;
            }
        }

        System.out.println("   Total swaps: " + swaps);
        return arr;
    }

    // ── Selection Sort ────────────────────────────────────────────────────────

   
    public static Patient[] selectionSortByAge(Patient[] patients) {
        Patient[] arr = copyArray(patients);
        int       n   = arr.length;

        System.out.println("\n-- Selection Sort by Age (youngest to oldest) --------");

        for (int i = 0; i < n - 1; i++) {
            // Assume first element of unsorted part is minimum
            int minIndex = i;

            // Find actual minimum in unsorted part [i+1 .. n-1]
            for (int j = i + 1; j < n; j++) {
                if (arr[j].getAge() < arr[minIndex].getAge()) {
                    minIndex = j;
                }
            }

            // Swap minimum with first unsorted element (if different)
            if (minIndex != i) {
                System.out.println("   Pass " + (i+1) + ": Min found = "
                    + arr[minIndex].getName() + " (age " + arr[minIndex].getAge()
                    + ") — swapping with index " + i
                    + " (" + arr[i].getName() + ")");
                swap(arr, i, minIndex);
            } else {
                System.out.println("   Pass " + (i+1) + ": Min already at index " + i
                    + " (" + arr[i].getName() + ") — no swap needed");
            }
        }

        return arr;
    }

    // ── Insertion Sort ────────────────────────────────────────────────────────

    
    public static Patient[] insertionSortByTriage(Patient[] patients) {
        Patient[] arr = copyArray(patients);
        int       n   = arr.length;

        System.out.println("\n-- Insertion Sort by Triage Score (1=urgent to 5=non-urgent) --");

        for (int i = 1; i < n; i++) {
            Patient key = arr[i];      // The element we are inserting
            int     j   = i - 1;      // Start comparing from element before key

            System.out.println("   i=" + i + ": Inserting " + key.getName()
                + " (triage=" + key.getTriageScore() + ")");

            // Shift elements that are LARGER than key one position to the right
            while (j >= 0 && arr[j].getTriageScore() > key.getTriageScore()) {
                System.out.println("      Shifting " + arr[j].getName()
                    + " (triage=" + arr[j].getTriageScore() + ") right");
                arr[j + 1] = arr[j];
                j--;
            }

            // Insert key in the correct gap
            arr[j + 1] = key;
            System.out.println("      Placed at index " + (j + 1));
        }

        return arr;
    }

    // ── Print Utility ─────────────────────────────────────────────────────────

    /**
     * Print a patient array as a table.
     */
    public static void printSorted(Patient[] arr, String title) {
        System.out.println("\n-- " + title + " --------------------");
        System.out.printf("   %-5s %-22s %-5s %-8s %-12s%n",
            "Rank", "Name", "Age", "Triage", "Status");
        System.out.println("   " + "-".repeat(55));

        for (int i = 0; i < arr.length; i++) {
            System.out.printf("   %-5d %-22s %-5d %-8d %-12s%n",
                i + 1,
                arr[i].getName(),
                arr[i].getAge(),
                arr[i].getTriageScore(),
                arr[i].getStatus());
        }
        System.out.println("   " + "-".repeat(55));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Swap two elements in an array. */
    private static void swap(Patient[] arr, int i, int j) {
        Patient temp = arr[i];
        arr[i]       = arr[j];
        arr[j]       = temp;
    }

    /** Return a shallow copy of the array (sort doesn't modify original). */
    private static Patient[] copyArray(Patient[] original) {
        Patient[] copy = new Patient[original.length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i];
        }
        return copy;
    }
}
