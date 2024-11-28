package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ActionCreatorImpl;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConditionCreatorImpl;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandlerImpl;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.*;

/** This class is responsible for tranforming a {@link FeatureModel} into a {@link Dopler} model. */
public class FmToDmTransformer {

    private final FeatureAndGroupHandler featureAndGroupHandler;
    private final ConstraintHandler constraintHandler;
    private final AttributeHandler attributeHandler;

    /**
     * Constructor of {@link FmToDmTransformer}
     */
    public FmToDmTransformer() {
        ConditionCreatorImpl conditionCreator = new ConditionCreatorImpl();
        ActionCreatorImpl actionCreator = new ActionCreatorImpl();
        constraintHandler = new ConstraintHandlerImpl(
                new TreeToDnfConverterImpl(new UnwantedConstraintsReplacerImpl(), new DnfSimplifierImpl()),
                new DnfToTreeConverterImpl(), actionCreator, conditionCreator, new DnfAlwaysTrueAndFalseRemoverImpl());
        featureAndGroupHandler = new FeatureAndGroupHandlerImpl(new VisibilityHandler(), new IdHandler());
        attributeHandler = new AttributeHandlerImpl(conditionCreator);
    }

    /**
     * Transforms the given {@link FeatureModel} into a {@link Dopler} model.
     *
     * @param featureModel {@link FeatureModel} to transform
     * @param modelName    Name for the newly created {@link Dopler} model
     * @param level        Strategy for the transformation: oneway or round-trip
     *
     * @return Newly created {@link Dopler} model with the given name
     */
    public Dopler transform(FeatureModel featureModel, String modelName, IModelTransformer.STRATEGY level) {
        Dopler decisionModel = new Dopler();
        decisionModel.setName(modelName);

        if (IModelTransformer.STRATEGY.ROUNDTRIP == level) {
            throw new RuntimeException("ROUNDTRIP not implemented");
        }

        Feature rootFeature = featureModel.getRootFeature();
        featureAndGroupHandler.handleFeature(rootFeature, decisionModel, featureModel);
        attributeHandler.handleAttributes(decisionModel, featureModel);
        constraintHandler.handleOwnConstraints(featureModel, decisionModel);

        return decisionModel;
    }
}
