package info.kgeorgiy.ja.shevchenko.i18n;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TextStatistics {
    public static void main(String[] args) {
        if(!argsCorrect()) {
            System.err.println("Wrong arguments");
            return;
        }
        final Locale textLoc, reportLoc;
        textLoc = Locale.forLanguageTag(args[0]);
        reportLoc = Locale.forLanguageTag(args[1]);
        if(!reportLocCorrect()) {
            System.err.println("Can't work with this output locale.");
        }
        final Path inputFilePath,outputFilePath;
        try {
            inputFilePath = Paths.get(args[args.length - 2]);
            outputFilePath = Paths.get(args[args.length - 1]);
        } catch (InvalidPathException e) {
            System.err.println("Can't work with this path: " + e.getMessage());
            return;
        }
        TextAnalyzer analyzer = new TextAnalyzer(textLoc, reportLoc, inputFilePath, outputFilePath);
        try{
            analyzer.analyze();
        }catch (IOException e) {
            System.err.println("Can't read input or write into output file: " + e.getMessage());
        }
    }

    private static boolean reportLocCorrect() {
        return true;
    }

    private static boolean argsCorrect() {
        return true;
    }
}
