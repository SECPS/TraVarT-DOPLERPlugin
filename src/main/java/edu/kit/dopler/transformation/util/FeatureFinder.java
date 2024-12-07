package edu.kit.dopler.transformation.util;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;

import java.util.Collection;
import java.util.Optional;

/** This interface is responsible for finding {@link Feature}s inside the {@link FeatureModel}. */
public interface FeatureFinder {

    /**
     * Finds for the given {@link Feature} the first parent that is NOT mandatory. A {@link Feature} is considered
     * mandatory of all its parent {@link Group}s are mandatory.
     *
     * @param featureModel {@link FeatureModel} that contains the given feature
     * @param feature      {@link Feature} from which a non-mandatory parent should be found
     *
     * @return Optional containing nothing if there is no non-mandatory parent or containing the non-mandatory parent if
     * one was found
     */
    Optional<Feature> findFirstNonMandatoryParent(FeatureModel featureModel, Feature feature);

    /**
     * This method is there for replacing {@link LiteralConstraint}s inside more complex constraints. This is needed
     * because some {@link LiteralConstraint}s are always true because the linked {@link Feature} itself and all its
     * parents are mandatory.
     * <p>
     * How it works: the code finds for the given {@link LiteralConstraint} the {@link Feature} that is linked to it.
     * Then it searches the first non-mandatory parent of the found {@link Feature}. At last, it returns a new
     * {@link LiteralConstraint} with the name of the non-mandatory parent.
     *
     * @param featureModel      {@link FeatureModel} containing the {@link Feature} and {@link LiteralConstraint}
     * @param literalConstraint {@link LiteralConstraint} to replace
     *
     * @return Optional containing nothing if there is no non-mandatory parent or containing the non-mandatory parent as
     * a {@link LiteralConstraint} if one was found
     */
    Optional<LiteralConstraint> findFirstNonMandatoryParent(FeatureModel featureModel,
                                                            LiteralConstraint literalConstraint);

    Optional<Feature> findFeatureByName(Collection<Feature> features, String displayId);
}
