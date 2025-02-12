package edu.kit.travart.dopler.sampling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Stream;

public class Z3Runner {

    /** You have to put the z3.exe in the z3 folder. */
    private static final String Z3_EXE = "z3" + File.separator + "z3.exe";

    /**
     * Starts a process of the local Z3 Solver and feeds him of SMT stream.
     *
     * @param z3Input String stream that is the input for the z3 solver
     *
     * @return Output of the Solver
     */
    Scanner runZ3(Stream<String> z3Input) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(Z3_EXE, "-in", "-smt2");

        try {
            Process process = processBuilder.start();

            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            for (String singleInput : z3Input.toList()) {
                writer.write(singleInput);
                writer.newLine();
            }
            writer.close();
            return new Scanner(process.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new SatException(ioException);
        }
    }
}
