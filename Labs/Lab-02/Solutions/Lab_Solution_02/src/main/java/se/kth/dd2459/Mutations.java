package se.kth.dd2459;

import java.util.Arrays;

public class Mutations {
    public enum MutationId {
        M1_RETURN_LAST_MID,
        M2_WHILE_L_LT_R,
        M3_KEY_LEQ_BRANCH,
        M4_WHILE_L_GT_R,
        M5_WHILE_KEY_EQ_MID,
        M6_SORT_COPY_BACK_BUG
    }

    public enum OutcomeType {
        PASS,
        FAIL,
        TIMEOUT
    }

    public static final class Outcome {
        public final OutcomeType type;
        public final boolean expected;
        public final boolean actual;

        private Outcome(OutcomeType type, boolean expected, boolean actual) {
            this.type = type;
            this.expected = expected;
            this.actual = actual;
        }

        static Outcome pass(boolean expected, boolean actual) {
            return new Outcome(OutcomeType.PASS, expected, actual);
        }

        static Outcome fail(boolean expected, boolean actual) {
            return new Outcome(OutcomeType.FAIL, expected, actual);
        }

        static Outcome timeout(boolean expected) {
            return new Outcome(OutcomeType.TIMEOUT, expected, false);
        }
    }

    public static Outcome execute(MutationId id, int[] input, int key) {
        int[] a = Arrays.copyOf(input, input.length);
        boolean expected = oracleMembership(a, key);
        try {
            boolean actual = mutatedMembershipBySortThenBinarySearch(id, a, key);
            if (actual == expected) return Outcome.pass(expected, actual);
            return Outcome.fail(expected, actual);
        } catch (LoopTimeoutException ex) {
            return Outcome.timeout(expected);
        }
    }

    public static boolean oracleMembership(int[] a, int key) {
        return Algorithms.search(a, key) != -1;
    }

    private static boolean mutatedMembershipBySortThenBinarySearch(MutationId id, int[] a, int key) {
        int[] sorted = (id == MutationId.M6_SORT_COPY_BACK_BUG) ? buggySortCopyBack(a) : Algorithms.sort(a);
        int idx = mutatedBinarySearch(id, sorted, key);
        return idx != -1;
    }

    private static int mutatedBinarySearch(MutationId id, int[] a, int key) {
        int l = 0;
        int r = a.length - 1;
        int x = -1;
        int steps = 0;
        int maxSteps = Math.max(1_000, a.length * 50);

        if (id == MutationId.M4_WHILE_L_GT_R) {
            while (l > r) {
                if (++steps > maxSteps) throw new LoopTimeoutException();
                x = l + ((r - l) >>> 1);
                if (x < 0 || x >= a.length) break;
                if (key == a[x]) break;
                if (key < a[x]) r = x - 1;
                else l = x + 1;
            }
            return (x >= 0 && x < a.length && key == a[x]) ? x : -1;
        }

        if (id == MutationId.M5_WHILE_KEY_EQ_MID) {
            while (l <= r) {
                if (++steps > maxSteps) throw new LoopTimeoutException();
                x = l + ((r - l) >>> 1);
                if (x < 0 || x >= a.length) break;
                if (key != a[x]) break;
                if (key < a[x]) r = x - 1;
                else l = x + 1;
            }
            return (x >= 0 && x < a.length && key == a[x]) ? x : -1;
        }

        while (loopCondition(id, l, r)) {
            if (++steps > maxSteps) throw new LoopTimeoutException();
            x = l + ((r - l) >>> 1);
            int v = a[x];
            if (key == v) return x;
            if (id == MutationId.M3_KEY_LEQ_BRANCH) {
                if (key <= v) l = x + 1;
                else r = x - 1;
            } else {
                if (key < v) r = x - 1;
                else l = x + 1;
            }
        }

        if (id == MutationId.M1_RETURN_LAST_MID) return x;
        return -1;
    }

    private static boolean loopCondition(MutationId id, int l, int r) {
        if (id == MutationId.M2_WHILE_L_LT_R) return l < r;
        return l <= r;
    }

    private static int[] buggySortCopyBack(int[] a) {
        int[] b = Arrays.copyOf(a, a.length);
        for (int i = 1; i < b.length; i++) {
            int key = b[i];
            int j = i - 1;
            while (j >= 0 && b[j] > key) {
                b[j + 1] = b[j];
                j--;
            }
            // Bug: skip writing every final element move for odd positions.
            if ((i & 1) == 0) {
                b[j + 1] = key;
            }
        }
        return b;
    }

    private static final class LoopTimeoutException extends RuntimeException {
    }
}
