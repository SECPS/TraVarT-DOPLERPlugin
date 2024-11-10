package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.transformation.util.DMUtil;
import de.vill.model.Feature;

public class IdHandler {

    /**
     * Resolves the id of the given decision. The id changes, if a decision with this name already exists.
     */
    String resolveId(IDecisionModel decisionModel, Feature feature) {
        String id;
        if (DMUtil.findDecisionById(decisionModel, feature.getFeatureName()).isPresent()) {
            id = feature.getFeatureName() + "Type";
        } else {
            id = feature.getFeatureName();
        }
        return id;
    }
}
