package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/** Implementation for {@link DnfSimplifier} */
public class DnfSimplifierImpl implements DnfSimplifier {

    @Override
    public List<List<Constraint>> simplifyDnf(Constraint constraint) {
        List<List<Constraint>> dnf = new ArrayList<>();

        if (constraint instanceof AndConstraint andConstraint) {
            //Early return of dnf is only an AND
            List<Constraint> conjunction = new ArrayList<>();
            dnf.add(conjunction);
            andConstraint.getConstraintSubParts().forEach(subPart -> handleBranchInConjunction(subPart, conjunction));
        } else if (constraint instanceof LiteralConstraint || constraint instanceof ExpressionConstraint ||
                (constraint instanceof NotConstraint notConstraint &&
                        (notConstraint.getContent() instanceof LiteralConstraint ||
                                notConstraint.getContent() instanceof ExpressionConstraint))) {
            //Early return of dnf is only a LITERAL
            List<Constraint> conjunction = new ArrayList<>();
            conjunction.add(constraint);
            dnf.add(conjunction);
        } else {
            //Convert dnf to list to better work on it
            dnf = convertDnfToList(constraint);

            //Simplify DNF
            removeLiteralsThatAppearMultipleTimesInSameConjunction(dnf);
            removeAlwaysFalseConjunctions(dnf);
            removeAlreadyCoveredConjunctions(dnf);
        }

        return dnf;
    }

    private static void handleBranchInConjunction(Constraint constraint, List<Constraint> conjunction) {
        switch (constraint) {
            case AndConstraint andConstraint -> andConstraint.getConstraintSubParts()
                    .forEach(child -> handleBranchInConjunction(child, conjunction));
            case NotConstraint notConstraint -> conjunction.add(notConstraint);
            case LiteralConstraint literalConstraint -> conjunction.add(literalConstraint);
            case ExpressionConstraint expressionConstraint -> conjunction.add(expressionConstraint);
            case null, default -> throw new UnexpectedTypeException(constraint);
        }
    }

    /** Remove conjunctions that are always false. */
    private static void removeAlwaysFalseConjunctions(List<List<Constraint>> dnf) {
        for (List<Constraint> constraints : new ArrayList<>(dnf)) {
            boolean isAlwaysFalse = false;
            for (Constraint constraint : constraints) {
                if (constraint instanceof NotConstraint &&
                        new HashSet<>(constraints).contains(((NotConstraint) constraint).getContent())) {
                    isAlwaysFalse = true;
                }

                if (new HashSet<>(constraints).contains(new NotConstraint(constraint))) {
                    isAlwaysFalse = true;
                }
            }

            if (isAlwaysFalse) {
                dnf.remove(constraints);
            }
        }
    }

    private List<List<Constraint>> convertDnfToList(Constraint constraint) {
        List<List<Constraint>> dnf = new ArrayList<>();
        Stack<Constraint> stack = new Stack<>();
        stack.push(constraint);
        while (!stack.empty()) {
            for (Constraint child : stack.pop().getConstraintSubParts()) {
                switch (child) {
                    case OrConstraint orConstraint -> stack.push(orConstraint);
                    case AndConstraint andConstraint -> dnf.add(createConjunction(andConstraint));
                    case NotConstraint notConstraint -> dnf.add(new ArrayList<>(List.of(notConstraint)));
                    case LiteralConstraint literalConstraint -> dnf.add(new ArrayList<>(List.of(literalConstraint)));
                    case ExpressionConstraint expressionConstraint ->
                            dnf.add(new ArrayList<>(List.of(expressionConstraint)));
                    case null, default -> throw new UnexpectedTypeException(child);
                }
            }
        }
        return dnf;
    }

    private List<Constraint> createConjunction(Constraint constraint) {
        List<Constraint> conjunction = new ArrayList<>();

        Stack<Constraint> stack = new Stack<>();
        stack.push(constraint);

        while (!stack.empty()) {
            Constraint current = stack.pop();
            if (current instanceof AndConstraint) {
                current.getConstraintSubParts().forEach(child -> handleBranchInConjunction(child, conjunction));
            } else {
                throw new UnexpectedTypeException(current);
            }
        }

        return conjunction;
    }

    /** Remove conjunctions that contain another conjunction completely. */
    private void removeAlreadyCoveredConjunctions(List<List<Constraint>> dnf) {
        for (List<Constraint> inner : new ArrayList<>(dnf)) {
            for (List<Constraint> outer : new ArrayList<>(dnf)) {
                if (!inner.equals(outer) && new HashSet<>(inner).containsAll(outer)) {
                    dnf.remove(inner);
                }
            }
        }
    }

    /** Remove literals from conjunctions, that appear more than once. These literals are unnecessary. */
    private void removeLiteralsThatAppearMultipleTimesInSameConjunction(List<List<Constraint>> dnf) {
        for (List<Constraint> conjunction : new ArrayList<>(dnf)) {
            Set<Constraint> set = new LinkedHashSet<>(conjunction);
            conjunction.clear();
            conjunction.addAll(set);
        }
    }
}
