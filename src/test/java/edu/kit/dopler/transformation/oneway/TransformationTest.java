package edu.kit.dopler.transformation.oneway;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.transformation.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TransformationTest {

    private static final String DATA_SOURCE_METHOD = "dataSourceMethod";
    private static final long TIMEOUT_TIME = 0L;

    /**
     * Compares the real model from the file with the transformed model.
     *
     * @param pathToBeTransformed   Expected model
     * @param pathToBeTransformedIn Real, transformed model
     */
    @ParameterizedTest(name = "{1}")
    @MethodSource(DATA_SOURCE_METHOD)
    void testTransformation(Path pathToBeTransformed, Path pathToBeTransformedIn) throws Exception {
        //Transform model and create argument
        String transformedModel = transform(pathToBeTransformed);
        String expectedModel = Files.readString(pathToBeTransformedIn);

        //Sort models
        String expected = TestUtils.sortModel(expectedModel);
        String actual = TestUtils.sortModel(transformedModel);

        String message = String.format("%n%nExpected:%n %s%n%nBut was: %n%s", expected, actual);
        Assertions.assertEquals(expected, actual, message);
    }

    //@ParameterizedTest(name = "{1}")
    @MethodSource(DATA_SOURCE_METHOD)
    void numberOfConfigs(Path pathToBeTransformed, Path pathToBeTransformedIn) {
        Assertions.assertTimeoutPreemptively(Duration.of(TIMEOUT_TIME, ChronoUnit.SECONDS), () -> {
            try {
                testNumberOfConfigs(pathToBeTransformed);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
    }

    abstract void testNumberOfConfigs(Path pathToBeTransformed) throws Exception;

    /**
     * Generates the data for the test method.
     *
     * @return Set of Arguments. Each argument consists of the expected data from the file and the real transformed
     * model.
     */
    private Stream<Arguments> dataSourceMethod() throws IOException {

        //Collect files depending on getFromEnding()
        List<Path> filePathsSet;
        try (Stream<Path> filePaths = Files.walk(Path.of(getPath()))
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(getFromEnding()))) {
            filePathsSet = filePaths.collect(Collectors.toList());
        }

        //Create Arguments
        List<Arguments> arguments = new ArrayList<>();
        for (Path pathToBeTransformed : filePathsSet) {
            String pathToBeTransformedIn = pathToBeTransformed.toString().replace(getFromEnding(), getToEnding());
            arguments.add(Arguments.of(pathToBeTransformed, Path.of(pathToBeTransformedIn)));
        }

        return arguments.stream();
    }

    protected abstract String transform(Path model) throws NotSupportedVariabilityTypeException, IOException;

    protected abstract String getToEnding();

    protected abstract String getFromEnding();

    protected abstract String getPath();
}
