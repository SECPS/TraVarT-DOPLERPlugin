package at.jku.cps.travart.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.transformation.oneway.DecisionModelOneWayTransformer;
import at.jku.cps.travart.dopler.transformation.roundtrip.DecisionModelRoundtripTransformer;
import de.vill.model.FeatureModel;

public class DecisionModelTransformer implements IModelTransformer<IDecisionModel> {
	private DecisionModel dm;


	private final DecisionModelRoundtripTransformer decisionModelRoundtripTransformer = new DecisionModelRoundtripTransformer();
	private final DecisionModelOneWayTransformer decisionModelOneWayTransformer = new DecisionModelOneWayTransformer();

	@Override
	public FeatureModel transform(final IDecisionModel model, final String modelName, final TRANSFORMATION_LEVEL level)
			throws NotSupportedVariabilityTypeException {
		if (level == TRANSFORMATION_LEVEL.ROUNDTRIP) {
		return decisionModelRoundtripTransformer.transform(model);
		}
		return decisionModelOneWayTransformer.transform(model, modelName);
	}

	@Override
	public IDecisionModel transform(final FeatureModel model, final String modelName, final TRANSFORMATION_LEVEL level)
			throws NotSupportedVariabilityTypeException {
		if (level == TRANSFORMATION_LEVEL.ROUNDTRIP) {
		return decisionModelRoundtripTransformer.transform(model);
		}
		return decisionModelOneWayTransformer.transform(model, modelName);
	}

}
