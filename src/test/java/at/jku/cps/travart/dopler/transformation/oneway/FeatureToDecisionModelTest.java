package at.jku.cps.travart.dopler.transformation.oneway;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.io.DecisionModelSerializer;
import at.jku.cps.travart.dopler.printer.DoplerPrettyPrinter;
import at.jku.cps.travart.dopler.transformation.FilePair;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FeatureToDecisionModelTest {

    private static final String PATH = "src/test/resources/oneway/feature_to_decision_model";

    /**
     * Compares the real DOPLER model from the file with the transformed DOPLER model.
     *
     * @param expected Expected DOPLER model
     * @param real     Real, transformed DOPLER model
     */
    @ParameterizedTest
    @MethodSource("dataSourceMethod")
    void testTransformation(String expected, String real) {
        String message = "\n" + "Expected: \n " + expected + "\n" + "But was: \n " + real;
        Assertions.assertEquals(expected, real, message);
    }


    /**
     * Generetes the data for the test method.
     *
     * @return Set of Arguments. Each argument consists of the expected data from the DOPLER file and the real transformed DOPLER model.
     * @throws IOException                          Gets thrown if a file did not exist or could not be read
     * @throws NotSupportedVariabilityTypeException Gets thrown if the model could not be transformed
     */
    static Stream<Arguments> dataSourceMethod() throws IOException, NotSupportedVariabilityTypeException {
        //Read files
        Set<Path> filePathsSet;
        try (Stream<Path> filePaths = Files.walk(Path.of(PATH))) {
            filePathsSet = filePaths.collect(Collectors.toSet());
        }

        //Create file pairs
        Set<FilePair> filePairs = new HashSet<>();
        for (Path path : filePathsSet) {
            if (Files.isDirectory(path)) {
                continue;
            }

            if (Files.isRegularFile(path) && path.toString().endsWith(".uvl")) {
                String filePathWithoutEnding = path.toString().replace(".uvl", ".csv");
                String doplerFilePath = path.toString().replace(".uvl", ".csv");

                String uvlData = Files.readString(path);
                String doplerData = Files.readString(Path.of(doplerFilePath));

                filePairs.add(new FilePair(filePathWithoutEnding, uvlData, doplerData));
            }
        }

        //Create arguments for testing
        Set<Arguments> arguments = new HashSet<>();
        for (FilePair filePair : filePairs) {
            String transformedData = transformUvlToDopler(filePair.getUvlData());
            arguments.add(Arguments.of(filePair.getDoplerData(), transformedData));
        }

        return arguments.stream();
    }

    /**
     * Transforms the given UVL model to a DOPLER model.
     */
    private static String transformUvlToDopler(String uvlModel) throws NotSupportedVariabilityTypeException {
        UVLModelFactory uvlModelFactory = new UVLModelFactory();
        FeatureModel featureModel = uvlModelFactory.parse(uvlModel);
        OneWayTransformer oneWayTransformer = new OneWayTransformer();
        IDecisionModel decisionModel = oneWayTransformer.transform(featureModel, "SomeName");
        DoplerPrettyPrinter doplerPrettyPrinter = new DoplerPrettyPrinter(new DecisionModelSerializer());
        return doplerPrettyPrinter.toText(decisionModel);
    }
}
