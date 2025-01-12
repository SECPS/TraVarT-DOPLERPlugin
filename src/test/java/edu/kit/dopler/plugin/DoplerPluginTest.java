package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.IDeserializer;
import at.jku.cps.travart.core.common.IPlugin;
import at.jku.cps.travart.core.common.IPrettyPrinter;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.TestUtils;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.model.Dopler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

class DoplerPluginTest {

    private static final Path TEST_DATA_PATH = Paths.get("src", "test", "resources", "plugin");

    private final IPlugin<Dopler> doplerPlugin = new DoplerPluginImpl();
    private final ISerializer<Dopler> serializer = doplerPlugin.getSerializer();
    private final IDeserializer<Dopler> deserializer = doplerPlugin.getDeserializer();
    private final IPrettyPrinter<Dopler> printer = doplerPlugin.getPrinter();
    private final DecisionModelReader decisionModelReader = new DecisionModelReader();

    private static Stream<Arguments> dataSourceMethod() throws IOException {
        List<Path> paths;
        try (Stream<Path> filePaths = Files.walk(TEST_DATA_PATH)) {
            paths = filePaths.toList();
        }

        return paths.stream().filter(path -> path.toString().endsWith(".csv")).map(Arguments::of);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataSourceMethod")
    void testSerialising(Path pathOfModel) throws Exception {
        String doplerModelAsString = Files.readString(pathOfModel);
        Dopler dopler = decisionModelReader.read(doplerModelAsString, "Some model name");
        String serialisedModel = serializer.serialize(dopler);
        Dopler deserializesModel = deserializer.deserialize(serialisedModel, new CsvFormat());
        String deserilisedModelAsString = printer.toText(deserializesModel);
        Assertions.assertEquals(TestUtils.sortDecisionModel(doplerModelAsString),
                TestUtils.sortDecisionModel(deserilisedModelAsString));
    }

    @Test
    void testTablePrinting() throws NotSupportedVariabilityTypeException {
        String modelAsString = """
                ID;Question;Type;Range;Cardinality;Constraint/Rule;Visible/relevant if
                A;?;String;;;;true
                B;?;Double;;;;true
                C;?;Boolean;true | false;;;true
                D;?;Enumeration;X | Y | Z;1:4;;true""";

        Dopler model = deserializer.deserialize(modelAsString, new CsvFormat());

        String[][] table = printer.toTable(model);
        Arrays.sort(table, Comparator.comparing(row -> row[0]));

        Assertions.assertArrayEquals(new String[]{"A", "?", "String", "", "", "", "true"}, table[0]);
        Assertions.assertArrayEquals(new String[]{"B", "?", "Double", "", "", "", "true"}, table[1]);
        Assertions.assertArrayEquals(new String[]{"C", "?", "Boolean", "true | false", "", "", "true"}, table[2]);
        Assertions.assertArrayEquals(new String[]{"D", "?", "Enumeration", "X | Y | Z", "1:4", "", "true"}, table[3]);
    }
}