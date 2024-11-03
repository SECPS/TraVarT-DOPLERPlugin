package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.Feature;
import de.vill.model.Group;

import java.util.List;

public class FeatureHandler {

    private final GroupHandler groupHandler;

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel;

    public FeatureHandler(GroupHandler groupHandler) {
        this.groupHandler = groupHandler;
    }

    public void handleFeature(Feature feature, IDecisionModel decisionModel) {
        List<Group> groups = feature.getChildren();
        for (Group group : groups) {
            groupHandler.handleGroup(group, decisionModel);
        }

        this.decisionModel = decisionModel;
    }
}
