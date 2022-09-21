package info.kgeorgiy.ja.shevchenko.walk;

import java.nio.file.Path;
import java.util.stream.Stream;

class SimpleWalker extends Walker {
    SimpleWalker(String[] args) throws WalkerException {
        super(args);
    }

    @Override
    Stream<Path> filterChild(Stream<Path> children) {
        return children;
    }
}
