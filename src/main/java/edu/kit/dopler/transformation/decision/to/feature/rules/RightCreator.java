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
import java.util.function.Supplier;

public class RightCreator {

    /** Create recursively a constraint from the actions */
    Constraint createRight(Set<IAction> actions) {
        Supplier<RuntimeException> exception = () -> new RuntimeException("Actions set contains no item");
        IAction first = actions.stream().findFirst().orElseThrow(exception);

        if (1 == actions.size()) {
            return createRightLiteral(first);
        }

        actions.remove(first);
        return new AndConstraint(createRightLiteral(first), createRight(actions));
    }

    private Constraint createRightLiteral(IAction iAction) {
        return switch (iAction) {
            case BooleanEnforce booleanEnforce -> new LiteralConstraint(booleanEnforce.getDecision().getDisplayId());
            case EnumEnforce enumEnforce -> new LiteralConstraint(enumEnforce.getValue().toString());
            case DisAllows disAllows -> new NotConstraint(new LiteralConstraint(disAllows.getDecision().getDisplayId()));
            case null, default -> throw new UnexpectedTypeException(iAction);
        };
    }

}
