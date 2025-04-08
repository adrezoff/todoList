package org.example.bot.utils;

import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.*;

public class DateTimeParser {
    private static final Locale RUSSIAN_LOCALE = new Locale("ru");
    private static final DateTimeFormatter[] DATE_FORMATS = {
            // Форматы с годом
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(RUSSIAN_LOCALE),

            // Форматы без года (день и месяц)
            DateTimeFormatter.ofPattern("dd.MM HH:mm").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("dd.MM HH:mm:ss").withLocale(RUSSIAN_LOCALE),

            // Текстовые форматы с месяцами
            DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("d MMMM HH:mm").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("d MMMM").withLocale(RUSSIAN_LOCALE),

            // Сокращенные названия месяцев
            DateTimeFormatter.ofPattern("d MMM yyyy HH:mm").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("d MMM yyyy").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("d MMM HH:mm").withLocale(RUSSIAN_LOCALE),
            DateTimeFormatter.ofPattern("d MMM").withLocale(RUSSIAN_LOCALE)
    };

    private static final Map<String, String> RELATIVE_DAYS = Map.of(
            "сегодня", "0 дней",
            "завтра", "1 день",
            "послезавтра", "2 дня"
    );

    private static final Pattern RELATIVE_PATTERN = Pattern.compile(
            "(через)\\s+(\\d+)\\s+(день|дня|дней|месяц|месяца|месяцев|недел[юи])");

    public static LocalDateTime parse(String input) throws DateTimeParseException {
        input = input.trim().toLowerCase();

        // 1. Пробуем распарсить относительные даты
        LocalDateTime relativeDate = parseRelativeDate(input);
        if (relativeDate != null) {
            return relativeDate;
        }

        // 2. Пробуем стандартные форматы дат
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                TemporalAccessor parsed = formatter.parse(input);
                LocalDateTime dateTime = completeDateTime(parsed);
                if (dateTime != null) {
                    return dateTime;
                }
            } catch (DateTimeParseException ignored) {
                // Пробуем следующий формат
            }
        }

        // 3. Пробуем извлечь дату без времени (например "24 мая")
        LocalDateTime dateOnly = parseDateWithoutTime(input);
        if (dateOnly != null) {
            return dateOnly;
        }

        throw new DateTimeParseException("Не удалось распознать дату: " + input, input, 0);
    }
    private static LocalDateTime parseRelativeExpression(String expression) {
        // Разбиваем выражение на части
        String[] parts = expression.split("\\s+");

        // Проверяем минимальное количество частей (например, "1 день 12:00" - 3 части)
        if (parts.length < 2) {
            return null;
        }

        int amount;
        try {
            amount = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        String unit = parts[1].toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        if (unit.startsWith("д")) { // день, дня, дней
            now = now.plusDays(amount);
        } else if (unit.startsWith("мес")) { // месяц, месяца, месяцев
            now = now.plusMonths(amount);
        } else if (unit.startsWith("недел")) { // неделя, недели, недель
            now = now.plusWeeks(amount);
        } else {
            return null;
        }

        // Если указано время (часть после единицы измерения)
        if (parts.length >= 3) {
            try {
                String[] timeParts = parts[2].split(":");
                int hours = Integer.parseInt(timeParts[0]);
                int minutes = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;

                // Проверяем корректность времени
                if (hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60) {
                    now = now.withHour(hours).withMinute(minutes).withSecond(0).withNano(0);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // Если время указано некорректно, оставляем время по умолчанию
            }
        }

        return now;
    }
    private static LocalDateTime parseRelativeDate(String input) {
        // Проверяем простые относительные даты
        if (RELATIVE_DAYS.containsKey(input)) {
            String days = RELATIVE_DAYS.get(input);
            return parseRelativeExpression(days + " 12:00");
        }

        // Проверяем сложные относительные выражения
        Matcher matcher = RELATIVE_PATTERN.matcher(input);
        if (matcher.matches()) {
            int amount = Integer.parseInt(matcher.group(2));
            String unit = matcher.group(3);

            LocalDateTime now = LocalDateTime.now();
            if (unit.startsWith("д")) {
                return now.plusDays(amount);
            } else if (unit.startsWith("мес")) {
                return now.plusMonths(amount);
            } else if (unit.startsWith("недел")) {
                return now.plusWeeks(amount);
            }
        }

        return null;
    }

    private static LocalDateTime completeDateTime(TemporalAccessor parsed) {
        try {
            LocalDate date = extractDate(parsed);
            LocalTime time = extractTime(parsed);
            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate extractDate(TemporalAccessor parsed) {
        // Если год не указан, определяем его автоматически
        int year = parsed.isSupported(ChronoField.YEAR)
                ? parsed.get(ChronoField.YEAR)
                : determineYear(parsed);

        int month = parsed.get(ChronoField.MONTH_OF_YEAR);
        int day = parsed.get(ChronoField.DAY_OF_MONTH);

        return LocalDate.of(year, month, day);
    }

    private static int determineYear(TemporalAccessor parsed) {
        Month month = Month.from(parsed);
        int currentYear = Year.now().getValue();

        if (month.getValue() < LocalDate.now().getMonthValue()) {
            return currentYear + 1;
        }
        return currentYear;
    }

    private static LocalTime extractTime(TemporalAccessor parsed) {
        if (!parsed.isSupported(ChronoField.HOUR_OF_DAY)) {
            return LocalTime.of(12, 0);
        }

        int hour = parsed.get(ChronoField.HOUR_OF_DAY);
        int minute = parsed.isSupported(ChronoField.MINUTE_OF_HOUR)
                ? parsed.get(ChronoField.MINUTE_OF_HOUR)
                : 0;

        return LocalTime.of(hour, minute);
    }

    private static LocalDateTime parseDateWithoutTime(String input) {
        try {
            DateTimeFormatter[] dateOnlyFormats = {
                    DateTimeFormatter.ofPattern("d MMMM").withLocale(RUSSIAN_LOCALE),
                    DateTimeFormatter.ofPattern("d MMM").withLocale(RUSSIAN_LOCALE),
                    DateTimeFormatter.ofPattern("dd.MM").withLocale(RUSSIAN_LOCALE)
            };

            for (DateTimeFormatter formatter : dateOnlyFormats) {
                try {
                    TemporalAccessor parsed = formatter.parse(input);
                    LocalDate date = extractDate(parsed);
                    return LocalDateTime.of(date, LocalTime.of(12, 0));
                } catch (DateTimeParseException ignored) {
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}