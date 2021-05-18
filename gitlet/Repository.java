package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * Keeps static variables to reference files
 * Has a host of methods that interact with files and write out the changes to existing or new files
 * does at a high level.
 *
 * @Pranit Panda
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File COMMIT_DIR = join(GITLET_DIR, "commitdirectory");

    private static File stagingAreaFile = join(GITLET_DIR, "staging");

    //If you call read object on this File, you will get the commit which is the head
    private static File head = join(GITLET_DIR, "head");

    //File that stores a hashmap of all branches
    private static File branchesFile = join(GITLET_DIR, "branches");

    private static File currentBranch = join(GITLET_DIR, "currentbranch");

    private static final String START = "<<<<<<< HEAD\n";

    private static final String MIDDLE = "=======\n";

    private static final String END = ">>>>>>>\n";

    public static void init() {
        //Filename, sha1 hash
        HashMap<String, String[]> stagingArea;

        // <Branch Name, sha1 of the commit>
        HashMap<String, String> branches = new HashMap<>();

        //short form, list of long forms


        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }


        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();

        stagingArea = new HashMap<>();
        writeObject(stagingAreaFile, stagingArea);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(1970, 0, 1, 0, 0, 0);


        Commit initialCommit = new Commit("initial commit", cal.getTime());


        String sha = shah1(initialCommit);
        File icFile = join(COMMIT_DIR, sha);

        branches.put("master", sha);
        writeContents(currentBranch, "master");

        writeObject(icFile, initialCommit);
        writeObject(head, initialCommit);
        writeObject(branchesFile, branches);


    }

    public static void add(String fileName) {
        HashMap<String, String[]> stagingArea = readObject(stagingAreaFile, HashMap.class);

        //finds original file
        File original = Utils.join(CWD, fileName);
        if (!original.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String s1 = sha1(readContents(original));

        //creates blob file to dump contents into
        File blob = Utils.join(GITLET_DIR, s1);

        //copies the original file into the blob file
        Utils.writeContents(blob, readContents(original));
        if (stagingArea.containsKey(fileName)) {
            stagingArea.remove(fileName);
        }
        Commit headCommit = headCommit();

        if (headCommit.getBlobs().get(fileName) != null) {
            if (headCommit.getBlobs().get(fileName).equals(s1)) {
                writeObject(stagingAreaFile, stagingArea);
                return;
            }
        }
        String[] info = {"add", s1};
        stagingArea.put(fileName, info);
        writeObject(stagingAreaFile, stagingArea);

    }

    public static void commit(String message) {
        HashMap<String, String[]> stagingArea = Utils.readObject(stagingAreaFile, HashMap.class);

        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        if (stagingArea.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit headCommit = headCommit();
        Commit newCommit = new Commit(message, headCommit);

        stagingArea = new HashMap<>();
        File commitFile = join(COMMIT_DIR, shah1(newCommit));
        writeObject(commitFile, newCommit);
        writeObject(stagingAreaFile, stagingArea);
        writeObject(head, newCommit);

        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        String cB = readContentsAsString(currentBranch);
        branches.put(cB, shah1(newCommit));

        writeObject(branchesFile, branches);

    }

    private static void mergeCommit(String message, Commit secondParent) {
        HashMap<String, String[]> stagingArea = Utils.readObject(stagingAreaFile, HashMap.class);

        if (stagingArea.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit headCommit = headCommit();
        Commit newCommit = new Commit(message, headCommit, secondParent);

        stagingArea = new HashMap<>();
        File commitFile = join(COMMIT_DIR, shah1(newCommit));
        writeObject(commitFile, newCommit);
        writeObject(stagingAreaFile, stagingArea);
        writeObject(head, newCommit);

        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        String cB = readContentsAsString(currentBranch);
        branches.put(cB, shah1(newCommit));
        writeObject(branchesFile, branches);

    }

    public static void log() {
        Commit current = headCommit();
        while (true) {

            String shahh1 = shah1(current);
            System.out.println("===");
            System.out.println("commit " + shahh1);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

            String formattedDate = simpleDateFormat.format(current.getDate());
            System.out.print("Date: ");
            System.out.println(formattedDate);
            System.out.println(current.getMessage());
            System.out.println();
            if (!current.hasParents()) {
                return;
            }
            current = current.getParents().get(0);
        }
    }

    public static void globalLog() {
        List<String> allCommits = plainFilenamesIn(COMMIT_DIR);
        for (String thing : allCommits) {
            File f = join(COMMIT_DIR, thing);
            Commit c = readObject(f, Commit.class);
            String s1 = shah1(c);
            System.out.println("===");
            System.out.println("commit " + s1);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

            String formattedDate = simpleDateFormat.format(c.getDate());
            System.out.print("Date: ");
            System.out.println(formattedDate);
            System.out.println(c.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        List<String> allCommits = plainFilenamesIn(COMMIT_DIR);
        boolean found = false;
        for (String thing : allCommits) {
            File f = join(COMMIT_DIR, thing);
            Commit c = readObject(f, Commit.class);
            if (c.getMessage().equals(message)) {
                String s1 = sha1(serialize(c));
                System.out.println(s1);
                found = true;
            }

        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void rm(String fileName) {
        HashMap<String, String[]> stagingArea = readObject(stagingAreaFile, HashMap.class);
        Commit current = headCommit();
        File f = join(CWD, fileName);

        if (stagedForAddition(fileName)) {
            stagingArea.remove(fileName);
        } else if (current.getBlobs().containsKey(fileName)) {
            String[] info = {"remove", current.getBlobs().get(fileName)};
            stagingArea.put(fileName, info);
            restrictedDelete(f);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        writeObject(stagingAreaFile, stagingArea);

    }

    public static void checkout(String filename) {
        Commit headCommit = headCommit();
        if (!headCommit.getBlobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File backup = join(GITLET_DIR, headCommit.getBlobs().get(filename));
        File working = join(CWD, filename);
        writeContents(working, readContents(backup));

    }

    public static void checkout(String commitID, String fileName) {
        commitID = idChecker(commitID);
        File commito = join(COMMIT_DIR, commitID);
        if (!commito.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit commit1 = readObject(commito, Commit.class);
        if (!commit1.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File backup = join(GITLET_DIR, commit1.getBlobs().get(fileName));
        File working = join(CWD, fileName);
        writeContents(working, readContents(backup));

    }

    private static void checkout(Commit c, String fileName) {
        String commitID = shah1(c);
        checkout(commitID, fileName);
    }

    public static void status() {
        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        Set<String> branchNames = branches.keySet();
        String[] brancho = (String[]) branchNames.toArray(new String[0]);
        Arrays.sort(brancho);
        System.out.println("=== Branches ===");
        String curr = readContentsAsString(currentBranch);
        for (String thing : brancho) {
            if (curr.equals(thing)) {
                System.out.print("*");
            }
            System.out.println(thing);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        HashMap<String, String[]> stagingArea = readObject(stagingAreaFile, HashMap.class);
        for (String thing : stagingArea.keySet()) {
            if (stagingArea.get(thing)[0].equals("add")) {
                System.out.println(thing);
            }

        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String thing : stagingArea.keySet()) {
            if (stagingArea.get(thing)[0].equals("remove")) {
                System.out.println(thing);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> allTheFiles = plainFilenamesIn(CWD);
        Commit current = headCommit();
        for (String file : current.getBlobs().keySet()) {

            File file1 = join(CWD, file);
            if (!file1.exists() && !stagingArea.containsKey(file)) {
                System.out.println(file + " (deleted)");
            } else if (file1.exists()) {
                String cwdsha = sha1(readContents(file1));
                if (!cwdsha.equals(current.getBlobs().get(file))) {
                    System.out.println(file + " (modified)");
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");


        for (String f : allTheFiles) {
            if (!current.getBlobs().containsKey(f) && !stagedForAddition(f)) {
                System.out.println(f);
            }
        }
        System.out.println();

    }

    public static void checkoutBranch(String branchName) {
        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        String cB = readContentsAsString(currentBranch);
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (cB.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }


        Commit current = headCommit();
        File destinationFile = join(COMMIT_DIR, branches.get(branchName));
        Commit destination = readObject(destinationFile, Commit.class);
        List<String> filesC = plainFilenamesIn(CWD);
        HashMap<String, String[]> stagingArea;

        for (String file : filesC) {
            if (!current.getBlobs().containsKey(file)) {
                if (!stagedForAddition(file)) {
                    if (destination.getBlobs().containsKey(file)) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }

        for (String fileName : filesC) {
            if (current.getBlobs().containsKey(fileName) || stagedForAddition(fileName)) {
                if (!destination.getBlobs().containsKey(fileName)) {
                    File tobeDeleted = join(CWD, fileName);
                    restrictedDelete(tobeDeleted);
                }
            }

        }
        for (String f : destination.getBlobs().keySet()) {
            File daFile = join(CWD, f);
            File daBaby = join(GITLET_DIR, destination.getBlobs().get(f));
            writeContents(daFile, readContents(daBaby));
        }
        stagingArea = new HashMap<>();
        writeObject(stagingAreaFile, stagingArea);
        writeContents(currentBranch, branchName);
        writeObject(head, destination);

    }

    public static void rmBranch(String branchName) {
        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);

        }
        String currentBranchName = readContentsAsString(currentBranch);
        if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }


        branches.remove(branchName);
        writeObject(branchesFile, branches);

    }

    public static void reset(String commitID) {
        commitID = idChecker(commitID);
        File commitFile = join(COMMIT_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit commito = readObject(commitFile, Commit.class);
        Commit headCommit = headCommit();
        List<String> allFiles = plainFilenamesIn(CWD);
        Set<String> mergedSet = new HashSet<>();
        mergedSet.addAll(allFiles);
        mergedSet.addAll(commito.getBlobs().keySet());
        for (String fileName : allFiles) {
            if (!headCommit.getBlobs().containsKey(fileName)) {
                if (!stagedForAddition(fileName)) {
                    if (commito.getBlobs().containsKey(fileName)) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
        for (String fileName : mergedSet) {
            if (commito.getBlobs().containsKey(fileName)) {
                checkout(commitID, fileName);
            } else {
                if (headCommit.getBlobs().containsKey(fileName) || stagedForAddition(fileName)) {
                    File tobDelete = join(CWD, fileName);
                    restrictedDelete(tobDelete);
                }
            }

        }
        writeObject(head, commito);
        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        branches.put(currentBranch(), commitID);
        writeObject(branchesFile, branches);
        HashMap<String, String[]> stagingArea = new HashMap<>();
        writeObject(stagingAreaFile, stagingArea);

    }

    public static void merge(String branchName) {
        HashMap<String, String[]> stagingArea = Utils.readObject(stagingAreaFile, HashMap.class);

        if (stagingArea.size() > 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(currentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        boolean conflict = false;
        Commit ancestor = branchCompare(branchName);
        Commit headCommit = headCommit();
        Commit bc = readObject(join(COMMIT_DIR, branches.get(branchName)), Commit.class);
        if (shah1(ancestor).equals(shah1(bc))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (shah1(headCommit).equals(shah1(ancestor))) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        List<String> allFiles = plainFilenamesIn(CWD);
        for (String file : allFiles) {
            if (!headCommit.getBlobs().containsKey(file) && bc.getBlobs().containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        for (String file : ancestor.getBlobs().keySet()) {
            boolean modBranch = modified(file, ancestor, bc);
            boolean modCurr = modified(file, ancestor, headCommit);
            if (modBranch && !modCurr) {
                if (bc.getBlobs().containsKey(file)) {
                    checkout(bc, file);
                    add(file);
                } else {
                    rm(file);
                }

            } else if (modBranch && modCurr) {
                if (modified(file, bc, headCommit)) {
                    String combo = combah(headCommit, bc, file);
                    writeContents(join(CWD, file), combo);
                    add(file);
                    conflict = true;

                }
            }
        }
        for (String file : bc.getBlobs().keySet()) {
            if (!ancestor.getBlobs().containsKey(file)) {
                if (!headCommit.getBlobs().containsKey(file)) {
                    checkout(bc, file);
                    add(file);
                } else if (modified(file, bc, headCommit)) {
                    String combo = combah(headCommit, bc, file);
                    writeContents(join(CWD, file), combo);
                    add(file);
                    conflict = true;
                }
            }
        }
        String message = "Merged " + branchName + " into " + currentBranch() + ".";
        mergeCommit(message, bc);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void branch(String branchName) {
        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit headC = headCommit();
        String s1 = sha1(serialize(headC));
        branches.put(branchName, s1);
        writeObject(branchesFile, branches);
    }

    private static boolean stagedForAddition(String fileName) {
        HashMap<String, String[]> stagingArea = readObject(stagingAreaFile, HashMap.class);
        return stagingArea.containsKey(fileName) && stagingArea.get(fileName)[0].equals("add");

    }

    private static String idChecker(String uid) {
        File commito = join(COMMIT_DIR, uid);
        if (commito.exists()) {
            return uid;
        }
        List<String> commitFullList = plainFilenamesIn(COMMIT_DIR);
        for (String name : commitFullList) {
            if (name.contains(uid)) {
                return name;
            }
        }
        return uid;

    }

    private static Commit headCommit() {
        return readObject(head, Commit.class);
    }

    private static String currentBranch() {
        return readContentsAsString(currentBranch);
    }

    public static Commit branchCompare(String branchname) {
        HashMap<String, String> branches = readObject(branchesFile, HashMap.class);
        String b2 = branches.get(branchname);
        String b1 = branches.get(currentBranch());
        File b2f = join(COMMIT_DIR, b2);
        File b1f = join(COMMIT_DIR, b1);
        Commit b1c = readObject(b1f, Commit.class);
        Commit b2c = readObject(b2f, Commit.class);
        return commonAncestorSetApproach(b1c, b2c);

    }

    public static Commit commonAncestorSetApproach(Commit commit1, Commit commit2) {
        Set<String> commitset1 = new HashSet<>();
        Set<String> commitset2 = new HashSet<>();
        commitset1.add(shah1(commit1));
        commitset2.add(shah1(commit2));
        Set<String> intersection = new HashSet<>(commitset1);
        intersection.retainAll(commitset2);
        while (intersection.size() == 0) {
            Set<String> parents1 = new HashSet<>();
            for (String s : commitset1) {
                Commit cc = shaToCommit(s);
                if (cc.hasParents()) {
                    parents1.addAll(cc.getParentsAsString());
                }
            }
            commitset1.addAll(parents1);
            Set<String> parents2 = new HashSet<>();
            for (String s : commitset2) {
                Commit cc = shaToCommit(s);
                if (cc.hasParents()) {
                    parents2.addAll(cc.getParentsAsString());
                }
            }
            commitset2.addAll(parents2);

            intersection = new HashSet<>(commitset1);
            intersection.retainAll(commitset2);
        }

        return shaToCommit(intersection.iterator().next());

    }

    public static String shah1(Commit c) {
        return sha1(serialize(c));
    }

    public static Commit shaToCommit(String sha) {
        File f = join(COMMIT_DIR, sha);
        return readObject(f, Commit.class);
    }

    private static boolean modified(String file, Commit ancestor, Commit current) {
        if (!ancestor.getBlobs().containsKey(file)) {
            return current.getBlobs().containsKey(file);
        }
        return !ancestor.getBlobs().get(file).equals(current.getBlobs().get(file));
    }

    public static File getStagingAreaFile() {
        return stagingAreaFile;
    }

    private static String combah(Commit commit1, Commit commi2, String fileName) {
        String s1;
        String s2;
        if (commit1.getBlobs().containsKey(fileName)) {
            File file = join(GITLET_DIR, commit1.getBlobs().get(fileName));
            s1 = readContentsAsString(file);
        } else {
            s1 = "";
        }
        if (commi2.getBlobs().containsKey(fileName)) {
            File file = join(GITLET_DIR, commi2.getBlobs().get(fileName));
            s2 = readContentsAsString(file);
        } else {
            s2 = "";
        }
        return START + s1 + MIDDLE + s2 + END;
    }


}
