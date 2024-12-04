package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;

/** This interface is responsible for creating the identifier of {@link IDecision}s. */
public interface IdHandler {

    /**
     * Creates an identifier for a {@link IDecision} with the given {@link Feature}.
     *
     * @param decisionModel {@link Dopler} model that will contain the {@link IDecision}
     * @param feature       {@link Feature} from which the {@link IDecision} is created
     *
     * @return Identifier of the {@link IDecision}
     */
    String resolveId(Dopler decisionModel, Feature feature);
}
