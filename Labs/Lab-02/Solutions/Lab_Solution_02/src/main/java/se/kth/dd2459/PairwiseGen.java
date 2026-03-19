package se.kth.dd2459;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PairwiseGen {
    public static void main(String[] args) throws Exception {
        if (args.length != 7) {
            System.out.println("Usage: PairwiseGen <outFile> <arrayLen> <minVal> <maxVal> <maxTests> <candidatesPerRound> <seed>");
            return;
        }
        String outFile = args[0];
        int arrayLen = Integer.parseInt(args[1]);
        int minVal = Integer.parseInt(args[2]);
        int maxVal = Integer.parseInt(args[3]);
        int maxTests = Integer.parseInt(args[4]);
        int candidates = Integer.parseInt(args[5]);
        long seed = Long.parseLong(args[6]);
        int written = generate(outFile, arrayLen, minVal, maxVal, maxTests, candidates, seed);
        System.out.println("Wrote pairwise tests: " + outFile + " (" + written + ")");
    }

    public static int generate(
            String outFile,
            int arrayLen,
            int minVal,
            int maxVal,
            int maxTests,
            int candidatesPerRound,
            long seed
    ) throws IOException {
        if (arrayLen < 0) throw new IllegalArgumentException("arrayLen must be >= 0");
        if (minVal > maxVal) throw new IllegalArgumentException("minVal must be <= maxVal");
        if (maxTests <= 0) throw new IllegalArgumentException("maxTests must be > 0");
        if (candidatesPerRound <= 0) throw new IllegalArgumentException("candidatesPerRound must be > 0");

        int variableCount = arrayLen + 1; // N array cells + key
        int[] domain = domain(minVal, maxVal);
        Random rnd = new Random(seed);
        Set<Long> uncovered = buildTargets(variableCount, domain.length, maxTests, candidatesPerRound, rnd);

        List<int[]> selected = new ArrayList<>();
        while (!uncovered.isEmpty() && selected.size() < maxTests) {
            int[] best = null;
            int bestScore = -1;
            for (int c = 0; c < candidatesPerRound; c++) {
                int[] cand = randomAssignment(variableCount, domain.length, rnd);
                int score = score(cand, uncovered);
                if (score > bestScore) {
                    bestScore = score;
                    best = cand;
                }
            }
            if (best == null || bestScore <= 0) break;
            selected.add(best);
            cover(best, uncovered);
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(outFile))) {
            w.write("# pairwise tests");
            w.newLine();
            for (int[] assignment : selected) {
                int[] arr = new int[arrayLen];
                for (int i = 0; i < arrayLen; i++) arr[i] = domain[assignment[i]];
                int key = domain[assignment[arrayLen]];
                w.write(TestCaseData.format(arr, key));
                w.newLine();
            }
        }
        return selected.size();
    }

    private static int[] domain(int minVal, int maxVal) {
        int[] d = new int[maxVal - minVal + 1];
        for (int i = 0; i < d.length; i++) d[i] = minVal + i;
        return d;
    }

    private static int[] randomAssignment(int variableCount, int domainSize, Random rnd) {
        int[] a = new int[variableCount];
        for (int i = 0; i < variableCount; i++) {
            a[i] = rnd.nextInt(domainSize);
        }
        return a;
    }

    private static Set<Long> buildTargets(
            int variableCount,
            int domainSize,
            int maxTests,
            int candidatesPerRound,
            Random rnd
    ) {
        long fullPairs = ((long) variableCount * (variableCount - 1)) / 2;
        long fullUniverse = fullPairs * (long) domainSize * (long) domainSize;

        // Keep memory bounded for large N/ranges: use sampled targets as an approximate pairwise objective.
        long desired = Math.min(fullUniverse, Math.max(5_000L, (long) maxTests * candidatesPerRound * 20L));
        int targetCount = (int) Math.min(desired, 200_000L);

        Set<Long> s = new HashSet<>(targetCount * 2);
        while (s.size() < targetCount) {
            int i = rnd.nextInt(variableCount);
            int j = rnd.nextInt(variableCount);
            if (i == j) continue;
            if (i > j) {
                int t = i;
                i = j;
                j = t;
            }
            int vi = rnd.nextInt(domainSize);
            int vj = rnd.nextInt(domainSize);
            s.add(pairKey(i, j, vi, vj));
        }
        return s;
    }

    private static int score(int[] assignment, Set<Long> uncovered) {
        int hits = 0;
        for (int i = 0; i < assignment.length; i++) {
            for (int j = i + 1; j < assignment.length; j++) {
                if (uncovered.contains(pairKey(i, j, assignment[i], assignment[j]))) hits++;
            }
        }
        return hits;
    }

    private static void cover(int[] assignment, Set<Long> uncovered) {
        for (int i = 0; i < assignment.length; i++) {
            for (int j = i + 1; j < assignment.length; j++) {
                uncovered.remove(pairKey(i, j, assignment[i], assignment[j]));
            }
        }
    }

    private static long pairKey(int i, int j, int vi, int vj) {
        long x = i;
        x = (x << 12) ^ j;
        x = (x << 12) ^ vi;
        x = (x << 12) ^ vj;
        return x;
    }
}
