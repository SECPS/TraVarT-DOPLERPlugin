package at.jku.cps.travart.dopler.transformation.util;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
public interface ITransformer {

    FeatureModel transform(IDecisionModel model)
            throws NotSupportedVariabilityTypeException;

    IDecisionModel transform(FeatureModel model)
            throws NotSupportedVariabilityTypeException;

}
