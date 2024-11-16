package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.transformation.util.MyUtil;
import de.vill.model.Feature;

class IdHandler {

    /**
     * Resolves the id of the given decision. The id changes, if a decision or value with this name already exists.
     */
    String resolveId(IDecisionModel decisionModel, Feature feature) {
        String featureName = feature.getFeatureName();

        //If decision or value already exists then append Type to the id.
        boolean decisionOrValueAlreadyExists = MyUtil.findDecisionById(decisionModel, featureName).isPresent() ||
                MyUtil.findDecisionByValue(decisionModel, featureName).isPresent();

        return decisionOrValueAlreadyExists ? featureName + "Type" : featureName;
    }
}
