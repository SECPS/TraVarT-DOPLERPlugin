package at.jku.cps.travart.dopler.transformation.util;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IDecision;

import java.util.Optional;
import java.util.function.Predicate;

/** Set of utility methods for editing decision models */
public final class DMUtil {

    private DMUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(IDecisionModel decisionModel, String id) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getId().equals(id)).findFirst();
    }

    public static Optional<IDecision<?>> findDecisionByValue(IDecisionModel decisionModel, String value) {
        //Decision should have the given value in its RangeValue
        Predicate<IDecision<?>> filter = decision -> decision.getRange().stream()
                .anyMatch(abstractRangeValue -> abstractRangeValue.toString().equals(value));

        return decisionModel.getDecisions().stream().filter(filter).findFirst();
    }
}
