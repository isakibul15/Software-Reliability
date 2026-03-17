package se.kth.dd2459;

import java.io.File;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Main {

    // Usage: Main <testfile>
    // Format per line:
    //   <space-separated ints> | <key>
    // Example:
    //   3 2 1 | 2
    //   | 5        (empty array)
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: Main <testfile>");
            return;
        }

        int executed = 0;

        Scanner sc = new Scanner(new File(args[0]));
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int bar = line.indexOf('|');
            if (bar < 0) {
                throw new IllegalArgumentException("Bad line (missing '|'): " + line);
            }

            String left = line.substring(0, bar).trim();
            String right = line.substring(bar + 1).trim();
            int key = Integer.parseInt(right);

            int[] a = parseArray(left);

            boolean result = Algorithms.membershipBySortThenBinarySearch(a, key);
            System.out.println(result);
            executed++;
        }
        sc.close();

        System.out.println("Executed tests: " + executed);
    }

    private static int[] parseArray(String s) {
        if (s.isEmpty()) return new int[0];

        StringTokenizer st = new StringTokenizer(s);
        int n = st.countTokens();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(st.nextToken());
        }
        return a;
    }
}
