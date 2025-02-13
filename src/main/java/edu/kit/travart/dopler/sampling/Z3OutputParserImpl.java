package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Implementation of {@link Z3OutputParser}. */
class Z3OutputParserImpl implements Z3OutputParser {

    /**
     * This regex matches the name value pairs in the input.
     * <p>
     * z3Answer has the form "((name_1 value_1)(name_2 value_2)...(name_n, value_n))"
     */
    private static final Pattern PATTERN = Pattern.compile("([^()]* ((false)|(true)))");

    @Override
    public Map<IConfigurable, Boolean> parseSatAnswer(String z3Answer) {
        List<String> allPairs = extractPairs(z3Answer);
        return createMap(allPairs);
    }

    private static List<String> extractPairs(String z3Answer) {
        List<String> allMatches = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(z3Answer);
        while (matcher.find()) {
            allMatches.add(matcher.group());
        }
        return allMatches;
    }

    /** A single pair has the form "name value" */
    private static Map<IConfigurable, Boolean> createMap(List<String> allPairs) {
        Map<IConfigurable, Boolean> pairs = new HashMap<>();
        for (String pair : allPairs) {
            String[] split = pair.split(" ");
            String name = split[0];
            boolean value = Boolean.parseBoolean(split[1]);
            pairs.put(new DoplerConfigurable(name, value), value);
        }
        return pairs;
    }
}
