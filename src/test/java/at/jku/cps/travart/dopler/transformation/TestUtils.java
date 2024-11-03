package at.jku.cps.travart.dopler.transformation;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TestUtils {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(\\\\r)?\\\\n");

    public static <T> T[] add2BeginningOfArray(T[] elements, T element) {
        T[] newArray = Arrays.copyOf(elements, elements.length + 1);
        newArray[0] = element;
        System.arraycopy(elements, 0, newArray, 1, elements.length);

        return newArray;
    }

    public static String sortModel(String model) {
        //Sometimes \r is used as line separator
        String sanitisedModel = NEW_LINE_PATTERN.matcher(model).replaceAll(System.lineSeparator());

        String[] lines = sanitisedModel.split(System.lineSeparator());
        String header = lines[0];
        String[] linesWithoutHeader = Arrays.copyOfRange(lines, 1, lines.length);
        Arrays.sort(linesWithoutHeader);
        return String.join(System.lineSeparator(), TestUtils.add2BeginningOfArray(linesWithoutHeader, header));
    }
}
