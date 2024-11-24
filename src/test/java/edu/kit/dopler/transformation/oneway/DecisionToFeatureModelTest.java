package edu.kit.dopler.transformation.oneway;

import at.jku.cps.travart.core.sampler.DefaultCoreModelSampler;
import de.vill.model.FeatureModel;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class DecisionToFeatureModelTest extends TransformationTest<Dopler, FeatureModel> {

    private static final String STANDARD_MODEL_NAME = "Some name";
    private static final Path TEST_DATA_PATH = Path.of("src/test/resources/oneway/decision/to/feature");

    @Override
    protected String getExpectedModel(Path pathOfExpectedModel) throws IOException {
        return Files.readString(pathOfExpectedModel);
    }

    @Override
    protected String convertToModelToString(FeatureModel featureModel) {
        return featureModel.toString();
    }

    @Override
    protected Path getTestDataPath() {
        return TEST_DATA_PATH;
    }

    @Override
    protected Dopler getModelToTransform(Path pathOfModelToBeTransformed) throws Exception {
        return new DecisionModelReader().read(pathOfModelToBeTransformed);
    }

    @Override
    protected FeatureModel transformModel(Dopler modelToBeTransformed) throws Exception {
        return new OneWayTransformer().transform(modelToBeTransformed, STANDARD_MODEL_NAME);
    }

    @Override
    protected int getAmountOfConfigsOfToModel(FeatureModel featureModel) throws Exception {
        return new DefaultCoreModelSampler().sampleValidConfigurations(featureModel).size();
    }

    @Override
    protected int getAmountOfConfigsOfFromModel(Dopler dopler) {
        return Main.getAmountOfConfigs(dopler);
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
