package edu.kit.dopler.transformation.decision.to.feature;

import com.google.inject.Inject;
import de.vill.model.Feature;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.Map;

public class ParentFinderImpl implements ParentFinder {

    private final FeatureFinder featureFinder;

    @Inject
    public ParentFinderImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    public Feature getParentFromVisibility(Map<Feature, IExpression> allFeatures, Feature rootFeature,
                                           IExpression visibility) {

        if (visibility instanceof BooleanLiteralExpression booleanLiteralExpression) {
            if (booleanLiteralExpression.getLiteral()) {
                return rootFeature; //Visibility is `true`. root is visibility
            }
        }

        if (visibility instanceof Equals equals) {
            IExpression left = equals.getLeftExpression();
            IExpression right = equals.getRightExpression();

            if (left instanceof DecisionValueCallExpression decisionValueCallExpression) {
                IDecision<?> decision = decisionValueCallExpression.getDecision();

                if (right instanceof BooleanLiteralExpression) {
                    return featureFinder.findFeatureByName(allFeatures.keySet(), decision.getDisplayId()).orElseThrow();
                } else if (right instanceof EnumeratorLiteralExpression enumeratorLiteralExpression) {
                    String value = enumeratorLiteralExpression.getEnumerationLiteral().getValue();
                    return featureFinder.findFeatureByName(allFeatures.keySet(), value).orElseThrow();
                }
            }
        }

        throw new RuntimeException("Visibility could not be translated");
    }
}
