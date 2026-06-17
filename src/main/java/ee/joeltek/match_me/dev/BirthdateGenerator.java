package ee.joeltek.match_me.dev;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class BirthdateGenerator {
    private static final Random RANDOM = new Random(42);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static String generate() {
        int startYear = 1970;
        int endYear = 2005;

        int year = startYear + RANDOM.nextInt(endYear - startYear + 1);
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(YearMonth.of(year, month).lengthOfMonth());

        LocalDate birthdate = LocalDate.of(year, month, day);
        return birthdate.format(FORMATTER); // "yyyy-MM-dd"
    }
}
