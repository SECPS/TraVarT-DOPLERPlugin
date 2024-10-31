/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.transformation.old;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.factory.impl.CoreModelFactory;
import at.jku.cps.travart.core.helpers.TraVarTUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.exc.ConditionCreationException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

public class DecisionModelRoundtripTransformer implements IModelTransformer<IDecisionModel> {

	@Override
	public FeatureModel transform(final IDecisionModel dm, final String modelName, final STRATEGY level)
			throws NotSupportedVariabilityTypeException {
		CoreModelFactory factory = CoreModelFactory.getInstance();
		try {
			FeatureModel fm = factory.create();
			final Feature root = factory.createFeature(modelName);
			TraVarTUtils.setAbstract(root, true);
			TraVarTUtils.addFeature(fm, root);
			TraVarTUtils.setRoot(fm, root);
			TransformDMtoFMUtil.createFeatures(fm, dm);
			TransformDMtoFMUtil.createFeatureTree(fm, dm);
			TransformDMtoFMUtil.createConstraints(fm, dm);
//			TraVarTUtils.deriveFeatureModelRoot(fm, "VirtualRoot");
			return fm;
		} catch (CircleInConditionException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

	@Override
	public IDecisionModel transform(final FeatureModel model, final String modelName, final STRATEGY level)
			throws NotSupportedVariabilityTypeException {
		try {
			DecisionModelFactory factory = DecisionModelFactory.getInstance();
			IDecisionModel dm = factory.create();
			dm.setName(TraVarTUtils.getFeatureName(TraVarTUtils.getRoot(model)));
			TransformFMtoDMUtil.convertFeature(factory, dm, TraVarTUtils.getRoot(model));
			TransformFMtoDMUtil.convertConstraints(factory, dm, model, TraVarTUtils.getOwnConstraints(model));
			TransformFMtoDMUtil.convertVisibilityCustomProperties(dm, TraVarTUtils.getFeatures(model));
			return dm;
		} catch (CircleInConditionException | ConditionCreationException | ReflectiveOperationException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

}
