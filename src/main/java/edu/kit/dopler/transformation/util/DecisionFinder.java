package edu.kit.dopler.transformation.util;

import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.EnumerationDecision;
import edu.kit.dopler.model.IDecision;

import java.util.Optional;

/** This interface is responsible for finding {@link IDecision}s inside the {@link Dopler} model */
public interface DecisionFinder {

    /**
     * Searches for a specific {@link IDecision} inside the given {@link Dopler} model. As a search parameter a
     * {@link String} value is given. This method returns the first {@link IDecision} that has the given identifier as
     * its own id.
     *
     * @param decisionModel {@link Dopler} model to search in
     * @param id            {@link String} id to search for
     *
     * @return Optional that contains an {@link IDecision} if one was found or is empty if no {@link IDecision} was
     * found
     */
    Optional<IDecision<?>> findDecisionById(Dopler decisionModel, String id);

    /**
     * Searches for a specific {@link IDecision} inside the given {@link Dopler} model. As a search parameter a
     * {@link String} value is given. This method returns the first {@link IDecision} that is a
     * {@link EnumerationDecision} and contains the given value in its range.
     *
     * @param decisionModel {@link Dopler} model to search in
     * @param value         {@link String} value to search for
     *
     * @return Optional that contains an {@link IDecision} if one was found or is empty if no {@link IDecision} was
     * found
     */
    Optional<IDecision<?>> findDecisionByValue(Dopler decisionModel, String value);
}
