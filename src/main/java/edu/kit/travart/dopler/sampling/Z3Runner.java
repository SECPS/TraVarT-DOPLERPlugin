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
package edu.kit.travart.dopler.sampling;

import java.util.Scanner;
import java.util.stream.Stream;

/** This interface is responsible for running th z3 sat solver. */
public interface Z3Runner {

    /**
     * Starts a process of the local Z3 Solver and feeds him of SMT stream.
     *
     * @param z3Input String stream that is the input for the z3 solver
     *
     * @return Output of the solver
     */
    Scanner runZ3(Stream<String> z3Input);
}
