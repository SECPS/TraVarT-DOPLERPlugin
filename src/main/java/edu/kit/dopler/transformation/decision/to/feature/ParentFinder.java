package edu.kit.dopler.transformation.decision.to.feature;

import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IExpression;

import java.util.Optional;
import java.util.Set;

/**
 * This interface is responsible for finding the correct parent of a {@link Feature} from a visibility in the
 * {@link Dopler} model.
 */
public interface ParentFinder {

    /**
     * Chooses a single {@link Feature} of the given map depending on the given visibility. This method is used to find
     * parent {@link Feature}s.
     *
     * @param allFeatures All {@link Feature}s to choose from
     * @param visibility  Visibility from which the {@link Feature} is chosen
     *
     * @return Optional that contains the parent, if one was found
     */
    Optional<Feature> getParentFromVisibility(Set<Feature> allFeatures, IExpression visibility);
}
