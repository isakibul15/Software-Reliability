package se.kth.dd2459;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        if ("runfile".equals(args[0])) {
            if (args.length != 3) {
                printUsage();
                return;
            }
            Mutations.MutationId id = Mutations.MutationId.valueOf(args[1]);
            String file = args[2];
            int n = testsUntilDetection(id, file);
            System.out.println(id + " -> " + renderCount(n));
            return;
        }

        if ("benchmark".equals(args[0])) {
            if (args.length != 10) {
                printUsage();
                return;
            }
            int arrayLen = Integer.parseInt(args[1]);
            int minVal = Integer.parseInt(args[2]);
            int maxVal = Integer.parseInt(args[3]);
            int randomTests = Integer.parseInt(args[4]);
            int pairwiseMaxTests = Integer.parseInt(args[5]);
            int randomRuns = Integer.parseInt(args[6]);
            int pairwiseRuns = Integer.parseInt(args[7]);
            int candidatesPerRound = Integer.parseInt(args[8]);
            String outDir = args[9];

            runBenchmark(arrayLen, minVal, maxVal, randomTests, pairwiseMaxTests, randomRuns, pairwiseRuns, candidatesPerRound, outDir);
            return;
        }

        printUsage();
    }

    private static void runBenchmark(
            int arrayLen,
            int minVal,
            int maxVal,
            int randomTests,
            int pairwiseMaxTests,
            int randomRuns,
            int pairwiseRuns,
            int candidatesPerRound,
            String outDir
    ) throws Exception {
        Path dir = Path.of(outDir);
        Files.createDirectories(dir);

        Map<Mutations.MutationId, List<Integer>> randomMeasures = new EnumMap<>(Mutations.MutationId.class);
        Map<Mutations.MutationId, List<Integer>> pairwiseMeasures = new EnumMap<>(Mutations.MutationId.class);
        for (Mutations.MutationId id : Mutations.MutationId.values()) {
            randomMeasures.put(id, new ArrayList<>());
            pairwiseMeasures.put(id, new ArrayList<>());
        }

        for (int run = 0; run < randomRuns; run++) {
            String file = dir.resolve("random_" + run + ".txt").toString();
            RandomGen.generate(file, randomTests, arrayLen, minVal, maxVal, 1_000_000L + run);
            for (Mutations.MutationId id : Mutations.MutationId.values()) {
                randomMeasures.get(id).add(testsUntilDetection(id, file));
            }
        }

        for (int run = 0; run < pairwiseRuns; run++) {
            String file = dir.resolve("pairwise_" + run + ".txt").toString();
            PairwiseGen.generate(file, arrayLen, minVal, maxVal, pairwiseMaxTests, candidatesPerRound, 2_000_000L + run);
            for (Mutations.MutationId id : Mutations.MutationId.values()) {
                pairwiseMeasures.get(id).add(testsUntilDetection(id, file));
            }
        }

        System.out.println("Mutation\tRandom(avg)\tPairwise(min)");
        for (Mutations.MutationId id : Mutations.MutationId.values()) {
            String avg = averageOrNA(randomMeasures.get(id));
            String min = minOrNA(pairwiseMeasures.get(id));
            System.out.println(id + "\t" + avg + "\t" + min);
        }
    }

    public static int testsUntilDetection(Mutations.MutationId id, String file) throws Exception {
        List<TestCaseData> cases = load(file);
        for (int i = 0; i < cases.size(); i++) {
            TestCaseData tc = cases.get(i);
            Mutations.Outcome out = Mutations.execute(id, tc.array, tc.key);
            if (out.type != Mutations.OutcomeType.PASS) {
                return i + 1;
            }
        }
        return -1;
    }

    private static List<TestCaseData> load(String file) throws Exception {
        List<TestCaseData> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#")) continue;
                list.add(TestCaseData.parse(s));
            }
        }
        return list;
    }

    private static String averageOrNA(List<Integer> values) {
        long sum = 0;
        int count = 0;
        for (int v : values) {
            if (v != -1) {
                sum += v;
                count++;
            }
        }
        if (count == 0) return "not found";
        return String.format("%.2f", (double) sum / count);
    }

    private static String minOrNA(List<Integer> values) {
        int best = Integer.MAX_VALUE;
        for (int v : values) {
            if (v != -1 && v < best) best = v;
        }
        if (best == Integer.MAX_VALUE) return "not found";
        return Integer.toString(best);
    }

    private static String renderCount(int n) {
        return (n == -1) ? "not found" : Integer.toString(n);
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  TestRunner runfile <MUTATION_ID> <testFile>");
        System.out.println("  TestRunner benchmark <arrayLen> <minVal> <maxVal> <randomTests> <pairwiseMaxTests> <randomRuns> <pairwiseRuns> <candidatesPerRound> <outDir>");
        System.out.println("Mutation IDs:");
        for (Mutations.MutationId id : Mutations.MutationId.values()) {
            System.out.println("  " + id);
        }
    }
}
