package info.kgeorgiy.ja.shevchenko.i18n;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.BreakIterator;
import java.text.Collator;
import java.util.Locale;
import java.util.ResourceBundle;

public final class TextAnalyzer {
    private static Locale textLoc, reportLoc;
    private static Path input, output;
    public TextAnalyzer(Locale textLoc, Locale reportLoc, Path inputPath, Path outputPath) {
        TextAnalyzer.textLoc = textLoc;
        TextAnalyzer.reportLoc = reportLoc;
        input = inputPath;
        output = outputPath;
    }
    public void analyze() throws IOException{
        String text;
        try {
            text = Files.readString(input);
        } catch (IOException e) {
            System.err.println("Can't read input file: " + e.getMessage());
            return;
        }
        Collator cmp = Collator.getInstance(textLoc);
        Statistic sentenceStat = StatisticAnalyzer.getTextStatistic(text, BreakIterator.getSentenceInstance(textLoc), true, cmp);
        Statistic wordStat = StatisticAnalyzer.getTextStatistic(text, BreakIterator.getWordInstance(textLoc),false, cmp);
        Statistic[] valuesStat = StatisticAnalyzer.getValuesStat(text, textLoc);
        Statistic numberStat = valuesStat[0];
        Statistic currencyStat = valuesStat[1];
        Statistic datesStat = valuesStat[2];
        ResourceBundle bundle;
        System.out.println(reportLoc.toLanguageTag());
        switch (reportLoc.getLanguage()) {
            case "en":
                bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.shevchenko.i18n.Bundle_en");
                break;
            case "ru":
                bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.shevchenko.i18n.Bundle_ru");
                break;
            default:
                System.err.println("ERROR: This locale is unavailable.");
                return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            String start = String.format("%s \"%s\"\n",
                    bundle.getString("file"), input.getFileName());
            String summaryStat = String.format("%s\n" +
                    "    %s %s: %d.\n" +
                    "    %s %s: %d.\n" +
                    "    %s %s: %d.\n" +
                    "    %s %s: %d.\n" +
                    "    %s %s: %d.\n",
                    bundle.getString("stats"),
                    bundle.getString("Number"),
                    bundle.getString("sentences"),
                    sentenceStat.number,
                    bundle.getString("Number"),
                    bundle.getString("words"),
                    wordStat.number,
                    bundle.getString("Number"),
                    bundle.getString("numbers"),
                    numberStat.number,
                    bundle.getString("Number"),
                    bundle.getString("currencies"),
                    currencyStat.number,
                    bundle.getString("Number"),
                    bundle.getString("dates"),
                    datesStat.number
                    );
            String sentStatText = String.format("%s\n" +
                            "    %s %s: %d (%d %s).\n" +
                            "    %s %s: \"%s\".\n" +
                            "    %s %s: \"%s\".\n" +
                            "    %s %s %s: %d (\"%s\").\n" +
                            "    %s %s %s: %d (\"%s\").\n" +
                            "    %s %s %s: %s.\n",
                    bundle.getString("sentenceStat"),
                    bundle.getString("Number"),
                    bundle.getString("sentences"),
                    sentenceStat.number,
                    sentenceStat.numberUnique,
                    bundle.getString("unique"),
                    bundle.getString("minneu"),
                    bundle.getString("sentence"),
                    sentenceStat.minElem.replaceAll("\\s", " "),
                    bundle.getString("maxneu"),
                    bundle.getString("sentence"),
                    sentenceStat.maxElem,
                    bundle.getString("minfem"),
                    bundle.getString("length"),
                    bundle.getString("sentensegen"),
                    sentenceStat.minSize,
                    sentenceStat.minSizeElem,
                    bundle.getString("maxfem"),
                    bundle.getString("length"),
                    bundle.getString("sentensegen"),
                    sentenceStat.maxSize,
                    sentenceStat.maxSizeElem,
                    bundle.getString("avgfem"),
                    bundle.getString("length"),
                    bundle.getString("sentensegen"),
                    sentenceStat.avgSize
            );
            String wordStatText = String.format("%s\n" +
                            "    %s %s: %d (%d %s).\n" +
                            "    %s %s: \"%s\".\n" +
                            "    %s %s: \"%s\".\n" +
                            "    %s %s %s: %d (\"%s\").\n" +
                            "    %s %s %s: %d (\"%s\").\n" +
                            "    %s %s %s: %s.\n",
                    bundle.getString("wordStat"),
                    bundle.getString("Number"),
                    bundle.getString("words"),
                    wordStat.number,
                    wordStat.numberUnique,
                    bundle.getString("unique"),
                    bundle.getString("minneu"),
                    bundle.getString("word"),
                    wordStat.minElem,
                    bundle.getString("maxneu"),
                    bundle.getString("word"),
                    wordStat.maxElem,
                    bundle.getString("minfem"),
                    bundle.getString("length"),
                    bundle.getString("wordgen"),
                    wordStat.minSize,
                    wordStat.minSizeElem,
                    bundle.getString("maxfem"),
                    bundle.getString("length"),
                    bundle.getString("wordgen"),
                    wordStat.maxSize,
                    wordStat.maxSizeElem,
                    bundle.getString("avgfem"),
                    bundle.getString("length"),
                    bundle.getString("wordgen"),
                    wordStat.avgSize
            );
            String numStatText = String.format("%s\n" +
                            "    %s %s: %d (%d %s).\n" +
                            "    %s %s: %s.\n" +
                            "    %s %s: %s.\n" +
                            "    %s %s: %s.\n",
                    bundle.getString("numbersStat"),
                    bundle.getString("Number"),
                    bundle.getString("numbers"),
                    numberStat.number,
                    numberStat.numberUnique,
                    bundle.getString("unique"),
                    bundle.getString("minneu"),
                    bundle.getString("number"),
                    numberStat.minElem,
                    bundle.getString("maxneu"),
                    bundle.getString("number"),
                    numberStat.maxElem,
                    bundle.getString("avgneu"),
                    bundle.getString("number"),
                    numberStat.avgSize
            );
            String curStatText = String.format("%s\n" +
                            "    %s %s: %d (%d %s).\n" +
                            "    %s %s: %s.\n" +
                            "    %s %s: %s.\n" +
                            "    %s %s: %s.\n",
                    bundle.getString("currencyStat"),
                    bundle.getString("Number"),
                    bundle.getString("currencies"),
                    currencyStat.number,
                    currencyStat.numberUnique,
                    bundle.getString("unique"),
                    bundle.getString("minfem"),
                    bundle.getString("cur"),
                    currencyStat.minElem,
                    bundle.getString("maxfem"),
                    bundle.getString("cur"),
                    currencyStat.maxElem,
                    bundle.getString("avgfem"),
                    bundle.getString("cur"),
                    currencyStat.avgSize
            );
            String datStatText = String.format("%s\n" +
                            "    %s %s: %d (%d %s).\n" +
                            "    %s %s: %s.\n" +
                            "    %s %s: %s.\n" +
                            "    %s %s: %s.\n",
                    bundle.getString("datesStat"),
                    bundle.getString("Number"),
                    bundle.getString("dates"),
                    datesStat.number,
                    datesStat.numberUnique,
                    bundle.getString("unique"),
                    bundle.getString("minfem"),
                    bundle.getString("date"),
                    datesStat.minElem,
                    bundle.getString("maxfem"),
                    bundle.getString("date"),
                    datesStat.maxElem,
                    bundle.getString("avgfem"),
                    bundle.getString("date"),
                    datesStat.avgSize
            );
            writer.write(start);
            writer.write(summaryStat);
            writer.write(sentStatText);
            writer.write(wordStatText);
            writer.write(numStatText);
            writer.write(curStatText);
            writer.write(datStatText);
        }
    }
}
