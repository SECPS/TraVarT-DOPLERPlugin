package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;

public class DmToFmTransformer {

    private final TreeBuilder treeBuilder;
    private final RuleHandler constraintHandler;

    @Inject
    public DmToFmTransformer(TreeBuilder treeBuilder, RuleHandler constraintHandler) {
        this.treeBuilder = treeBuilder;
        this.constraintHandler = constraintHandler;
    }

    public FeatureModel transform(Dopler decisionModel, String modelName, IModelTransformer.STRATEGY level) {
        FeatureModel featureModel = new FeatureModel();

        featureModel.setRootFeature(treeBuilder.buildTree(decisionModel));
        //featureModel.getOwnConstraints().add(constraintHandler.createConstraints(decisionModel));

        return featureModel;
    }
}
