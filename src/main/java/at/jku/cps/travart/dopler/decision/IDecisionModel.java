/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.core.common.IValidate;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.IDecision;

import java.util.Collection;
import java.util.Map;

public interface IDecisionModel extends IValidate {

    /**
     * Returns the factoryId which created this {@link IDecisionModel}.
     *
     * @return the factoryId
     */
    String getFactoryId();

    /**
     * Returns the file location where the model is stored.
     *
     * @return the file location.
     */
    String getSourceFile();

    /**
     * Update the storage location of this {@link IDecisionModel}.
     *
     * @param sourceFile the location of this {@link IDecisionModel}.
     */
    void setSourceFile(String sourceFile);

    void setName(String name);

    String getName();

    @SuppressWarnings("rawtypes")
    void add(IDecision decision);

    void addAll(Collection<IDecision<?>> decisions);

    @SuppressWarnings("rawtypes")
    boolean remove(IDecision decision);

    void clear();

    @SuppressWarnings("rawtypes")
    IDecision get(String id);

    Collection<String> getDecisionNames();

    Collection<IDecision<?>> getDecisions();

    @SuppressWarnings("rawtypes")
    boolean contains(IDecision decision);

    boolean containsAll(Collection<IDecision<?>> decisions);

    int size();

    void reset() throws RangeValueException;

    Map<IConfigurable, Boolean> getCurrentConfiguration();

}
