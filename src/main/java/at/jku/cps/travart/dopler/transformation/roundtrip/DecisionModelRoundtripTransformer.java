package at.jku.cps.travart.dopler.transformation.roundtrip;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.helpers.TraVarTUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.exc.ConditionCreationException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import de.vill.model.FeatureModel;

public class DecisionModelRoundtripTransformer implements IModelTransformer<IDecisionModel> {

	private FeatureModel fm;
	private IDecisionModel dm;

	@Override
	public FeatureModel transform(final IDecisionModel model, final String modelName, final STRATEGY level)
			throws NotSupportedVariabilityTypeException {
		dm = model;
		fm = new FeatureModel();
		try {
			TransformDMtoFMUtil.createFeatures(fm, dm);
			TransformDMtoFMUtil.createFeatureTree(fm, dm);
			TransformDMtoFMUtil.createConstraints(fm, dm);
			TraVarTUtils.deriveFeatureModelRoot(fm.getFeatureMap(), "VirtualRoot");
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
			dm = factory.create();
			dm.setName(fm.getRootFeature().getFeatureName());
			TransformFMtoDMUtil.convertFeature(factory, dm, fm.getRootFeature());
			TransformFMtoDMUtil.convertConstraints(factory, dm, fm, fm.getConstraints());
			TransformFMtoDMUtil.convertVisibilityCustomProperties(dm, fm.getFeatureMap().values());
			return dm;
		} catch (CircleInConditionException | ConditionCreationException | ReflectiveOperationException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

}
