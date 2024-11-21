package at.jku.cps.travart.dopler.transformation.oneway;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import edu.kit.dopler.io.DecisionModelWriter;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.oneway.OneWayTransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FeatureToDecisionModelTest extends TransformationTest {

    private static final String STANDARD_MODEL_NAME = "Some name";
    private static final Path TEMP_PATH = Path.of("src/test/resources/oneway/TEMP.txt");

    /**
     * Transforms the given UVL model to a DOPLER model.
     */
    @Override
    protected String transform(Path model) throws NotSupportedVariabilityTypeException, IOException {
        UVLModelFactory uvlModelFactory = new UVLModelFactory();
        FeatureModel featureModel = uvlModelFactory.parse(Files.readString(model));
        OneWayTransformer oneWayTransformer = new OneWayTransformer();
        Dopler decisionModel = oneWayTransformer.transform(featureModel, STANDARD_MODEL_NAME);

        DecisionModelWriter decisionModelWriter = new DecisionModelWriter();
        decisionModelWriter.write(decisionModel, TEMP_PATH); //Save in temp file

        String result = Files.readString(TEMP_PATH); //Read temp file
        Files.writeString(TEMP_PATH, ""); //Reset temp file
        return result;
    }

    @Override
    protected final String getToEnding() {
        return ".csv";
    }

    @Override
    protected final String getFromEnding() {
        return ".uvl";
    }

    @Override
    protected final String getPath() {
        return "src/test/resources/oneway/feature/to/decision";
    }
}
