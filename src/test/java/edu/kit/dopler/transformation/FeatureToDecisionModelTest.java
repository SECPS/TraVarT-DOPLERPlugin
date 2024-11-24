package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.sampler.DefaultCoreModelSampler;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import edu.kit.dopler.io.DecisionModelWriter;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FeatureToDecisionModelTest extends TransformationTest<FeatureModel, Dopler> {

    private static final String STANDARD_MODEL_NAME = "Some name";
    private static final Path TEMP_PATH = Path.of("src/test/resources/oneway/TEMP.txt");
    private static final Path TEST_DATA_PATH = Path.of("src/test/resources/oneway/feature/to/decision");

    @Override
    protected String getExpectedModel(Path pathOfExpectedModel) throws IOException {
        return TestUtils.sortDecisionModel(Files.readString(pathOfExpectedModel));
    }

    @Override
    protected String convertToModelToString(Dopler dopler) throws Exception {
        new DecisionModelWriter().write(dopler, TEMP_PATH);
        String result = Files.readString(TEMP_PATH); //Read temp file
        Files.writeString(TEMP_PATH, ""); //Reset temp file
        return TestUtils.sortDecisionModel(result);
    }

    @Override
    protected String getToEnding() {
        return ".csv";
    }

    @Override
    protected String getFromEnding() {
        return ".uvl";
    }

    @Override
    protected Path getTestDataPath() {
        return TEST_DATA_PATH;
    }

    @Override
    protected FeatureModel getModelToTransform(Path pathOfModelToBeTransformed) throws Exception {
        return new UVLModelFactory().parse(Files.readString(pathOfModelToBeTransformed));
    }

    @Override
    protected Dopler transformModel(FeatureModel modelToBeTransformed) throws NotSupportedVariabilityTypeException {
        return new OneWayTransformer().transform(modelToBeTransformed, STANDARD_MODEL_NAME);
    }

    @Override
    protected int getAmountOfConfigsOfToModel(Dopler dopler) {
        return Main.getAmountOfConfigs(dopler);
    }

    @Override
    protected int getAmountOfConfigsOfFromModel(FeatureModel featureModel) throws Exception {
        return new DefaultCoreModelSampler().sampleValidConfigurations(featureModel).size();
    }
}
