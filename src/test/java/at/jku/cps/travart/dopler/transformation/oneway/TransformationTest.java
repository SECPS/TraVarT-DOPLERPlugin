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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TransformationTest {

    /**
     * Compares the real model from the file with the transformed model.
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
     * Generates the data for the test method.
     *
     * @return Set of Arguments. Each argument consists of the expected data from the file and the real transformed model.
     * @throws IOException                          Gets thrown if a file did not exist or could not be read
     * @throws NotSupportedVariabilityTypeException Gets thrown if the model could not be transformed
     */
    private Stream<Arguments> dataSourceMethod() throws IOException, NotSupportedVariabilityTypeException {

        //Collect files depending on getFromEnding()
        Set<Path> filePathsSet;
        try (Stream<Path> filePaths = Files.walk(Path.of(getPath())).filter(path -> Files.isRegularFile(path) && path.toString().endsWith(getFromEnding()))) {
            filePathsSet = filePaths.collect(Collectors.toSet());
        }

        //Create Arguments
        Set<Arguments> arguments = new HashSet<>();
        for (Path path : filePathsSet) {
            String doplerFilePath = path.toString().replace(getFromEnding(), getToEnding());

            String expectedModel = Files.readString(Path.of(doplerFilePath));

            //Transform model and create argument
            String transformedData = transform(path);
            arguments.add(Arguments.of(expectedModel, transformedData));
        }

        return arguments.stream();
    }

    protected abstract String transform(Path model) throws NotSupportedVariabilityTypeException, IOException;

    protected abstract String getToEnding();

    protected abstract String getFromEnding();

    protected abstract String getPath();

}
