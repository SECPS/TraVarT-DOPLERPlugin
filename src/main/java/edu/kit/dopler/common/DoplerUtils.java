/*******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors: 
 *    @author Fabian Eger
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 * All rights reserved
 *******************************************************************************/
package edu.kit.dopler.common;

import edu.kit.dopler.model.Decision.DecisionType;
import edu.kit.dopler.model.*;

import java.util.Set;
import java.util.stream.Collectors;

public final class DoplerUtils {

    private DoplerUtils() {
    }

    public static EnumerationLiteral getEnumerationliteral(Dopler dm, IValue<?> enumString) {
        for (Enumeration o : dm.getEnumSet()) {
            for (EnumerationLiteral enumerationLiteral : o.getEnumerationLiterals()) {
                if (enumerationLiteral.getValue().equals(enumString.getValue())) {
                    return enumerationLiteral;
                }
            }
        }

        throw new RuntimeException("Literal not found: " + enumString);
    }

    public static IDecision<?> getDecision(Dopler dm, String displayId) {
        for (IDecision<?> decision : dm.getDecisions()) {
            if (decision.getDisplayId().equals(displayId)) {
                return decision;
            }
        }
        throw new RuntimeException("Decision not found: " + displayId);
    }

    public static Set<IDecision<?>> getEnumerationDecisions(Dopler dm) {
        return dm.getDecisions().stream().filter(decision -> DecisionType.ENUM == decision.getDecisionType())
                .collect(Collectors.toSet());
    }
}
