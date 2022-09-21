package info.kgeorgiy.ja.shevchenko.i18n;

import java.util.ListResourceBundle;

public class Bundle_ru extends ListResourceBundle {
    private static final Object[][] CONTENTS = {
            {"file", "Анализируемый файл"},
            {"stats", "Сводная статистика"},
            {"Number", "Число"},
            {"sentences", "предложений"},
            {"words","слов"},
            {"numbers", "чисел"},
            {"currencies", "cумм"},
            {"dates", "дат"},

            {"sentenceStat", "Статистика по предложениям"},
            {"minneu", "Минимальное"},
            {"minfem", "Минимальная"},
            {"maxneu", "Максимальное"},
            {"maxfem", "Максимальная"},
            {"sentence", "предложение"},
            {"sentensegen", "предложения"},
            {"length", "длина"},
            {"avgfem", "Средняя"},
            {"unique", "различных"},

            {"wordStat", "Статистика по словам"},
            {"word", "слово"},
            {"wordgen", "слова"},

            {"numbersStat", "Статистика по числам"},
            {"number", "число"},
            {"avgneu", "Среднее"},

            {"currencyStat", "Статистика по суммам денег"},
            {"cur", "сумма"},

            {"datesStat", "Статистика по датам"},
            {"date", "дата"},

    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
