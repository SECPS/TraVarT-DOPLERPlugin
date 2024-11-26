package edu.kit.dopler.transformation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TransformationTest<FromModel, ToModel> {

    private static final long TIMEOUT_TIME = 1L;

    /**
     * Compares the real model from the file with the transformed model.
     *
     * @param pathOfTheBeTransformedModel Expected model
     * @param pathOfExpectedModel         Real, transformed model
     */
    @ParameterizedTest(name = "{1}")
    @MethodSource("dataSourceMethod")
    void testTransformation(Path pathOfTheBeTransformedModel, Path pathOfExpectedModel) throws Exception {
        FromModel modelToTransform = getModelToTransform(pathOfTheBeTransformedModel);

        String transformedModel = convertToModelToString(transformModel(modelToTransform));
        String expectedModel = getExpectedModel(pathOfExpectedModel);

        String message = String.format("%n%nExpected:%n %s%n%nBut was: %n%s", expectedModel, transformedModel);
        Assertions.assertEquals(expectedModel, transformedModel, message);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("dataSourceMethod")
    @Disabled
    void testNumberOfConfigs(Path pathToBeTransformed, Path pathToBeTransformedIn) throws Exception {
        FromModel modelToBeTransformed = getModelToTransform(pathToBeTransformed);
        ToModel transformedModel = transformModel(modelToBeTransformed);

        Assertions.assertTimeoutPreemptively(Duration.of(TIMEOUT_TIME, ChronoUnit.MILLIS), () -> {
            int amountOfConfigsExpected = getAmountOfConfigsOfFromModel(modelToBeTransformed);
            int amountOfConfigsReal = getAmountOfConfigsOfToModel(transformedModel);
            Assertions.assertEquals(amountOfConfigsExpected, amountOfConfigsReal);
        });
    }

    protected abstract int getAmountOfConfigsOfToModel(ToModel toModel) throws Exception;

    protected abstract int getAmountOfConfigsOfFromModel(FromModel fromModel) throws Exception;

    /**
     * Generates the data for the test method.
     *
     * @return Set of Arguments. Each argument consists of the expected data from the file and the real transformed
     * model.
     */
    private Stream<Arguments> dataSourceMethod() throws IOException {

        //Collect files depending on getFromEnding()
        List<Path> filePathsSet;
        try (Stream<Path> filePaths = Files.walk(getTestDataPath())
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(getFromEnding()))) {
            filePathsSet = filePaths.toList();
        }

        //Create Arguments
        List<Arguments> arguments = new ArrayList<>();
        for (Path pathToBeTransformed : filePathsSet) {
            String pathToBeTransformedIn = pathToBeTransformed.toString().replace(getFromEnding(), getToEnding());
            arguments.add(Arguments.of(pathToBeTransformed, Path.of(pathToBeTransformedIn)));
        }

        return arguments.stream();
    }

    protected abstract String getToEnding();

    protected abstract String getFromEnding();

    protected abstract Path getTestDataPath();

    protected abstract FromModel getModelToTransform(Path pathOfModelToBeTransformed) throws Exception;

    protected abstract ToModel transformModel(FromModel modelToBeTransformed) throws Exception;

    protected abstract String getExpectedModel(Path pathOfExpectedModel) throws Exception;

    protected abstract String convertToModelToString(ToModel toModel) throws Exception;
}
