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
package edu.kit.dopler.model;

import java.util.Collections;
import java.util.Set;

public class Assets {

    private String description;
    private IExpression inclusionCondition;
    private Set<Assets> assets = Collections.emptySet();
    public Assets(String description, IExpression inclusionCondition) {
        this.description = description;
        this.inclusionCondition = inclusionCondition;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    IExpression getInclusionCondition() {
        return inclusionCondition;
    }

    public void setInclusionCondition(IExpression inclusionCondition) {
        this.inclusionCondition = inclusionCondition;
    }

    public Set<Assets> getAssets() {
        return assets;
    }

    public void setAssets(Set<Assets> assets) {
        this.assets = assets;
    }
}
