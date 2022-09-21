package info.kgeorgiy.ja.shevchenko.walk;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

class RecursiveWalker extends Walker {
    RecursiveWalker(String[] args) throws WalkerException {
        super(args);
    }

    @Override
    Stream<Path> filterChild(Stream<Path> children) {
        return children.filter(Predicate.not(Files::isDirectory));
    }
}
