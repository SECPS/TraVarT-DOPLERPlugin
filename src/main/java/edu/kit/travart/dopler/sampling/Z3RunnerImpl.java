package edu.kit.travart.dopler.sampling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/** Implementation of {@link Z3Runner}. */
public class Z3RunnerImpl implements Z3Runner {

    /** You have to put the z3.exe in the z3 folder. */
    private static final String Z3_EXE = "z3" + File.separator + "z3.exe";

    @Override
    public Scanner runZ3(Stream<String> z3Input) {
        Process process = startZ3Process();
        inputData(z3Input, process.getOutputStream());
        return new Scanner(process.getInputStream(), StandardCharsets.UTF_8);
    }

    private static Process startZ3Process() {
        Process process;
        try {
            process = new ProcessBuilder().command(Z3_EXE, "-in", "-smt2").start();
        } catch (IOException ioException) {
            throw new SatException(ioException);
        }
        return process;
    }

    private static void inputData(Stream<String> z3Input, OutputStream out) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            List<String> list = z3Input.toList();
            for (String singleInput : list) {
                writer.write(singleInput);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new SatException(e);
        }
    }
}
