/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.transformation.oneway.DecisionModelOneWayTransformer;
import at.jku.cps.travart.dopler.transformation.roundtrip.DecisionModelRoundtripTransformer;
import de.vill.model.FeatureModel;

public class DecisionModelTransformer implements IModelTransformer<IDecisionModel> {

	private final DecisionModelRoundtripTransformer decisionModelRoundtripTransformer = new DecisionModelRoundtripTransformer();
	private final DecisionModelOneWayTransformer decisionModelOneWayTransformer = new DecisionModelOneWayTransformer();

	@Override
	public FeatureModel transform(final IDecisionModel model, final String modelName, final STRATEGY level)
			throws NotSupportedVariabilityTypeException {
		if (level == STRATEGY.ROUNDTRIP) {
		return decisionModelRoundtripTransformer.transform(model);
		}
		return decisionModelOneWayTransformer.transform(model, modelName);
	}

	@Override
	public IDecisionModel transform(final FeatureModel model, final String modelName, final STRATEGY level)
			throws NotSupportedVariabilityTypeException {
		if (level == STRATEGY.ROUNDTRIP) {
		return decisionModelRoundtripTransformer.transform(model);
		}
		return decisionModelOneWayTransformer.transform(model, modelName);
	}

}
