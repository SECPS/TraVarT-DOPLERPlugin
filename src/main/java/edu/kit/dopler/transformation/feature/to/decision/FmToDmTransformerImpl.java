package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;

import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ONE_WAY;
import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ROUNDTRIP;

/** Implementation of {@link FmToDmTransformer} */
public class FmToDmTransformerImpl implements FmToDmTransformer {

    private final FeatureAndGroupHandler featureAndGroupHandler;
    private final ConstraintHandler constraintHandler;
    private final AttributeHandler attributeHandler;

    @Inject
    FmToDmTransformerImpl(FeatureAndGroupHandler featureAndGroupHandler, ConstraintHandler constraintHandler,
                          AttributeHandler attributeHandler) {
        this.featureAndGroupHandler = featureAndGroupHandler;
        this.constraintHandler = constraintHandler;
        this.attributeHandler = attributeHandler;
    }

    @Override
    public Dopler transform(FeatureModel featureModel, IModelTransformer.STRATEGY level) {
        Dopler decisionModel = new Dopler();

        Feature rootFeature = featureModel.getRootFeature();
        if (ONE_WAY == level) {
            //Do not keep root feature
            featureAndGroupHandler.handleFeature(rootFeature, decisionModel, featureModel, ONE_WAY);
        } else if (ROUNDTRIP == level) {
            //Keep root feature in DM
            Group group = new Group(Group.GroupType.MANDATORY);
            group.getFeatures().add(rootFeature);
            featureAndGroupHandler.handleGroup(featureModel, decisionModel, group, ROUNDTRIP);
        }

        attributeHandler.handleAttributes(decisionModel, featureModel, level);
        constraintHandler.handleOwnConstraints(featureModel, decisionModel);

        return decisionModel;
    }
}
