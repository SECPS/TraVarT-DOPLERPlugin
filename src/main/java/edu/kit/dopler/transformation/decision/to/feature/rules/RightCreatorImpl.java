package edu.kit.dopler.transformation.decision.to.feature.rules;

import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.FeatureNotPresentException;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.*;

public class RightCreatorImpl implements RightCreator {

    private final FeatureFinder featureFinder;

    @Inject
    public RightCreatorImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    /** Create recursively a constraint from the actions */
    @Override
    public Constraint createRight(Feature root, Set<IAction> actions) {
        List<IAction> sortedActions = new ArrayList<>(actions);
        sortedActions.sort(Comparator.comparing(Object::toString));

        IAction firstAction = sortedActions.stream().findFirst().orElseThrow();

        if (1 == sortedActions.size()) {
            return createRightLiteral(root, firstAction);
        }

        sortedActions.remove(firstAction);
        return new AndConstraint(createRightLiteral(root, firstAction),
                createRight(root, new LinkedHashSet<>(sortedActions)));
    }

    /** Create a single constraint from the given {@link IAction}. */
    private Constraint createRightLiteral(Feature root, IAction action) {
        return switch (action) {
            case BooleanEnforce booleanEnforce -> handleBooleanEnforce(root, booleanEnforce);
            case EnumEnforce enumEnforce -> handleEnumEnforce(root, enumEnforce);
            case DisAllows disAllows -> handleDisAllows(root, disAllows);
            case null, default -> throw new UnexpectedTypeException(action);
        };
    }

    private NotConstraint handleDisAllows(Feature root, DisAllows disAllows) {
        String displayId = disAllows.getDisAllowValue().getValue().toString();
        Optional<Feature> feature = featureFinder.findFeatureByName(root, displayId);

        if (feature.isEmpty()) {
            throw new FeatureNotPresentException(displayId);
        }

        return new NotConstraint(new LiteralConstraint(feature.orElseThrow().getFeatureName()));
    }

    private LiteralConstraint handleEnumEnforce(Feature root, EnumEnforce enumEnforce) {
        String value = enumEnforce.getValue().toString();
        Optional<Feature> feature = featureFinder.findFeatureByName(root, value);

        if (feature.isEmpty()) {
            throw new FeatureNotPresentException(value);
        }

        return new LiteralConstraint(feature.orElseThrow().getFeatureName());
    }

    private Constraint handleBooleanEnforce(Feature root, BooleanEnforce booleanEnforce) {
        String displayId = booleanEnforce.getDecision().getDisplayId();
        Optional<Feature> feature = featureFinder.findFeatureByName(root, displayId);

        if (feature.isEmpty()) {
            throw new FeatureNotPresentException(displayId);
        }

        if (booleanEnforce.getValue() instanceof BooleanValue booleanValue && booleanValue.getValue()) {
            //decision = true
            return new LiteralConstraint(feature.get().getFeatureName());
        } else {
            //decision = false
            return new NotConstraint(new LiteralConstraint(feature.get().getFeatureName()));
        }
    }
}
