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
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 *******************************************************************************/
package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.IDeserializer;
import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.common.IPlugin;
import at.jku.cps.travart.core.common.IPrettyPrinter;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.core.common.IStatistics;
import edu.kit.dopler.injection.Injector;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.Transformer;
import org.pf4j.Extension;

import java.util.List;

/**
 * Implementation of {@link IPlugin}  with the {@link Dopler} model as type variable.
 */
@Extension
public class DoplerPlugin implements IPlugin<Dopler> {

    private static final String ID = "dopler-decision-plugin";

    private final Injector injector;

    /**
     * Constructor of {@link DoplerPlugin}:
     */
    public DoplerPlugin() {
        injector = new Injector();
    }

    @Override
    public IModelTransformer<Dopler> getTransformer() {
        return injector.getInstance(Transformer.class);
    }

    @Override
    public IDeserializer<Dopler> getDeserializer() {
        return injector.getInstance(DoplerDeserializer.class);
    }

    @Override
    public IStatistics<Dopler> getStatistics() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ISerializer<Dopler> getSerializer() {
        return injector.getInstance(DoplerSerializer.class);
    }

    @Override
    public IPrettyPrinter<Dopler> getPrinter() {
        return injector.getInstance(DoplerPrettyPrinter.class);
    }

    @Override
    public String getName() {
        return "Decision-Oriented Product Line Engineering for effective Reuse";
    }

    @Override
    public String getAbbreviation() {
        return "DOPLER";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return List.of(new CsvFormat().extension());
    }
}