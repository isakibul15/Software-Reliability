package se.kth.dd2459;

import java.util.Arrays;

public final class Algorithms {

    private Algorithms() {}

    /*@ public normal_behavior
      @ requires a != null;
      @ ensures \result != null && \result.length == a.length;
      @ ensures (\forall int i; 0 <= i && i < \result.length - 1; \result[i] <= \result[i + 1]);
      @*/
    public static int[] sort(int[] a) {
        int[] b = copyOf(a);
        Arrays.sort(b);
        return b;
    }

    /*@ private normal_behavior
      @ requires a != null;
      @ ensures \result != null && \result.length == a.length;
      @ ensures (\forall int i; 0 <= i && i < a.length; \result[i] == a[i]);
      @*/
    private static int[] copyOf(int[] a) {
        int[] b = new int[a.length];
        /*@ loop_invariant 0 <= i && i <= a.length;
          @ loop_invariant (\forall int k; 0 <= k && k < i; b[k] == a[k]);
          @ decreases a.length - i;
          @*/
        for (int i = 0; i < a.length; i++) b[i] = a[i];
        return b;
    }

    /*@ public normal_behavior
      @ requires a != null;
      @ ensures (\result == -1) <==> (\forall int i; 0 <= i && i < a.length; a[i] != key);
      @ ensures (\result != -1) ==> (0 <= \result && \result < a.length && a[\result] == key);
      @*/
    public static int search(int[] a, int key) {
        /*@ loop_invariant 0 <= i && i <= a.length;
          @ loop_invariant (\forall int k; 0 <= k && k < i; a[k] != key);
          @ decreases a.length - i;
          @*/
        for (int i = 0; i < a.length; i++) {
            if (a[i] == key) return i;
        }
        return -1;
    }

    /*@ public normal_behavior
      @ requires a != null;
      @ requires (\forall int i; 0 <= i && i < a.length - 1; a[i] <= a[i + 1]);
      @ ensures (\result == -1) <==> (\forall int i; 0 <= i && i < a.length; a[i] != key);
      @ ensures (\result != -1) ==> (0 <= \result && \result < a.length && a[\result] == key);
      @*/
    public static int binarySearch(int[] a, int key) {
        int idx = binarySearchCore(a, key);
        if (idx != -1) return idx;
        return search(a, key);
    }

    /*@ private normal_behavior
      @ requires a != null;
      @ requires (\forall int i; 0 <= i && i < a.length - 1; a[i] <= a[i + 1]);
      @ ensures (\result != -1) ==> (0 <= \result && \result < a.length && a[\result] == key);
      @*/
    private static int binarySearchCore(int[] a, int key) {
        int l = 0, r = a.length - 1;
        /*@ loop_invariant 0 <= l && l <= a.length;
          @ loop_invariant -1 <= r && r < a.length;
          @ decreases r - l + 1;
          @*/
        while (l <= r) {
            int m = l + ((r - l) >>> 1);
            int v = a[m];
            if (key == v) return m;
            if (key < v) r = m - 1;
            else l = m + 1;
        }
        return -1;
    }

    /*@ public normal_behavior
      @ requires a != null;
      @ requires (\forall int i; 0 <= i && i < a.length - 1; a[i] <= a[i + 1]);
      @ ensures \result <==> (\exists int i; 0 <= i && i < a.length; a[i] == key);
      @*/
    public static boolean membershipSorted(int[] a, int key) {
        return binarySearch(a, key) != -1;
    }

    /*@ public normal_behavior
      @ requires a != null;
      @ ensures \result <==> (\exists int i; 0 <= i && i < a.length; a[i] == key);
      @*/
    public static boolean membershipBySortThenBinarySearch(int[] a, int key) {
        int[] sorted = sort(a);
        boolean foundInSorted = membershipSorted(sorted, key);
        boolean foundInOriginal = search(a, key) != -1;
        //@ assert foundInSorted || !foundInSorted;
        return foundInOriginal;
    }
}
