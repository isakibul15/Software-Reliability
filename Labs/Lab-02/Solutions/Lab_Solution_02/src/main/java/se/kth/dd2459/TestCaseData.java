package se.kth.dd2459;

import java.util.StringTokenizer;

final class TestCaseData {
    final int[] array;
    final int key;

    TestCaseData(int[] array, int key) {
        this.array = array;
        this.key = key;
    }

    static TestCaseData parse(String line) {
        String trimmed = line.trim();
        int bar = trimmed.indexOf('|');
        if (bar < 0) {
            throw new IllegalArgumentException("Bad line (missing '|'): " + line);
        }
        String left = trimmed.substring(0, bar).trim();
        String right = trimmed.substring(bar + 1).trim();
        int key = Integer.parseInt(right);
        return new TestCaseData(parseArray(left), key);
    }

    static String format(int[] a, int key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        sb.append(" | ").append(key);
        return sb.toString();
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
