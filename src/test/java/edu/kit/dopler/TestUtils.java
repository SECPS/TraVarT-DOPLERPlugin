package edu.kit.dopler;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static edu.kit.dopler.io.CSVUtils.createCSVFormat;

/** This class contains some utility methods for the tests */
public final class TestUtils {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(\\\\r)?\\\\n");
    private static final String ENUM_SEPARATOR = " | ";

    private TestUtils() {
    }

    /**
     * Sorts the given decision model
     *
     * @param model Model to sort
     *
     * @return Sorted model
     *
     * @throws IOException Gets thrown, if the model could not be transformed into the CSV format
     */
    public static String sortDecisionModel(String model) throws IOException {
        //Sometimes \r is used as line separator
        String sanitisedModel = NEW_LINE_PATTERN.matcher(model).replaceAll(System.lineSeparator());
        CSVParser parser = createCSVFormat(true).parse(new StringReader(sanitisedModel));

        List<CSVRecord> records = parser.getRecords();
        records.sort(Comparator.comparing(o -> o.get(0)));

        List<String> lines = new ArrayList<>();
        for (CSVRecord record : records) {
            List<String> line = new ArrayList<>();
            List<String> temp2 = record.stream().toList();
            for (String value : temp2) {

                //Sort enum values
                String newValue = value;
                if (value.contains(ENUM_SEPARATOR)) {
                    String[] enums = value.split(Pattern.quote(ENUM_SEPARATOR));
                    Arrays.sort(enums);
                    newValue = String.join(ENUM_SEPARATOR, enums);
                }

                if (newValue.contains(";") && !newValue.startsWith("\"")) {
                    line.add("\"" + newValue.replace(System.lineSeparator(), "") + "\"");
                } else {
                    line.add(newValue.replace(System.lineSeparator(), ""));
                }
            }

            lines.add(String.join(";", line));
        }

        lines.addFirst(String.join(";", stringArray()));
        return String.join(System.lineSeparator(), lines);
    }

    private static String[] stringArray() {
        return Arrays.stream(CsvHeader.values()).map(CsvHeader::toString).toArray(String[]::new);
    }

    private enum CsvHeader {
        ID("ID"), QUESTION("Question"), TYPE("Type"), RANGE("Range"), CARDINALITY("Cardinality"),
        RULES("Constraint/Rule"), VISIBLITY("Visible/relevant if");

        private final String header;

        CsvHeader(String header) {
            this.header = header;
        }

        @Override
        public String toString() {
            return header;
        }
    }
}
