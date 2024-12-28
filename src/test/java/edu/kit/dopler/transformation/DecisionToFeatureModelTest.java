package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.sampler.DefaultCoreModelSampler;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.io.DecisionModelWriter;
import edu.kit.dopler.model.Dopler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class DecisionToFeatureModelTest extends TransformationTest<Dopler, FeatureModel> {

    @Override
    protected String readToModelAsString(Path path) throws IOException {
        return Files.readString(path);
    }

    @Override
    protected String readFromModelAsString(Path path) throws Exception {
        return TestUtils.sortDecisionModel(Files.readString(path));
    }

    @Override
    protected String convertToModelToString(FeatureModel featureModel) {
        return featureModel.toString();
    }

    @Override
    protected String convertFromModelToString(Dopler toModel) throws Exception {
        new DecisionModelWriter().write(toModel, TEMP_PATH);
        return TestUtils.sortDecisionModel(Files.readString(TEMP_PATH));
    }

    @Override
    protected Dopler getFromModelFromPath(Path path) throws Exception {
        return new DecisionModelReader().read(path);
    }

    @Override
    protected FeatureModel getToModelFromString(String model) {
        return new UVLModelFactory().parse(model);
    }

    @Override
    protected FeatureModel transformFromModelToToModel(Dopler modelToBeTransformed,
                                                       IModelTransformer.STRATEGY strategy) {
        return new Transformer().transform(modelToBeTransformed, Transformer.STANDARD_MODEL_NAME, strategy);
    }

    @Override
    protected Dopler transformToModelToFromModel(FeatureModel modelToBeTransformed) {
        return new Transformer().transform(modelToBeTransformed, Transformer.STANDARD_MODEL_NAME,
                IModelTransformer.STRATEGY.ONE_WAY);
    }

    @Override
    protected int getAmountOfConfigsOfToModel(FeatureModel featureModel) throws Exception {
        return new DefaultCoreModelSampler().sampleValidConfigurations(featureModel).size();
    }

    @Override
    protected int getAmountOfConfigsOfFromModel(Dopler dopler) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected Path getRoundTripDataPath() {
        return Path.of("src", "test", "resources", "roundtrip", "decision", "to", "feature");
    }

    @Override
    protected Path getOneWayDataPath() {
        return Paths.get("src", "test", "resources", "oneway", "decision", "to", "feature");
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
