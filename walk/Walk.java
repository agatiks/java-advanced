package info.kgeorgiy.ja.shevchenko.walk;

public class Walk {
    public static void main(String[] args) {
        try {
            SimpleWalker simpleWalker = new SimpleWalker(args);
            ActiveWalker.walk(simpleWalker);
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        }
    }
}
