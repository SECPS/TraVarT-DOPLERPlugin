/*******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 *
 * Contributors: 
 *    @author Fabian Eger
 *    @author Kevin Feichtinger
 *
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

    @SuppressWarnings("rawtypes")
    public static EnumerationLiteral getEnumerationliteral(Dopler dm, IValue enumString) {
        for (Enumeration o : dm.getEnumSet()) {
            for (EnumerationLiteral enumerationLiteral : o.getEnumerationLiterals()) {
                if (enumerationLiteral.getValue().equals(enumString.getValue())) {
                    return enumerationLiteral;
                }
            }
        }

        throw new RuntimeException("Literal not found: " + enumString);
    }

    @SuppressWarnings("rawtypes")
    public static IDecision getDecision(Dopler dm, String displayId) {
        for (Object o : dm.getDecisions()) {
            IDecision decision = (IDecision) o;
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
