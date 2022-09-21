package info.kgeorgiy.ja.shevchenko.walk;

class ActiveWalker {
    static void walk(Walker walker) {
        try {
            walker.walk();
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                walker.close();
            } catch (WalkerException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
