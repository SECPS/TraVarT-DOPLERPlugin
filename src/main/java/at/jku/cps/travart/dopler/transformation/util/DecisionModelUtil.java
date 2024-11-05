package at.jku.cps.travart.dopler.transformation.util;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IDecision;

import java.util.Optional;

/** Set of utility methods for editing decision models */
public final class DecisionModelUtil {

    private DecisionModelUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(IDecisionModel decisionModel, String id) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getId().equals(id)).findFirst();
    }
}
