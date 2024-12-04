package edu.kit.dopler.transformation.util;

import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;

import java.util.Optional;

/** This interface is responsible for finding {@link IDecision}s inside the {@link Dopler} model */
public interface DecisionFinder {

    Optional<IDecision<?>> findDecisionById(Dopler decisionModel, String id);

    Optional<IDecision<?>> findDecisionByValue(Dopler decisionModel, String value);
}
