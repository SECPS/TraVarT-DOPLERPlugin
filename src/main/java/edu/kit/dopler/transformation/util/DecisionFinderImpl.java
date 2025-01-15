package edu.kit.dopler.transformation.util;

import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.EnumerationDecision;
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
            //Decision should have the given value in its RangeValue
            if (decision instanceof EnumerationDecision enumerationDecision &&
                    enumerationDecision.getEnumeration().getEnumerationLiterals().stream()
                            .anyMatch(literal -> literal.getValue().equals(value))) {
                return Optional.of(decision);
            }
        }

        return Optional.empty();
    }
}
