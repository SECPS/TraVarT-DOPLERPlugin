package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.sampler.DefaultCoreModelSampler;
import de.vill.model.FeatureModel;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class DecisionToFeatureModelTest extends TransformationTest<Dopler, FeatureModel> {

    private static final String STANDARD_MODEL_NAME = "Root";

    @Override
    protected String readToModelAsString(Path path) throws IOException {
        return Files.readString(path);
    }

    @Override
    protected String readFromModelAsString(Path path) throws Exception {
        return "";
    }

    @Override
    protected String convertToModelToString(FeatureModel featureModel) {
        return featureModel.toString();
    }

    @Override
    protected String convertFromModelToString(Dopler toModel) throws Exception {
        return "";
    }

    @Override
    protected Path getOneWayDataPath() {
        return Paths.get("src", "test", "resources", "oneway", "decision", "to", "feature");
    }

    @Override
    protected Dopler getFromModelFromPath(Path path) throws Exception {
        return new DecisionModelReader().read(path);
    }

    @Override
    protected FeatureModel getToModelFromString(String model) throws Exception {
        return null;
    }

    @Override
    protected FeatureModel transformFromModelToToModel(Dopler modelToBeTransformed, IModelTransformer.STRATEGY strategy)
            throws Exception {
        return new Transformer().transform(modelToBeTransformed, STANDARD_MODEL_NAME);
    }

    @Override
    protected Dopler transformToModelToFromModel(FeatureModel modelToBeTransformed, IModelTransformer.STRATEGY strategy)
            throws Exception {
        return null;
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
    protected Path getRoundTripDataPath() {
        return null;
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
