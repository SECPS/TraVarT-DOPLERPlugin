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
package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.EquivalenceConstraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.constraint.ParenthesisConstraint;

/**
 * Implementation of {@link UnwantedConstraintsReplacer}. Replaces unwanted constraints like "=>", "<=>", "()" and
 * replaces them with different ones.
 */
public class UnwantedConstraintsReplacerImpl implements UnwantedConstraintsReplacer {

    @Override
    public Constraint replaceUnwantedConstraints(Constraint constraint) {
        ParenthesisConstraint parenthesisConstraint = new ParenthesisConstraint(constraint.clone());
        convertRecursive(parenthesisConstraint);
        return parenthesisConstraint.getContent();
    }

    /** Go through the tree of the given {@link Constraint} and replace recursively all unwanted ones. */
    private void convertRecursive(Constraint constraint) {
        for (Constraint oldChild : constraint.getConstraintSubParts()) {
            Constraint newChild = createNewChild(oldChild);
            constraint.replaceConstraintSubPart(oldChild, newChild);
            convertRecursive(newChild);
        }
    }

    private Constraint createNewChild(Constraint constraint) {
        //Replacing rules:
        // A => B  ~> !A | B
        // A <=> B ~> A => B & B => A
        // (A)     ~> A
        return switch (constraint) {
            case ImplicationConstraint implicationConstraint ->
                    new OrConstraint(new NotConstraint(implicationConstraint.getLeft()),
                            implicationConstraint.getRight());
            case EquivalenceConstraint equivalenceConstraint -> new AndConstraint(
                    new ImplicationConstraint(equivalenceConstraint.getLeft(), equivalenceConstraint.getRight()),
                    new ImplicationConstraint(equivalenceConstraint.getRight(), equivalenceConstraint.getLeft()));
            case ParenthesisConstraint parenthesisConstraint -> createNewChild(parenthesisConstraint.getContent());
            default -> constraint;
        };
    }
}
