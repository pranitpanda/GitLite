package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.*;

/**
 * Represents a gitlet commit object.
 * Gitlet commit has all the stuff man
 * does at a high level.
 *
 * @author Pranit Panda
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;
    private Date date;

    /**
     * first string is the filename,
     * second string is the blob sha1 to access the backup
     */
    private HashMap<String, String> blobs;

    private String parentsha1;
    private String parent2sha1;

    private int distance;


    public Commit(String message) {
        this.message = message;
        blobs = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = cal.getTime();
        stage();
        parentsha1 = "";
        parent2sha1 = "";
        distance = 0;
    }

    public Commit(String message, Commit parent) {
        this.message = message;
        blobs = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = cal.getTime();
        distance = parent.distance + 1;
        for (String fileName : parent.blobs.keySet()) {
            blobs.put(fileName, parent.blobs.get(fileName));
        }

        stage();
        parentsha1 = Repository.shah1(parent);
        parent2sha1 = "";
    }

    public Commit(String message, Commit parent1, Commit parent2) {
        this.message = message;
        blobs = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = cal.getTime();
        distance = Math.max(parent1.distance, parent2.distance) + 1;
        for (String fileName : parent1.blobs.keySet()) {
            blobs.put(fileName, parent1.blobs.get(fileName));
        }
        stage();
        parentsha1 = Repository.shah1(parent1);
        parent2sha1 = Repository.shah1(parent2);
    }


    public Commit(String message, Date date1) {
        date = date1;
        this.message = message;
        blobs = new HashMap<>();

        stage();

        parentsha1 = "";
        parent2sha1 = "";
        distance = 0;
    }

    private void stage() {
        HashMap<String, String[]> stagingA = Utils.readObject(getStagingAreaFile(), HashMap.class);
        for (String fileName : stagingA.keySet()) {
            if (stagingA.get(fileName)[0].equals("add")) {
                String shah1 = stagingA.get(fileName)[1];
                blobs.put(fileName, shah1);
            } else {
                blobs.remove(fileName);
            }

        }
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }


    public void setParent2sha1(String parent2sha1) {
        this.parent2sha1 = parent2sha1;
    }

    /**
     * If no parents, returns null
     * If one parent returns arraylist with one value
     * If two parents returns arraylist with two values, in order.
     *
     * @return
     */
    public ArrayList<Commit> getParents() {
        ArrayList<Commit> parents = new ArrayList<>();
        if (parentsha1.equals("")) {
            return null;
        }
        File parentFile = Utils.join(COMMIT_DIR, parentsha1);
        parents.add(Utils.readObject(parentFile, Commit.class));
        if (parent2sha1.equals("")) {
            return parents;
        }
        File parent2File = Utils.join(COMMIT_DIR, parent2sha1);
        parents.add(Utils.readObject(parent2File, Commit.class));
        return parents;
    }

    public ArrayList<String> getParentsAsString() {
        ArrayList<Commit> input = getParents();
        ArrayList<String> output = new ArrayList<>();
        for (Commit c : input) {
            output.add(shah1(c));
        }
        return output;
    }

    public boolean hasParents() {
        return !parentsha1.equals("");
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }
}
