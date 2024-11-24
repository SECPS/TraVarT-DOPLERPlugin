package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import de.vill.model.constraint.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
            handleBranchInConjunction(andConstraint.getLeft(), conjunction);
            handleBranchInConjunction(andConstraint.getRight(), conjunction);
        } else if (constraint instanceof NotConstraint || constraint instanceof LiteralConstraint ||
                constraint instanceof ExpressionConstraint) {
            //Early return of dnf is only a LITERAL
            List<Constraint> conjunction = new ArrayList<>();
            conjunction.add(constraint);
            dnf.add(conjunction);
        } else {
            //Convert dnf to list to better work on it
            dnf = convertDnfToList(constraint);

            //Simplify DNF
            dnf = simplify(dnf);
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

    private List<List<Constraint>> convertDnfToList(Constraint constraint) {
        List<List<Constraint>> dnf = new ArrayList<>();
        Stack<Constraint> stack = new Stack<>();
        stack.push(constraint);
        while (!stack.empty()) {
            Constraint current = stack.pop();
            if (current instanceof OrConstraint) {
                current.getConstraintSubParts().forEach(child -> handleBranchInDisjunction(child, stack, dnf));
            } else {
                throw new UnexpectedTypeException(current);
            }
        }
        return dnf;
    }

    private void handleBranchInDisjunction(Constraint constraint, Stack<Constraint> stack, List<List<Constraint>> dnf) {
        switch (constraint) {
            case OrConstraint orConstraint -> stack.push(orConstraint);
            case AndConstraint andConstraint -> dnf.add(createConjunction(andConstraint));
            case NotConstraint notConstraint -> dnf.add(new ArrayList<>(List.of(notConstraint)));
            case LiteralConstraint literalConstraint -> dnf.add(new ArrayList<>(List.of(literalConstraint)));
            case ExpressionConstraint expressionConstraint -> dnf.add(new ArrayList<>(List.of(expressionConstraint)));
            case null, default -> throw new UnexpectedTypeException(constraint);
        }
    }

    private List<Constraint> createConjunction(Constraint constraint) {
        List<Constraint> conjunction = new ArrayList<>();

        Stack<Constraint> stack = new Stack<>();
        stack.push(constraint);

        while (!stack.empty()) {
            Constraint current = stack.pop();
            if (current instanceof AndConstraint) {
                current.getConstraintSubParts().forEach(child -> handleBranchInConjunction(child, stack, conjunction));
            } else {
                throw new UnexpectedTypeException(current);
            }
        }

        return conjunction;
    }

    private static void handleBranchInConjunction(Constraint constraint, Stack<Constraint> stack,
                                                  List<Constraint> conjunction) {
        switch (constraint) {
            case AndConstraint andConstraint -> stack.push(andConstraint);
            case LiteralConstraint literalConstraint -> conjunction.add(literalConstraint);
            case ExpressionConstraint expressionConstraint -> conjunction.add(expressionConstraint);
            case NotConstraint notConstraint -> conjunction.add(notConstraint);
            case null, default -> throw new UnexpectedTypeException(constraint);
        }
    }

    /** Simplifies the given dnf with three simple rules. */
    private List<List<Constraint>> simplify(List<List<Constraint>> dnf) {
        return removeAlreadyCoveredConjunctions(removeRepeatingLiterals(removeAlwaysFalseConjunctions(dnf)));
    }

    /** Remove conjunctions that contain another conjunction completely. */
    private List<List<Constraint>> removeAlreadyCoveredConjunctions(List<List<Constraint>> dnf) {
        List<List<Constraint>> simplifiedDnf = new ArrayList<>(dnf);
        for (List<Constraint> inner : dnf) {
            for (List<Constraint> outer : dnf) {
                if (!inner.equals(outer) && new HashSet<>(inner).containsAll(outer)) {
                    simplifiedDnf.remove(inner);
                }
            }
        }
        return simplifiedDnf;
    }

    /** Remove literals that appear more than once. */
    private List<List<Constraint>> removeRepeatingLiterals(List<List<Constraint>> dnf) {
        List<List<Constraint>> simplifiedDnf = new ArrayList<>();

        for (List<Constraint> constraints : dnf) {
            List<Constraint> literals = new ArrayList<>();
            simplifiedDnf.add(literals);

            for (Constraint constraint : constraints) {
                if (!literals.contains(constraint)) {
                    literals.add(constraint);
                }
            }
        }

        return simplifiedDnf;
    }

    /** Remove conjunctions that are always false. */
    private static List<List<Constraint>> removeAlwaysFalseConjunctions(List<List<Constraint>> dnf) {
        List<List<Constraint>> simplifiedDnf = new ArrayList<>();

        for (List<Constraint> constraints : dnf) {
            boolean canBeTrue = true;
            for (Constraint constraint : constraints) {
                if (constraint instanceof NotConstraint) {
                    if (constraints.contains(((NotConstraint) constraint).getContent())) {
                        canBeTrue = false;
                    }
                } else {
                    if (constraints.contains(new NotConstraint(constraint))) {
                        canBeTrue = false;
                    }
                }
            }

            if (canBeTrue) {
                simplifiedDnf.add(constraints);
            }
        }
        return simplifiedDnf;
    }
}
