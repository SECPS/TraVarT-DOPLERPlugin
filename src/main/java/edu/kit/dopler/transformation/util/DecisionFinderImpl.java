package edu.kit.dopler.transformation.util;

import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.EnumerationDecision;
import edu.kit.dopler.model.EnumerationLiteral;
import edu.kit.dopler.model.IDecision;

import java.util.Optional;

/** Implementation of {@link DecisionFinder}. */
public class DecisionFinderImpl implements DecisionFinder {

    @Override
    public Optional<IDecision<?>> findDecisionById(Dopler decisionModel, String id) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getDisplayId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<IDecision<?>> findDecisionByValue(Dopler decisionModel, String value) {
        for (IDecision<?> decision : decisionModel.getDecisions()) {
            if (decision instanceof EnumerationDecision enumerationDecision) {
                //Decision should have the given value in its RangeValue
                for (EnumerationLiteral literal : enumerationDecision.getEnumeration().getEnumerationLiterals()) {
                    if (literal.getValue().equals(value)) {
                        return Optional.of(decision);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
