package at.jku.cps.travart.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.transformation.oneway.DecisionModelOneWayTransformer;
import at.jku.cps.travart.dopler.transformation.roundtrip.DecisionModelRoundtripTransformer;
import de.vill.model.FeatureModel;

public class DecisionModelTransformer implements IModelTransformer<IDecisionModel> {

	private final DecisionModelRoundtripTransformer decisionModelRoundtripTransformer = new DecisionModelRoundtripTransformer();
	private final DecisionModelOneWayTransformer decisionModelOneWayTransformer = new DecisionModelOneWayTransformer();

	@Override
	public FeatureModel transform(final IDecisionModel model, final String modelName, final TRANSFORMATION_LEVEL level)
			throws NotSupportedVariabilityTypeException {
		if (level == TRANSFORMATION_LEVEL.ROUNDTRIP) {
		this.dm = model;
		this.fm = new FeatureModel();
		try {
			TransformDMtoFMUtil.createFeatures(fm,dm);
			TransformDMtoFMUtil.createFeatureTree(fm,dm);
			TransformDMtoFMUtil.createConstraints(fm,dm);
			TraVarTUtils.deriveFeatureModelRoot(fm.getFeatureMap(), "VirtualRoot");
			return fm;
		} catch (CircleInConditionException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
		return decisionModelOneWayTransformer.transform(model, modelName);
	}

	@Override
	public IDecisionModel transform(final FeatureModel model, final String modelName, final TRANSFORMATION_LEVEL level)
			throws NotSupportedVariabilityTypeException {
		if (level == TRANSFORMATION_LEVEL.ROUNDTRIP) {
		try {
			factory = DecisionModelFactory.getInstance();
			dm = factory.create();
			dm.setName(fm.getRootFeature().getFeatureName());
			TransformFMtoDMUtil.convertFeature(factory,dm,fm.getRootFeature());
			TransformFMtoDMUtil.convertConstraints(factory,dm,fm,fm.getConstraints());
			TransformFMtoDMUtil.convertVisibilityCustomProperties(dm,fm.getFeatureMap().values());
			return dm;
		} catch (CircleInConditionException | ConditionCreationException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
		return decisionModelOneWayTransformer.transform(model, modelName);
	}

}
