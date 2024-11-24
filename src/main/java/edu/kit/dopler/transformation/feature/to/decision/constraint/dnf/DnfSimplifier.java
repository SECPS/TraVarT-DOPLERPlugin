package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

import java.util.List;

/**
 * Contains methods to work on DNFs.
 */
public interface DnfSimplifier {

    /**
     * Simplifies a DNF.
     *
     * @param constraint DNF as {@link Constraint} to simplify.
     *
     * @return Simplified DNF. The inner lists are conjunctions. The outer list is the disjunction.
     */
    List<List<Constraint>> simplifyDnf(Constraint constraint);
}
