package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.rule.*;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ParenthesisConstraint;

import java.util.List;
import java.util.Optional;

/** Implementation of {@link TreeToDnfConverter} */
public class TreeToDnfConverterImpl implements TreeToDnfConverter {

    private final UnwantedConstraintsReplacer replacer;
    private final DnfSimplifier dnfSimplifier;

    private static final List<Rule> RULES =
            List.of(new NotNotRule(), new MorgenOrRule(), new MorgenAndRule(), new DistributiveLeftRule(),
                    new DistributiveRightRule());

    /** Constructor of {@link TreeToDnfConverterImpl} */
    public TreeToDnfConverterImpl() {
        replacer = new UnwantedConstraintsReplacerImpl();
        dnfSimplifier = new DnfSimplifierImpl();
    }

    @Override
    public List<List<Constraint>> convertToDnf(Constraint constraint) {
        Constraint clonedConstraint = constraint.clone();

        Constraint sanitisedConstraint = replacer.replaceUnwantedConstraints(clonedConstraint);
        ParenthesisConstraint parenthesisConstraint = new ParenthesisConstraint(sanitisedConstraint);

        for (int i = 0; i < getDepth(constraint); i++) {
            convertRecursive(parenthesisConstraint);
        }

        return dnfSimplifier.simplifyDnf(parenthesisConstraint.getContent());
    }

    /**
     * Goes through the given {@link Constraint} and applies the {@link Rule}s on the entire tree.
     *
     * @param constraint Constraint to match rules against.
     */
    private void convertRecursive(Constraint constraint) {
        for (Constraint oldChild : constraint.getConstraintSubParts()) {
            Constraint newChild = applyRules(oldChild);
            constraint.replaceConstraintSubPart(oldChild, newChild);
            convertRecursive(newChild);
        }
    }

    /**
     * Find first matching {@link Rule}, apply it to the given {@link Constraint} and return the (maybe) replaced
     * {@link Constraint}.
     *
     * @param constraint {@link Constraint} to check rules against and to convert.
     *
     * @return The given constraint as it was or, if a {@link Rule} applied, a converted {@link Constraint}.
     */
    private Constraint applyRules(Constraint constraint) {
        for (Rule rule : RULES) {
            Optional<Constraint> optional = rule.replace(constraint);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return constraint;
    }

    /** Calculate the depth of the given constraint. */
    private int getDepth(Constraint constraint) {
        return constraint.getConstraintSubParts().stream().mapToInt(this::getDepth).max().orElse(1);
    }
}
