package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.commit(args[1]);
                break;
            case "log":
                checkInitialized();
                Repository.log();
                break;
            case "checkout":
                checkInitialized();
                incorrectOperands(args, 2);
                if (args[1].equals("--")) {
                    //checkout file name
                    Repository.checkout(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkout(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    System.out.println("Incorrect Operands.");
                    System.exit(0);
                }

                break;
            case "rm":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.rm(args[1]);
                break;
            case "status":
                checkInitialized();
                Repository.status();
                break;
            case "rm-branch":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "global-log":
                checkInitialized();
                Repository.globalLog();
                break;
            case "find":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.find(args[1]);
                break;
            case "branch":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.branch(args[1]);
                break;
            case "reset":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                checkInitialized();
                incorrectOperands(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
                break;
        }
    }

    private static void incorrectOperands(String[] got, int wanted) {
        if (got.length >= wanted) {
            return;
        }
        System.out.println("Incorrect Operands.");
        System.exit(0);
    }

    private static void checkInitialized() {
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet Directory.");
            System.exit(0);
        }
    }

}
