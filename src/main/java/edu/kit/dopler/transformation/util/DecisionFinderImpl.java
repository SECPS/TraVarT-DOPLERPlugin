package edu.kit.dopler.transformation.util;

import edu.kit.dopler.model.*;

import java.util.Optional;
import java.util.function.Predicate;

/** Implementation of {@link DecisionFinder}. */
public class DecisionFinderImpl implements DecisionFinder {

    @Override
    public Optional<IDecision<?>> findDecisionById(Dopler decisionModel, String id) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getDisplayId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<IDecision<?>> findDecisionByValue(Dopler decisionModel, String value) {
        //Decision should have the given value in its RangeValue
        Predicate<IDecision<?>> filter = decision -> {

            switch (decision) {
                case EnumerationDecision enumerationDecision -> {
                    Enumeration enumeration = enumerationDecision.getEnumeration();
                    for (EnumerationLiteral literal : enumeration.getEnumerationLiterals()) {
                        if (literal.getValue().equals(value)) {
                            return true;
                        }
                    }
                }
                case null, default -> {
                    return false;
                }
            }

            return false;
        };

        return decisionModel.getDecisions().stream().filter(filter).findFirst();
    }
}
