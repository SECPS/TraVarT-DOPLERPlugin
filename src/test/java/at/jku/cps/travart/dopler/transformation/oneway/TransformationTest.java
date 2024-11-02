package at.jku.cps.travart.dopler.transformation.oneway;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TransformationTest {

    /**
     * Compares the real model from the file with the transformed model.
     *
     * @param pathToBeTransformed   Expected model
     * @param pathToBeTransformedIn Real, transformed model
     */
    @ParameterizedTest(name = "{1}")
    @MethodSource("dataSourceMethod")
    void testTransformation(Path pathToBeTransformed, Path pathToBeTransformedIn) throws NotSupportedVariabilityTypeException, IOException {

        //Transform model and create argument
        String transformedData = transform(pathToBeTransformed);
        String expectedModel = Files.readString(pathToBeTransformedIn);

        String message = "\n\n" + "Expected: \n " + expectedModel + "\n\n" + "But was: \n" + transformedData;
        Assertions.assertEquals(expectedModel, transformedData, message);
    }

    /**
     * Generates the data for the test method.
     *
     * @return Set of Arguments. Each argument consists of the expected data from the file and the real transformed model.
     */
    private Stream<Arguments> dataSourceMethod() throws IOException, NotSupportedVariabilityTypeException {

        //Collect files depending on getFromEnding()
        List<Path> filePathsSet;
        try (Stream<Path> filePaths = Files.walk(Path.of(getPath())).filter(path -> Files.isRegularFile(path) && path.toString().endsWith(getFromEnding()))) {
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
