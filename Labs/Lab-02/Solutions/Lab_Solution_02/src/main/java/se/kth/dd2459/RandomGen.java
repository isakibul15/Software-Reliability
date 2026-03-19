package se.kth.dd2459;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RandomGen {
    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.out.println("Usage: RandomGen <outFile> <numTests> <arrayLen> <minVal> <maxVal> <seed>");
            return;
        }
        String outFile = args[0];
        int numTests = Integer.parseInt(args[1]);
        int arrayLen = Integer.parseInt(args[2]);
        int minVal = Integer.parseInt(args[3]);
        int maxVal = Integer.parseInt(args[4]);
        long seed = Long.parseLong(args[5]);
        generate(outFile, numTests, arrayLen, minVal, maxVal, seed);
        System.out.println("Wrote random tests: " + outFile + " (" + numTests + ")");
    }

    public static void generate(
            String outFile,
            int numTests,
            int arrayLen,
            int minVal,
            int maxVal,
            long seed
    ) throws IOException {
        if (arrayLen < 0) throw new IllegalArgumentException("arrayLen must be >= 0");
        if (minVal > maxVal) throw new IllegalArgumentException("minVal must be <= maxVal");
        Random rnd = new Random(seed);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(outFile))) {
            w.write("# random tests");
            w.newLine();
            for (int i = 0; i < numTests; i++) {
                int[] a = new int[arrayLen];
                for (int j = 0; j < arrayLen; j++) {
                    a[j] = nextInRange(rnd, minVal, maxVal);
                }
                int key = nextInRange(rnd, minVal, maxVal);
                w.write(TestCaseData.format(a, key));
                w.newLine();
            }
        }
    }

    private static int nextInRange(Random rnd, int minVal, int maxVal) {
        return minVal + rnd.nextInt(maxVal - minVal + 1);
    }
}
