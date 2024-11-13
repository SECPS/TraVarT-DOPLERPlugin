package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

import java.util.List;

/**
 * Converts a DNF in form of a double list to a tree.
 */
public interface DnfToTreeConverter {

    /**
     * Converts a DNF from a {@link List} constaining {@link List}s to a tree.
     *
     * @param dnf DNF to convert as a {@link List}.
     *
     * @return DNF as a tree.
     */
    Constraint createDnfFromList(List<List<Constraint>> dnf);
}
