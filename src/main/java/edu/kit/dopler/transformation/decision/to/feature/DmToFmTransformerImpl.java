package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.decision.to.feature.rules.RuleHandler;

import static edu.kit.dopler.transformation.Transformer.STANDARD_MODEL_NAME;

/** Implementation of {@link DmToFmTransformer}. */
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
    public FeatureModel transform(Dopler decisionModel, IModelTransformer.STRATEGY level) {
        Feature rootFeature = removeStandardRoot(treeBuilder.buildTree(decisionModel, level));

        treeBeautifier.beautify(rootFeature, level);

        FeatureModel featureModel = new FeatureModel();
        featureModel.setRootFeature(rootFeature);

        ruleHandler.handleRules(decisionModel, featureModel);

        return featureModel;
    }

    /** Remove root if it is the standard root */
    private static Feature removeStandardRoot(Feature rootFeature) {
        Feature feature = rootFeature;
        boolean hasNoParent = null == feature.getParentGroup();
        boolean hasStandardName = feature.getFeatureName().equals(STANDARD_MODEL_NAME);
        boolean hasOneChild = 1 == feature.getChildren().size();
        boolean hasOneGrandChild = 1 == feature.getChildren().getFirst().getFeatures().size();
        if (hasNoParent && hasStandardName && hasOneChild && hasOneGrandChild) {
            feature = feature.getChildren().getFirst().getFeatures().getFirst();
            feature.setParentGroup(null);
        }
        return feature;
    }
}
