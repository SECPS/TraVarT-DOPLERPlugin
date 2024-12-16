package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.decision.to.feature.rules.RuleHandler;

public class DmToFmTransformerImpl implements DmToFmTransformer {

    private final TreeBuilder treeBuilder;
    private final RuleHandler ruleHandler;
    private final TreeBeautifier treeBeautifier;

    @Inject
    public DmToFmTransformerImpl(TreeBuilder treeBuilder, RuleHandler ruleHandler, TreeBeautifier treeBeautifier) {
        this.treeBuilder = treeBuilder;
        this.ruleHandler = ruleHandler;
        this.treeBeautifier = treeBeautifier;
    }

    @Override
    public FeatureModel transform(Dopler decisionModel, String modelName, IModelTransformer.STRATEGY level) {
        Feature rootFeature = treeBuilder.buildTree(decisionModel, modelName, level);
        treeBeautifier.beautify(rootFeature);

        FeatureModel featureModel = new FeatureModel();
        featureModel.setRootFeature(rootFeature);

        ruleHandler.handleRules(decisionModel, featureModel);

        return featureModel;
    }
}
