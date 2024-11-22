package edu.kit.dopler.transformation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestUtils {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(\\\\r)?\\\\n");

    public static <T> T[] add2BeginningOfArray(T[] elements, T element) {
        T[] newArray = Arrays.copyOf(elements, elements.length + 1);
        newArray[0] = element;
        System.arraycopy(elements, 0, newArray, 1, elements.length);

        return newArray;
    }

    public static String sortModel(String model) throws IOException {
        //Sometimes \r is used as line separator
        String sanitisedModel = NEW_LINE_PATTERN.matcher(model).replaceAll(System.lineSeparator());

        CSVParser parser = createCSVFormat(true).parse(new StringReader(sanitisedModel));

        List<CSVRecord> records = parser.stream().collect(Collectors.toCollection(ArrayList::new));

        records.sort(Comparator.comparing(o -> o.get(0)));

        List<String> lines = new ArrayList<>();
        for (CSVRecord record : records) {
            List<String> line = new ArrayList<>();
            List<String> temp2 = record.stream().collect(Collectors.toList());
            for (String value : temp2) {
                line.add(value.replace("\n", ""));
            }

            lines.add(String.join(";", line));
        }

        lines.add(0, String.join(";", DMCSVHeader.stringArray()));

        return String.join(System.lineSeparator(), lines);
    }


    public static CSVFormat createCSVFormat(final boolean skipHeader) {
        CSVFormat.Builder builder = CSVFormat.EXCEL.builder();
        builder.setDelimiter(DELIMITER);
        builder.setHeader(DMCSVHeader.stringArray());
        builder.setSkipHeaderRecord(skipHeader);
        return builder.build();
    }

    private static final char DELIMITER = ';';
}
