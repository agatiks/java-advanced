package info.kgeorgiy.ja.shevchenko.i18n;

import java.util.ListResourceBundle;

public class Bundle_en extends ListResourceBundle {
    private static final Object[][] CONTENTS = {
            {"file", "Parsed file"},
            {"stats", "Summary statistics"},
            {"Number", "Number of"},
            {"sentences", "sentences"},
            {"words","words"},
            {"numbers", "numbers"},
            {"currencies", "currencies"},
            {"dates", "dates"},

            {"sentenceStat", "Sentence statistic"},
            {"minneu", "Minimum"},
            {"minfem", "Minimum"},
            {"maxneu", "Maximum"},
            {"maxfem", "Maximum"},
            {"sentence", "sentence"},
            {"sentensegen", "sentence"},
            {"length", "length of"},
            {"avgfem", "Average"},
            {"unique", "различных"},

            {"wordStat", "Words statistic"},
            {"word", "word"},
            {"wordgen", "word"},

            {"numbersStat", "Numbers statistic"},
            {"number", "число"},
            {"avgneu", "Average"},

            {"currencyStat", "Currency statistic"},
            {"cur", "currency"},

            {"datesStat", "Dates statistic"},
            {"date", "date"},
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
