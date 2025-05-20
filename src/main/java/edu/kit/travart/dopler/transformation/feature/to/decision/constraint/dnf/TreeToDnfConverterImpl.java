/*******************************************************************************
 * SPDX-License-Identifier: MPL-2.0
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors:
 *    @author Yannick Kraml
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 *******************************************************************************/
package edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ParenthesisConstraint;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.rule.DnfRule;

import java.util.List;
import java.util.Optional;

/** Implementation of {@link TreeToDnfConverter} */
public class TreeToDnfConverterImpl implements TreeToDnfConverter {

    /** Saves the rules that are needed to convert a logical formula into DNF. */
    private final List<DnfRule> dnfRules;
    private final UnwantedConstraintsReplacer unwantedConstraintsReplacer;
    private final DnfSimplifier dnfSimplifier;

    /**
     * Constructor of {@link TreeToDnfConverterImpl}.
     *
     * @param unwantedConstraintsReplacer {@link UnwantedConstraintsReplacer}
     * @param dnfSimplifier               {@link DnfSimplifier}
     * @param dnfRules                    {@link List} of {@link DnfRule}s
     */
    public TreeToDnfConverterImpl(UnwantedConstraintsReplacer unwantedConstraintsReplacer, DnfSimplifier dnfSimplifier,
                                  List<DnfRule> dnfRules) {
        this.dnfRules = List.copyOf(dnfRules);
        this.unwantedConstraintsReplacer = unwantedConstraintsReplacer;
        this.dnfSimplifier = dnfSimplifier;
    }

    @Override
    public List<List<Constraint>> convertToDnf(Constraint constraint) {
        Constraint sanitisedConstraint = unwantedConstraintsReplacer.replaceUnwantedConstraints(constraint.clone());
        ParenthesisConstraint parenthesisConstraint = new ParenthesisConstraint(sanitisedConstraint);

        for (int i = 0; i < getDepth(parenthesisConstraint); i++) {
            convertRecursive(parenthesisConstraint);
        }

        return dnfSimplifier.simplifyDnf(parenthesisConstraint.getContent());
    }

    /**
     * Goes through the given {@link Constraint} and applies the {@link DnfRule}s on the entire tree.
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
     * Find first matching {@link DnfRule}, apply it to the given {@link Constraint} and return the (maybe) replaced
     * {@link Constraint}.
     *
     * @param constraint {@link Constraint} to check rules against and to convert.
     *
     * @return The given constraint as it was or, if a {@link DnfRule} applied, a converted {@link Constraint}.
     */
    private Constraint applyRules(Constraint constraint) {
        for (DnfRule dnfRule : dnfRules) {
            Optional<Constraint> optional = dnfRule.replace(constraint);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return constraint;
    }

    /** Calculate the depth of the given constraint. */
    private int getDepth(Constraint constraint) {
        return 1 + constraint.getConstraintSubParts().stream().mapToInt(this::getDepth).max().orElse(1);
    }
}
