package edu.kit.travart.dopler.sampling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Stream;

/** Implementation of {@link Z3Runner}. */
public class Z3RunnerImpl implements Z3Runner {

    /** You have to put the z3.exe in the z3 folder. */
    private static final String Z3_EXE = "z3" + File.separator + "z3.exe";

    @Override
    public Scanner runZ3(Stream<String> z3Input) {
        try {
            Process process = new ProcessBuilder().command(Z3_EXE, "-in", "-smt2").start();
            OutputStream out = process.getOutputStream();
            InputStream in = process.getInputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            for (String singleInput : z3Input.toList()) {
                writer.write(singleInput);
                writer.newLine();
            }

            return new Scanner(in, StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new SatException(ioException);
        }
    }
}
