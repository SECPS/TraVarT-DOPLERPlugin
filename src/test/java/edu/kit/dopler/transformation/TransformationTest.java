package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
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

import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ONE_WAY;
import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ROUNDTRIP;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TransformationTest<FromModel, ToModel> {

    private static final long TIMEOUT_TIME = 1L;

    /**
     * Compares the real model from the file with the transformed model.
     *
     * @param pathOfTheBeTransformedModel Expected model
     * @param pathOfExpectedModel         Real, transformed model
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("oneWayDataSourceMethod")
    void testOneWayTransformation(Path pathOfTheBeTransformedModel, Path pathOfExpectedModel) throws Exception {
        FromModel modelToTransform = getFromModelFromPath(pathOfTheBeTransformedModel);

        String transformedModel = convertToModelToString(transformFromModelToToModel(modelToTransform, ONE_WAY));
        String expectedModel = readToModelAsString(pathOfExpectedModel);

        String message = String.format("%n%nExpected:%n%s%n%nBut was: %n%s%n%n", expectedModel, transformedModel);
        Assertions.assertEquals(expectedModel, transformedModel, message);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripDataSourceMethod")
    @Disabled
    void testRoundTripTransformation(Path path1, Path path2, Path path3) throws Exception {
        // first transformation
        FromModel modelToTransform = getFromModelFromPath(path1);
        String transformedModel = convertToModelToString(transformFromModelToToModel(modelToTransform, ROUNDTRIP));
        String expectedModel = readToModelAsString(path2);

        String message = String.format("%n%nExpected:%n%s%n%nBut was: %n%s%n%n", expectedModel, transformedModel);
        Assertions.assertEquals(expectedModel, transformedModel, message);

        // second transformation
        ToModel modelToTransform2 = getToModelFromString(transformedModel);
        String transformedModel2 = convertFromModelToString(transformToModelToFromModel(modelToTransform2, ONE_WAY));
        String expectedModel2 = readFromModelAsString(path3);

        String message2 = String.format("%n%nExpected:%n%s%n%nBut was: %n%s%n%n", expectedModel2, transformedModel2);
        Assertions.assertEquals(expectedModel2, transformedModel2, message2);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("oneWayDataSourceMethod")
    @Disabled
    void testNumberOfConfigs(Path pathToBeTransformed, Path pathToBeTransformedIn) throws Exception {
        FromModel modelToBeTransformed = getFromModelFromPath(pathToBeTransformed);
        ToModel transformedModel =
                transformFromModelToToModel(modelToBeTransformed, IModelTransformer.STRATEGY.ROUNDTRIP);

        Assertions.assertTimeoutPreemptively(Duration.of(TIMEOUT_TIME, ChronoUnit.MILLIS), () -> {
            int amountOfConfigsExpected = getAmountOfConfigsOfFromModel(modelToBeTransformed);
            int amountOfConfigsReal = getAmountOfConfigsOfToModel(transformedModel);
            Assertions.assertEquals(amountOfConfigsExpected, amountOfConfigsReal);
        });
    }

    /**
     * Generates the data for the test method.
     *
     * @return Set of Arguments. Each argument consists of the expected data from the file and the real transformed
     * model.
     */
    private Stream<Arguments> oneWayDataSourceMethod() throws IOException {

        //Collect files depending on getFromEnding()
        List<Path> filePathsSet;
        try (Stream<Path> filePaths = Files.walk(getOneWayDataPath())
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

    private Stream<Arguments> roundTripDataSourceMethod() throws IOException {

        //Collect files depending on getFromEnding()
        List<Path> filePathsSet;
        try (Stream<Path> filePaths = Files.walk(getRoundTripDataPath())
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".1" + getFromEnding()))) {
            filePathsSet = filePaths.toList();
        }

        //Create Arguments
        List<Arguments> arguments = new ArrayList<>();
        for (Path path1 : filePathsSet) {
            String path2 = path1.toString().replace(".1" + getFromEnding(), ".2" + getToEnding());
            String path3 = path1.toString().replace(".1" + getFromEnding(), ".3" + getFromEnding());
            arguments.add(Arguments.of(path1, Path.of(path2), Path.of(path3)));
        }

        return arguments.stream();
    }

    protected abstract Path getRoundTripDataPath();

    protected abstract String getToEnding();

    protected abstract String getFromEnding();

    protected abstract Path getOneWayDataPath();

    protected abstract FromModel getFromModelFromPath(Path path) throws Exception;

    protected abstract ToModel getToModelFromString(String model) throws Exception;

    protected abstract ToModel transformFromModelToToModel(FromModel modelToBeTransformed,
                                                           IModelTransformer.STRATEGY strategy) throws Exception;

    protected abstract FromModel transformToModelToFromModel(ToModel modelToBeTransformed,
                                                             IModelTransformer.STRATEGY strategy) throws Exception;

    protected abstract String readToModelAsString(Path path) throws Exception;

    protected abstract String readFromModelAsString(Path path) throws Exception;

    protected abstract String convertToModelToString(ToModel toModel) throws Exception;

    protected abstract String convertFromModelToString(FromModel toModel) throws Exception;

    protected abstract int getAmountOfConfigsOfToModel(ToModel toModel) throws Exception;

    protected abstract int getAmountOfConfigsOfFromModel(FromModel fromModel) throws Exception;
}
