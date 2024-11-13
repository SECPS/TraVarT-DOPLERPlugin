package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.constraint.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

/** Implementation for {@link DnfSimplifier} */
public class DnfSimplifierImpl implements DnfSimplifier {

    private static final String UNEXPECTED_TYPE = "Unexpected type";

    @Override
    public List<List<Constraint>> simplifyDnf(Constraint constraint) {
        List<List<Constraint>> dnf = new ArrayList<>();

        if (constraint instanceof AndConstraint) {
            //Early return of dnf is only an AND
            List<Constraint> conjunction = new ArrayList<>();
            dnf.add(conjunction);
            AndConstraint andConstraint = (AndConstraint) constraint;
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
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            handleBranchInConjunction(andConstraint.getLeft(), conjunction);
            handleBranchInConjunction(andConstraint.getRight(), conjunction);
        } else if (constraint instanceof NotConstraint || constraint instanceof LiteralConstraint ||
                constraint instanceof ExpressionConstraint) {
            conjunction.add(constraint);
        } else {
            throw new UnexpectedTypeException(constraint);
        }
    }

    private List<List<Constraint>> convertDnfToList(Constraint constraint) {
        List<List<Constraint>> dnf = new ArrayList<>();
        Stack<Constraint> stack = new Stack<>();
        stack.push(constraint);
        while (!stack.empty()) {
            Constraint currentConstraint = stack.pop();
            if (currentConstraint instanceof OrConstraint) {
                OrConstraint orConstraint = (OrConstraint) currentConstraint;
                handleBranchInDisjunction(orConstraint.getLeft(), stack, dnf);
                handleBranchInDisjunction(orConstraint.getRight(), stack, dnf);
            } else {
                throw new RuntimeException(UNEXPECTED_TYPE);
            }
        }
        return dnf;
    }

    private void handleBranchInDisjunction(Constraint constraint, Stack<Constraint> stack, List<List<Constraint>> dnf) {
        if (constraint instanceof OrConstraint) {
            stack.push(constraint);
        } else if (constraint instanceof AndConstraint) {
            dnf.add(createConjunction(constraint));
        } else if (constraint instanceof NotConstraint || constraint instanceof LiteralConstraint ||
                constraint instanceof ExpressionConstraint) {
            List<Constraint> list = new ArrayList<>();
            list.add(constraint);
            dnf.add(list);
        } else {
            throw new RuntimeException(UNEXPECTED_TYPE);
        }
    }

    private List<Constraint> createConjunction(Constraint constraint) {
        List<Constraint> conjunction = new ArrayList<>();

        Stack<Constraint> stack = new Stack<>();
        stack.push(constraint);

        while (!stack.empty()) {
            Constraint current = stack.pop();

            if (current instanceof AndConstraint) {
                AndConstraint andConstraint = (AndConstraint) current;
                handleBranchInConjunction(andConstraint.getLeft(), stack, conjunction);
                handleBranchInConjunction(andConstraint.getRight(), stack, conjunction);
            } else {
                throw new RuntimeException(UNEXPECTED_TYPE);
            }
        }

        return conjunction;
    }

    private static void handleBranchInConjunction(Constraint constraint, Stack<Constraint> stack,
                                                  List<Constraint> conjunction) {
        if (constraint instanceof AndConstraint) {
            stack.push(constraint);
        } else if (constraint instanceof LiteralConstraint || constraint instanceof ExpressionConstraint ||
                constraint instanceof NotConstraint) {
            conjunction.add(constraint);
        } else {
            throw new RuntimeException(UNEXPECTED_TYPE);
        }
    }

    /** Simplifies the given dnf with three rules */
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

    /** Remove conjunctions that are always false */
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
