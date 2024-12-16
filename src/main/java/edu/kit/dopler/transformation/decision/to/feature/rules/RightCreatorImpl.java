package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import edu.kit.dopler.model.BooleanEnforce;
import edu.kit.dopler.model.DisAllows;
import edu.kit.dopler.model.EnumEnforce;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Set;

public class RightCreatorImpl implements RightCreator {

    /** Create recursively a constraint from the actions */
    @Override
    public Constraint createRight(Set<IAction> actions) {
        IAction firstAction = actions.stream().findFirst().orElseThrow();

        if (1 == actions.size()) {
            return createRightLiteral(firstAction);
        }

        actions.remove(firstAction);
        return new AndConstraint(createRightLiteral(firstAction), createRight(actions));
    }

    /** Create a single constraint from the given {@link IAction}. */
    private Constraint createRightLiteral(IAction action) {
        return switch (action) {
            case BooleanEnforce booleanEnforce -> new LiteralConstraint(booleanEnforce.getDecision().getDisplayId());
            case EnumEnforce enumEnforce -> new LiteralConstraint(enumEnforce.getValue().toString());
            case DisAllows disAllows ->
                    new NotConstraint(new LiteralConstraint(disAllows.getDecision().getDisplayId()));
            case null, default -> throw new UnexpectedTypeException(action);
        };
    }
}
