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
