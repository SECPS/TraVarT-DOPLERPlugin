package at.jku.cps.travart.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.helpers.TraVarTUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.exc.ConditionCreationException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import de.vill.model.FeatureModel;

@SuppressWarnings({ })
public class DecisionModelTransformer implements IModelTransformer<IDecisionModel> {

	private FeatureModel fm;
	private IDecisionModel dm;
	private DecisionModelFactory factory;

	public FeatureModel transform(IDecisionModel arg0, String arg1) throws NotSupportedVariabilityTypeException {
		this.dm = arg0;
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
	}

	public IDecisionModel transform(FeatureModel arg0, String arg1) throws NotSupportedVariabilityTypeException {
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
	}


	
}
