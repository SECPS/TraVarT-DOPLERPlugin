package edu.kit.dopler.transformation.decision.to.feature;

import com.google.inject.Inject;
import de.vill.model.Feature;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.CanNotBeTranslatedException;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.Optional;
import java.util.Set;

/** Implementation of {@link ParentFinder}. */
public class ParentFinderImpl implements ParentFinder {

    private final FeatureFinder featureFinder;

    @Inject
    ParentFinderImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    @Override
    public Optional<Feature> getParentFromVisibility(Set<Feature> allFeatures, Feature feature,
                                                     IExpression visibility) {

        //covers 'true'
        if (visibility instanceof BooleanLiteralExpression booleanLiteralExpression &&
                booleanLiteralExpression.getLiteral()) {
            return Optional.empty(); //Visibility is `true`. The root is the parent.
        }

        //covers 'getValue(someDecision) = someValue'
        if (visibility instanceof Equals equals) {
            IExpression left = equals.getLeftExpression();
            IExpression right = equals.getRightExpression();

            if (left instanceof DecisionValueCallExpression decisionValueCallExpression) {
                IDecision<?> decision = decisionValueCallExpression.getDecision();

                if (right instanceof BooleanLiteralExpression) {
                    return Optional.of(
                            featureFinder.findFeatureByName(allFeatures, decision.getDisplayId()).orElseThrow());
                } else if (right instanceof EnumeratorLiteralExpression enumeratorLiteralExpression) {
                    String value = enumeratorLiteralExpression.getEnumerationLiteral().getValue();
                    return Optional.of(featureFinder.findFeatureByName(allFeatures, value).orElseThrow());
                }
            }
        }

        throw new CanNotBeTranslatedException(visibility);
    }
}
