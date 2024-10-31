/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.transformation.old.oneway;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;

public class DecisionModelOneWayTransformer implements IModelTransformer<IDecisionModel> {

	@Override
	public FeatureModel transform(final IDecisionModel model, final String modelName, final STRATEGY level)
			throws NotSupportedVariabilityTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDecisionModel transform(final FeatureModel model, final String modelName, final STRATEGY level)
			throws NotSupportedVariabilityTypeException {
		// TODO Auto-generated method stub
		return null;
	}

}
