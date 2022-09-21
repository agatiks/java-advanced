package info.kgeorgiy.ja.shevchenko.i18n;

import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatisticAnalyzer{
    public static Statistic getTextStatistic(String text, BreakIterator iterator, boolean isSentence, Collator cmp) {
        Statistic textStat = new Statistic();
        double sum = 0;
        Set<String> uniqueElems = new HashSet<>();
        iterator.setText(text);
        int startOfElem = iterator.first(), endOfElem = iterator.next();
        while(endOfElem != BreakIterator.DONE) {
            String s = text.substring(startOfElem, endOfElem).replaceAll("\\s+", " ");
            String currElem = (isSentence) ?
                    s :
                    s.replaceAll("[^\\w]", "").replaceAll("[\\d]", "");
            if (!currElem.isEmpty()) {
                textStat.number++;
                uniqueElems.add(currElem);
                sum += currElem.length();
                if(textStat.minElem == null || cmp.compare(currElem, textStat.minElem) < 0) {
                    textStat.minElem = currElem;
                }
                if(textStat.maxElem == null || cmp.compare(currElem, textStat.maxElem) > 0) {
                    textStat.maxElem = currElem;
                }
                if(textStat.minSizeElem == null || textStat.minSize > currElem.length()) {
                    textStat.minSizeElem = currElem;
                    textStat.minSize = currElem.length();
                }
                if(textStat.maxSizeElem == null || textStat.maxSize < currElem.length()) {
                    textStat.maxSizeElem = currElem;
                    textStat.maxSize = currElem.length();
                }
            }
            startOfElem = endOfElem;
            endOfElem = iterator.next();
        }
        textStat.numberUnique = uniqueElems.size();
        textStat.avgSize = String.valueOf(sum/textStat.number);
        return textStat;
    }

    public static Statistic[] getValuesStat(String text, Locale textLoc) {
        BreakIterator it = BreakIterator.getWordInstance(textLoc);
        Calendar calendar = Calendar.getInstance(textLoc);
        it.setText(text);
        int startOfString = it.first();
        int endOfString = it.next();

        BreakIterator dateIt = BreakIterator.getSentenceInstance(textLoc);
        dateIt.setText(text);
        int startOfSent = dateIt.first();
        int endOfSent = dateIt.next();

        BreakIterator linesIt = BreakIterator.getLineInstance(textLoc);
        linesIt.setText(text);
        int startOfLine = linesIt.first();
        int endOfLine = linesIt.next();

        List<NumberStat> numberList = new ArrayList<>();
        List<CurrencyStat> currencyList = new ArrayList<>();
        List<DateStat> datesList = new ArrayList<>();

        NumberFormat isCurrency = NumberFormat.getCurrencyInstance(textLoc);
        String symbol = Currency.getInstance(new Locale("ru", "RU")).getSymbol();
        NumberFormat isNumber = NumberFormat.getNumberInstance(textLoc);
        DateFormat isMediumDate = DateFormat.getDateInstance(DateFormat.MEDIUM, textLoc);
        Pattern pattern = Pattern.compile("dd M yyyy г");
        DateFormat isShortDate = DateFormat.getDateInstance(DateFormat.SHORT, textLoc);

        while(endOfSent != BreakIterator.DONE) {
            String currLine = text.substring(startOfSent, endOfSent);
            Matcher matcher = pattern.matcher(currLine);
            if(matcher.find()) {
                System.out.println(matcher.group());
            }
            try {
                calendar.setTime(isMediumDate.parse(currLine));
                datesList.add(new DateStat(calendar.getTime()));
            } catch (ParseException ignored) {
            }
            startOfSent = endOfSent;
            endOfSent = dateIt.next();
        }

        while (endOfLine != BreakIterator.DONE) {
            String currLine = text.substring(startOfLine, endOfLine);
            try {
                calendar.setTime(isShortDate.parse(currLine));
                datesList.add(new DateStat(calendar.getTime()));
            } catch (ParseException ignored) {
            }
            try {
                Number curr = isCurrency.parse(currLine);
                currencyList.add(new CurrencyStat(curr));
            } catch (ParseException ignored) {
            }
            startOfLine = endOfLine;
            endOfLine = linesIt.next();
        }

        while (endOfString != BreakIterator.DONE) {
            String currentElement = text.substring(startOfString, endOfString);
            startOfString = endOfString;
            endOfString = it.next();

            try {
                Number curr = isCurrency.parse(currentElement);
                currencyList.add(new CurrencyStat(curr));
                numberList.add(new NumberStat(curr.doubleValue()));
                continue;
            } catch (ParseException ignored) {
            }

            try {
                Number curr = isNumber.parse(currentElement);
                numberList.add(new NumberStat(curr.doubleValue()));
            } catch (ParseException ignored) {
            }
        }
        System.out.println(numberList);
        System.out.println(currencyList);
        System.out.println(datesList);
        Statistic numberStat = StatisticAnalyzer.getNumberStat(numberList);
        Statistic currencyStat = StatisticAnalyzer.getCurrencyStat(currencyList, isCurrency);
        Statistic datesStat = StatisticAnalyzer.getDatesStat(datesList, isMediumDate);

        return new Statistic[]{numberStat, currencyStat, datesStat};
    }

    private static Statistic getNumberStat(List<NumberStat> list) {
        if(list.isEmpty()) {
            return new Statistic();
        }
        Statistic stat = new Statistic();
        stat.number = list.size();
        stat.numberUnique = (int) list.stream().distinct().count();
        stat.minElem = list.stream().map(NumberStat::getValue).min(Double::compare).get().toString();
        stat.maxElem = list.stream().map(NumberStat::getValue).max(Double::compare).get().toString();
        stat.avgSize = String.valueOf(list.stream()
                .map(NumberStat::getValue)
                .reduce(Double::sum).get()/stat.number);
        return stat;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Statistic getCurrencyStat(List<CurrencyStat> list, NumberFormat isCurrency) {
        if(list.isEmpty()) {
            return new Statistic();
        }
        Statistic stat = new Statistic();
        stat.number = list.size();
        stat.numberUnique = (int) list.stream().distinct().count();
        stat.minElem = isCurrency.format(list.stream()
                .map(elem -> elem.getValue().doubleValue())
                .min(Double::compare)
                .get());
        stat.maxElem = isCurrency.format(list.stream()
                .map(elem -> elem.getValue().doubleValue())
                .max(Double::compare)
                .get());
        stat.avgSize = isCurrency.format(list.stream()
                .map(elem -> elem.getValue().doubleValue())
                .reduce(Double::sum).get()/stat.number);
        return stat;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Statistic getDatesStat(List<DateStat> list, DateFormat isDate) {
        if(list.isEmpty()) {
            return new Statistic();
        }
        Statistic stat = new Statistic();
        stat.number = list.size();
        stat.numberUnique = (int) list.stream().distinct().count();
        stat.minElem = isDate.format(list.stream()
                .map(DateStat::getValue)
                .min(Date::compareTo)
                .get());
        stat.maxElem = isDate.format(list.stream()
                .map(DateStat::getValue)
                .max(Date::compareTo)
                .get());
        stat.avgSize = isDate.format(list.stream().map(e -> (double) e.getValue().getTime())
                .reduce(Double::sum).get() / stat.number);
        return stat;
    }

    private static class CurrencyStat {
        private final Number value;

        CurrencyStat(Number value) {
            this.value = value;
        }

        Number getValue() {
            return value;
        }
    }

    private static class DateStat {
        private final Date value;

        DateStat(Date value) {
            this.value = value;
        }

        Date getValue() {
            return value;
        }
    }

    private static class NumberStat {
        private final Double value;

        NumberStat(Double value) {
            this.value = value;
        }

        Double getValue() {
            return value;
        }
    }

}
