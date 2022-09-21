package info.kgeorgiy.ja.shevchenko.walk;

public class RecursiveWalk {
    public static void main(String[] args) {
        try {
            Walker recursiveWalker = new RecursiveWalker(args);
            ActiveWalker.walk(recursiveWalker);
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        }
    }
}
