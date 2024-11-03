package at.jku.cps.travart.dopler.transformation.oneway;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.io.DecisionModelSerializer;
import at.jku.cps.travart.dopler.printer.DoplerPrettyPrinter;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FeatureToDecisionModelTest extends TransformationTest {

    private static final String STANDARD_MODEL_NAME = "Some name";

    /**
     * Transforms the given UVL model to a DOPLER model.
     */
    @Override
    protected String transform(Path model) throws NotSupportedVariabilityTypeException, IOException {
        UVLModelFactory uvlModelFactory = new UVLModelFactory();
        FeatureModel featureModel = uvlModelFactory.parse(Files.readString(model));
        OneWayTransformer oneWayTransformer = new OneWayTransformer();
        IDecisionModel decisionModel = oneWayTransformer.transform(featureModel, STANDARD_MODEL_NAME);
        DoplerPrettyPrinter doplerPrettyPrinter = new DoplerPrettyPrinter(new DecisionModelSerializer());
        return doplerPrettyPrinter.toText(decisionModel);
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
