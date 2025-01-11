package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.Decision;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.Rule;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;

import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY;

/**
 * This interface is responsible for creating the {@link Decision}s. The rules are only later filled into the model (see
 * {@link ConstraintHandler}). How it works: for each group in the {@link FeatureModel} one or more {@link Decision}s
 * (depending on the type of the group) are created.
 */
public interface FeatureAndGroupHandler {

    /**
     * Goes through the entire tree of the given {@link Feature}, creates {@link Rule}s and adds them to the given
     * {@link Dopler} model.
     *
     * @param featureModel  {@link FeatureModel} that contains the given {@link Feature}
     * @param decisionModel {@link Dopler} model that will contain the newly created {@link IDecision}s
     * @param feature       Root {@link Feature} of the tree
     * @param level         {@link STRATEGY} that will be used
     */
    void handleFeature(FeatureModel featureModel, Dopler decisionModel, Feature feature, STRATEGY level);

    /**
     * Goes through the entire tree of the given {@link Group}, creates {@link Rule}s and adds them to the given
     * {@link Dopler} model.
     *
     * @param featureModel  {@link FeatureModel} that contains the given {@link Group}
     * @param decisionModel {@link Dopler} model that will contain the newly created {@link IDecision}s
     * @param group         Root {@link Group} of the tree
     * @param level         {@link STRATEGY} that will be used
     */
    void handleGroup(FeatureModel featureModel, Dopler decisionModel, Group group, STRATEGY level);
}
