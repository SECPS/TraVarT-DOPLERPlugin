package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.ArrayList;
import java.util.List;

public class DmToFmTransformer {

    private final TreeBuilder treeBuilder;
    private final RuleHandler constraintHandler;

    public DmToFmTransformer() {
        treeBuilder = new TreeBuilder();
        constraintHandler = new RuleHandler();
    }

    public FeatureModel transform(Dopler decisionModel, String modelName, IModelTransformer.STRATEGY level) {
        FeatureModel featureModel = new FeatureModel();

        featureModel.setRootFeature(createFeatures(decisionModel));
        featureModel.getOwnConstraints().add(constraintHandler.createConstraints(decisionModel));


        return featureModel;
    }

    private Feature createFeatures(Dopler decisionModel) {
        List<Feature> enumFeatures = new ArrayList<>();
        List<Feature> optionalFeatures = new ArrayList<>();

        for (IDecision<?> decision : decisionModel.getDecisions()) {

            switch (decision) {
                case EnumerationDecision enumerationDecision ->
                        handleEnumDecision(optionalFeatures, enumerationDecision);
                case BooleanDecision booleanDecision -> handleBooleanDecision(optionalFeatures, booleanDecision);
                case null, default -> throw new UnexpectedTypeException(decision);
            }
        }

        return treeBuilder.buildTree(decisionModel, enumFeatures, optionalFeatures);
    }

    private void handleBooleanDecision(List<Feature> orGroups, BooleanDecision booleanDecision) {
        orGroups.add(new Feature(booleanDecision.getDisplayId()));
    }

    private void handleEnumDecision(List<Feature> enumFeatures, EnumerationDecision enumerationDecision) {

        Feature parentFeature = new Feature(enumerationDecision.getDisplayId());
        enumFeatures.add(parentFeature);

        Group group;
        if (1 == enumerationDecision.getMaxCardinality()) {
            group = new Group(Group.GroupType.ALTERNATIVE);
        } else {
            group = new Group(Group.GroupType.OR);
        }
        parentFeature.addChildren(group);

        for (EnumerationLiteral literal : enumerationDecision.getEnumeration().getEnumerationLiterals()) {
            Feature childFeature = new Feature(literal.getValue());
            group.getFeatures().add(childFeature);
        }
    }
}
