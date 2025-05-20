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
package edu.kit.travart.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.io.DecisionModelWriter;
import edu.kit.dopler.model.Dopler;
import edu.kit.travart.dopler.plugin.DoplerPlugin;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ONE_WAY;
import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ROUNDTRIP;

class TransformationStressTest {

    private static final Path DATA_PATH = Path.of("src", "test", "resources", "stress", "working");
    private final DoplerPlugin plugin = new DoplerPlugin();
    private final IModelTransformer<Dopler> transformer = plugin.getTransformer();

    /**
     * Tries to transform the given model in a few different ways and checks if an error occurred.
     *
     * @param path Expected model    Real, transformed model
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("dataSourceMethod")
    @Execution(ExecutionMode.CONCURRENT)
    void tryToConvert(Path path) throws Exception {
        FeatureModel featureModel = new UVLModelFactory().parse(Files.readString(path));
        Dopler dopler1 = transformer.transform(featureModel, "", ONE_WAY);
        Dopler dopler2 = transformer.transform(featureModel, "", ROUNDTRIP);
        Dopler cleanDopler1 = new DecisionModelReader().read(new DecisionModelWriter().write(dopler1), "");
        Dopler cleanDopler2 = new DecisionModelReader().read(new DecisionModelWriter().write(dopler2), "");
        transformer.transform(cleanDopler1, "", ONE_WAY);
        transformer.transform(cleanDopler2, "", ROUNDTRIP);
    }

    private static Stream<Arguments> dataSourceMethod() throws IOException {
        //Collect files
        List<Path> pathList;
        try (Stream<Path> filePaths = Files.walk(DATA_PATH)
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".uvl"))) {
            pathList = new ArrayList<>(filePaths.toList());
        }

        //Sort by size
        pathList.sort((o1, o2) -> {
            try {
                return Long.compare(Files.size(o1), Files.size(o2));
            } catch (IOException e) {
                return 0;
            }
        });

        //Create Arguments
        List<Arguments> arguments = new ArrayList<>();
        for (Path pathToBeTransformed : pathList) {
            arguments.add(Arguments.of(pathToBeTransformed));
        }

        return arguments.stream();
    }
}
