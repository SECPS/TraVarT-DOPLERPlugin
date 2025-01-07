package edu.kit.dopler.transformation.decision.to.feature;

import de.vill.model.Feature;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;

import java.util.List;

public interface AttributeCreator {

    void handleAttributeDecisions(List<IDecision<?>> attributeDecisions, Feature rootFeature,
                                  List<IAction> allActions);
}
