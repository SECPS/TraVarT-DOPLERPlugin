package edu.kit.dopler.transformation.feature.to.decision;

import com.google.inject.Inject;
import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.util.DecisionFinder;

/** Implementation of {@link IdHandler}. */
public class IdHandlerImpl implements IdHandler {

    private final DecisionFinder decisionFinder;

    /** Constructor of {@link IdHandlerImpl} */
    @Inject
    public IdHandlerImpl(DecisionFinder decisionFinder) {
        this.decisionFinder = decisionFinder;
    }

    @Override
    public String resolveId(Dopler decisionModel, Feature feature) {
        String featureName = feature.getFeatureName();

        //If decision or value already exists then append "*" to the id.
        while (nameExistsAlready(decisionModel, featureName)) {
            featureName = featureName + "*";
        }

        return featureName;
    }


    private boolean nameExistsAlready(Dopler decisionModel, String featureName) {
        return decisionFinder.findDecisionById(decisionModel, featureName).isPresent() ||
                decisionFinder.findDecisionByValue(decisionModel, featureName).isPresent();
    }
}
