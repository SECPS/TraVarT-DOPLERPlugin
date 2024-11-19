package at.jku.cps.travart.dopler.transformation.oneway;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.oneway.OneWayTransformer;

import java.io.IOException;
import java.nio.file.Path;

public class DecisionToFeatureModelTest extends TransformationTest {

    private static final String STANDARD_MODEL_NAME = "Some name";

    /**
     * Transforms the given DOPLER model to a UVL model.
     */
    @Override
    protected String transform(Path model) throws NotSupportedVariabilityTypeException, IOException {
        //DecisionModelDeserializer decisionModelDeserializer = new DecisionModelDeserializer();
        //Dopler decisionModel = decisionModelDeserializer.deserializeFromFile(model);
        //OneWayTransformer oneWayTransformer = new OneWayTransformer();
        //FeatureModel featureModel = oneWayTransformer.transform(decisionModel, STANDARD_MODEL_NAME);
        //return featureModel.toString();

        return "";
    }

    @Override
    protected String getPath() {
        return "src/test/resources/oneway/decision/to/feature";
    }

    @Override
    protected String getToEnding() {
        return ".uvl";
    }

    @Override
    protected String getFromEnding() {
        return ".csv";
    }
}
