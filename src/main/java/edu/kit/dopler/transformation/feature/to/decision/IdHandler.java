package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.util.MyUtil;

/** This class is responsible for creating the identifier for the decisions. */
class IdHandler {

    /**
     * Resolves the id of the given decision. The id changes, if a decision or value with the same name already exists.
     */
    String resolveId(Dopler decisionModel, Feature feature) {
        String featureName = feature.getFeatureName();

        //If decision or value already exists then append "Type" to the id.
        boolean decisionOrValueAlreadyExists = MyUtil.findDecisionById(decisionModel, featureName).isPresent() ||
                MyUtil.findDecisionByValue(decisionModel, featureName).isPresent();

        return decisionOrValueAlreadyExists ? featureName + "Type" : featureName;
    }
}
