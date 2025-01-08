package edu.kit.dopler.transformation.feature.to.decision;

import com.google.inject.Inject;
import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.util.DecisionFinder;

/** Implementation of {@link IdHandler}. */
public class IdHandlerImpl implements IdHandler {

    private final DecisionFinder decisionFinder;

    @Inject
    IdHandlerImpl(DecisionFinder decisionFinder) {
        this.decisionFinder = decisionFinder;
    }

    @Override
    public String resolveId(Dopler decisionModel, Feature feature) {
        StringBuilder featureNameBuilder = new StringBuilder(feature.getFeatureName());

        //If decision or value already exists then append "*" to the id.
        while (nameExistsAlready(decisionModel, featureNameBuilder.toString())) {
            featureNameBuilder.append("*");
        }

        return featureNameBuilder.toString();
    }

    private boolean nameExistsAlready(Dopler decisionModel, String featureName) {
        return decisionFinder.findDecisionById(decisionModel, featureName).isPresent() ||
                decisionFinder.findDecisionByValue(decisionModel, featureName).isPresent();
    }
}
