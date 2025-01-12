package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.common.IPlugin;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import edu.kit.dopler.TestUtils;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.plugin.CsvFormat;
import edu.kit.dopler.plugin.DoplerPluginImpl;
import edu.kit.dopler.transformation.decision.to.feature.TreeBeautifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


class FeatureToDecisionModelTest extends TransformationTest<FeatureModel, Dopler> {

    private final IPlugin<Dopler> plugin = new DoplerPluginImpl();

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
        return TestUtils.sortDecisionModel(plugin.getSerializer().serialize(dopler));
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
        return plugin.getDeserializer().deserialize(model, new CsvFormat());
    }

    @Override
    protected Dopler transformFromModelToToModel(FeatureModel modelToBeTransformed, IModelTransformer.STRATEGY strategy)
            throws NotSupportedVariabilityTypeException {
        return plugin.getTransformer().transform(modelToBeTransformed, TreeBeautifier.STANDARD_MODEL_NAME, strategy);
    }

    @Override
    protected FeatureModel transformToModelToFromModel(Dopler modelToBeTransformed)
            throws NotSupportedVariabilityTypeException {
        return plugin.getTransformer()
                .transform(modelToBeTransformed, TreeBeautifier.STANDARD_MODEL_NAME, IModelTransformer.STRATEGY.ONE_WAY);
    }
}
