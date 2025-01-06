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

class TestUtils {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(\\\\r)?\\\\n");
    private static final String ENUM_SEPERATOR = " | ";

    static String sortDecisionModel(String model) throws IOException {
        //Sometimes \r is used as line separator
        String sanitisedModel = NEW_LINE_PATTERN.matcher(model).replaceAll(System.lineSeparator());
        CSVParser parser = createCSVFormat().parse(new StringReader(sanitisedModel));

        List<CSVRecord> records = parser.getRecords();
        records.sort(Comparator.comparing(o -> o.get(0)));

        List<String> lines = new ArrayList<>();
        for (CSVRecord record : records) {
            List<String> line = new ArrayList<>();
            List<String> temp2 = record.stream().toList();
            for (String value : temp2) {

                //Sort enum values
                String newValue = value;
                if (value.contains(ENUM_SEPERATOR)) {
                    String[] enums = value.split(Pattern.quote(ENUM_SEPERATOR));
                    Arrays.sort(enums);
                    newValue = String.join(ENUM_SEPERATOR, enums);
                }

                if (newValue.contains(";") && !newValue.startsWith("\"")) {
                    line.add("\"" + newValue.replace("\n", "") + "\"");
                } else {
                    line.add(newValue.replace("\n", ""));
                }
            }

            lines.add(String.join(";", line));
        }

        lines.addFirst(String.join(";", DMCSVHeader.stringArray()));

        return String.join(System.lineSeparator(), lines);
    }

    private static CSVFormat createCSVFormat() {
        CSVFormat.Builder builder = CSVFormat.EXCEL.builder();
        builder.setDelimiter(';');
        builder.setHeader(DMCSVHeader.stringArray());
        builder.setSkipHeaderRecord(true);
        return builder.build();
    }
}
