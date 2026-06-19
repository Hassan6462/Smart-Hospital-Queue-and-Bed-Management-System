package ds;


public class AVLTree {

    // ── Inner Node class ──────────────────────────────────────────────────────

    private static class Node {
        String deptName;       // Department name (BST key)
        int    totalBeds;      // Total beds in this department
        int    availableBeds;  // Available beds
        int    height;         // Height of this node (for balance factor)
        Node   left;
        Node   right;

        Node(String deptName, int totalBeds) {
            this.deptName      = deptName;
            this.totalBeds     = totalBeds;
            this.availableBeds = totalBeds;
            this.height        = 1;        // New leaf has height 1
            this.left          = null;
            this.right         = null;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private Node root;
    private int  size;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AVLTree() {
        root = null;
        size = 0;
    }

   
    private int height(Node node) {
        return (node == null) ? 0 : node.height;
    }

   
    private void updateHeight(Node node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    
    private int balanceFactor(Node node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

   
    private Node rotateRight(Node z) {
        Node y  = z.left;
        Node T3 = y.right;   
        
        y.right = z;
        z.left  = T3;

        
        updateHeight(z);
        updateHeight(y);

        System.out.println("   [AVL] Right Rotation on: " + z.deptName
            + " | new subtree root: " + y.deptName);
        return y; 
    }

    
    private Node rotateLeft(Node z) {
        Node y  = z.right;
        Node T2 = y.left;    
        
        y.left  = z;
        z.right = T2;

        
        updateHeight(z);
        updateHeight(y);

        System.out.println("   [AVL] Left Rotation on: " + z.deptName
            + " | new subtree root: " + y.deptName);
        return y; 
    }

   
    public void insert(String deptName, int totalBeds) {
        root = insertNode(root, deptName, totalBeds);
        size++;
        System.out.println("[AVL TREE] Inserted department: " + deptName
            + " (" + totalBeds + " beds) | Tree size: " + size);
    }

    private Node insertNode(Node node, String deptName, int totalBeds) {

        
        if (node == null) {
            return new Node(deptName, totalBeds);
        }

        int cmp = deptName.compareToIgnoreCase(node.deptName);

        if (cmp < 0) {
            node.left  = insertNode(node.left,  deptName, totalBeds);
        } else if (cmp > 0) {
            node.right = insertNode(node.right, deptName, totalBeds);
        } else {
            System.out.println("[WARN] Department already exists: " + deptName);
            return node; 
        }

       
        updateHeight(node);

        
        int bf = balanceFactor(node);

      
        if (bf > 1 && deptName.compareToIgnoreCase(node.left.deptName) < 0) {
            System.out.println("   [AVL] LL imbalance at: " + node.deptName);
            return rotateRight(node);
        }

    
        if (bf < -1 && deptName.compareToIgnoreCase(node.right.deptName) > 0) {
            System.out.println("   [AVL] RR imbalance at: " + node.deptName);
            return rotateLeft(node);
        }

      
        if (bf > 1 && deptName.compareToIgnoreCase(node.left.deptName) > 0) {
            System.out.println("   [AVL] LR imbalance at: " + node.deptName);
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

       
        if (bf < -1 && deptName.compareToIgnoreCase(node.right.deptName) < 0) {
            System.out.println("   [AVL] RL imbalance at: " + node.deptName);
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

    
        return node;
    }

    
    public boolean search(String deptName) {
        Node result = searchNode(root, deptName);
        if (result == null) {
            System.out.println("[AVL] Department not found: " + deptName);
            return false;
        }
        System.out.println("[AVL] Found: " + result.deptName
            + " | Beds: " + result.availableBeds + "/" + result.totalBeds + " available");
        return true;
    }

    private Node searchNode(Node node, String deptName) {
        if (node == null) return null;

        int cmp = deptName.compareToIgnoreCase(node.deptName);
        if (cmp == 0)  return node;
        if (cmp < 0)   return searchNode(node.left,  deptName);
        return             searchNode(node.right, deptName);
    }

  
    public void occupyBed(String deptName) {
        Node node = searchNode(root, deptName);
        if (node != null && node.availableBeds > 0) {
            node.availableBeds--;
            System.out.println("[AVL] " + deptName + " available beds: " + node.availableBeds);
        }
    }

    
    public void freeBed(String deptName) {
        Node node = searchNode(root, deptName);
        if (node != null && node.availableBeds < node.totalBeds) {
            node.availableBeds++;
            System.out.println("[AVL] " + deptName + " available beds: " + node.availableBeds);
        }
    }

    
    public void printAllDepartments() {
        System.out.println("\n-- Hospital Departments (AVL Tree - Inorder = Sorted) --");
        System.out.printf("   %-25s %-12s %-12s %-8s%n",
            "Department", "Total Beds", "Available", "Height");
        System.out.println("   " + "-".repeat(60));

        if (root == null) {
            System.out.println("   (No departments added)");
        } else {
            inorderPrint(root);
        }

        System.out.println("   " + "-".repeat(60));
        System.out.println("   Total departments: " + size);
        System.out.println("   Tree height: " + height(root));
    }

    private void inorderPrint(Node node) {
        if (node == null) return;
        inorderPrint(node.left);
        System.out.printf("   %-25s %-12d %-12d %-8d%n",
            node.deptName, node.totalBeds, node.availableBeds, node.height);
        inorderPrint(node.right);
    }

  
    public void printTreeStructure() {
        System.out.println("\n-- AVL Tree Structure (BF = Balance Factor) ----------");
        if (root == null) {
            System.out.println("   (Empty tree)");
            return;
        }
        printTreeNode(root, "", true);
        System.out.println("\n   Tree height     : " + height(root));
        System.out.println("   Total depts     : " + size);
        System.out.println("   (All balance factors should be -1, 0, or +1)");
    }

    private void printTreeNode(Node node, String prefix, boolean isLeft) {
        if (node == null) return;
        System.out.println(prefix + (isLeft ? "+-- " : "\\-- ")
            + node.deptName
            + "  [h=" + node.height
            + ", bf=" + balanceFactor(node) + "]");
        printTreeNode(node.left,  prefix + (isLeft ? "|   " : "    "), true);
        printTreeNode(node.right, prefix + (isLeft ? "|   " : "    "), false);
    }

    public int getSize()        { return size; }
    public int getTreeHeight()  { return height(root); }
}
