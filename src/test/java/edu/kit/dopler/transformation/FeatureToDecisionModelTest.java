package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.io.DecisionModelWriter;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class FeatureToDecisionModelTest extends TransformationTest<FeatureModel, Dopler> {

    private static final String STANDARD_MODEL_NAME = "Root";
    private static final Path TEMP_PATH = Paths.get("src", "test", "resources", "oneway", ".temporary.txt");

    @Override
    protected String readToModelAsString(Path path) throws IOException {
        return TestUtils.sortDecisionModel(Files.readString(path));
    }

    @Override
    protected String readFromModelAsString(Path path) throws Exception {
        return Files.readString(path);
    }

    @Override
    protected String convertToModelToString(Dopler dopler) throws Exception {
        new DecisionModelWriter().write(dopler, TEMP_PATH);
        String result = Files.readString(TEMP_PATH); //Read temp file
        Files.writeString(TEMP_PATH, ""); //Reset temp file
        return TestUtils.sortDecisionModel(result);
    }

    @Override
    protected String convertFromModelToString(FeatureModel featureModel) {
        return featureModel.toString();
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
    protected Path getOneWayDataPath() {
        return Paths.get("src", "test", "resources", "oneway", "feature", "to", "decision");
    }

    @Override
    protected Path getRoundTripDataPath() {
        return Path.of("src", "test", "resources", "roundtrip", "feature", "to", "decision");
    }

    @Override
    protected FeatureModel getFromModelFromPath(Path path) throws Exception {
        return new UVLModelFactory().parse(Files.readString(path));
    }

    @Override
    protected Dopler getToModelFromString(String model) throws Exception {
        Files.writeString(TEMP_PATH, model);
        return new DecisionModelReader().read(TEMP_PATH);
    }

    @Override
    protected Dopler transformFromModelToToModel(FeatureModel modelToBeTransformed,
                                                 IModelTransformer.STRATEGY strategy) {
        return new Transformer().transform(modelToBeTransformed, STANDARD_MODEL_NAME, strategy);
    }

    @Override
    protected FeatureModel transformToModelToFromModel(Dopler modelToBeTransformed,
                                                       IModelTransformer.STRATEGY strategy) {
        return new Transformer().transform(modelToBeTransformed, STANDARD_MODEL_NAME, strategy);
    }

    @Override
    protected int getAmountOfConfigsOfToModel(Dopler dopler) {
        return Main.getAmountOfConfigs(dopler);
    }

    @Override
    protected int getAmountOfConfigsOfFromModel(FeatureModel featureModel) {
        //return new DefaultCoreModelSampler().sampleValidConfigurations(featureModel).size();
        return -1;
    }
}
