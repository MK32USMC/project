package ca.concordia.util;

import ca.concordia.model.BixiTrip;

/** merge & sorts class to simplify array sorting **/
public class MergeSort {

    /** merge & sorts in ascending order of time **/
    public static void sortByStartTime(SimpleList<BixiTrip> list) {
        if (list.size() <= 1) return;
        BixiTrip[] arr = toArray(list);
        mergeSortTime(arr, 0, arr.length - 1);
        fromArray(list, arr);
    }

    private static void mergeSortTime(BixiTrip[] arr, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        mergeSortTime(arr, lo, mid);
        mergeSortTime(arr, mid + 1, hi);
        mergeTime(arr, lo, mid, hi);
    }

    private static void mergeTime(BixiTrip[] arr, int lo, int mid, int hi) {
        BixiTrip[] tmp = new BixiTrip[hi - lo + 1];
        int i = lo, j = mid + 1, k = 0;
        while (i <= mid && j <= hi) {
            if (arr[i].getStartTimeMs() <= arr[j].getStartTimeMs()) tmp[k++] = arr[i++];
            else                                                      tmp[k++] = arr[j++];
        }
        while (i <= mid) tmp[k++] = arr[i++];
        while (j <= hi)  tmp[k++] = arr[j++];
        for (int x = 0; x < tmp.length; x++) arr[lo + x] = tmp[x];
    }

    /** merge & sorts by descending order of duration **/
    public static void sortByDurationDesc(SimpleList<BixiTrip> list) {
        if (list.size() <= 1) return;
        BixiTrip[] arr = toArray(list);
        mergeSortDuration(arr, 0, arr.length - 1);
        fromArray(list, arr);
    }

    private static void mergeSortDuration(BixiTrip[] arr, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        mergeSortDuration(arr, lo, mid);
        mergeSortDuration(arr, mid + 1, hi);
        mergeDuration(arr, lo, mid, hi);
    }

    private static void mergeDuration(BixiTrip[] arr, int lo, int mid, int hi) {
        BixiTrip[] tmp = new BixiTrip[hi - lo + 1];
        int i = lo, j = mid + 1, k = 0;
        while (i <= mid && j <= hi) {
            // descending — larger duration first
            if (arr[i].getDurationMinutes() >= arr[j].getDurationMinutes()) tmp[k++] = arr[i++];
            else                                                              tmp[k++] = arr[j++];
        }
        while (i <= mid) tmp[k++] = arr[i++];
        while (j <= hi)  tmp[k++] = arr[j++];
        for (int x = 0; x < tmp.length; x++) arr[lo + x] = tmp[x];
    }

    /** merge and sort ascending alphabetically with aligned counts **/
    public static void sortStringsAlphaWithCounts(String[] keys, int[] counts, int len) {
        if (len <= 1) return;
        mergeSortAlphaWithCounts(keys, counts, 0, len - 1);
    }

    private static void mergeSortAlphaWithCounts(String[] keys, int[] counts, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        mergeSortAlphaWithCounts(keys, counts, lo, mid);
        mergeSortAlphaWithCounts(keys, counts, mid + 1, hi);
        mergeAlphaWithCounts(keys, counts, lo, mid, hi);
    }

    private static void mergeAlphaWithCounts(String[] keys, int[] counts, int lo, int mid, int hi) {
        String[] tmpK = new String[hi - lo + 1];
        int[]    tmpC = new int[hi - lo + 1];
        int i = lo, j = mid + 1, k = 0;
        while (i <= mid && j <= hi) {
            if (keys[i].compareTo(keys[j]) <= 0) {
                tmpK[k] = keys[i]; tmpC[k] = counts[i]; k++; i++;
            } else {
                tmpK[k] = keys[j]; tmpC[k] = counts[j]; k++; j++;
            }
        }
        while (i <= mid) { tmpK[k] = keys[i]; tmpC[k] = counts[i]; k++; i++; }
        while (j <= hi)  { tmpK[k] = keys[j]; tmpC[k] = counts[j]; k++; j++; }
        for (int x = 0; x < tmpK.length; x++) {
            keys[lo + x]   = tmpK[x];
            counts[lo + x] = tmpC[x];
        }
    }

    /** deals with string over int priority in alphabetical descending sorting **/
    public static void sortByCountDesc(String[] keys, int[] counts, int len) {
        if (len <= 1) return;
        mergeSortCount(keys, counts, 0, len - 1);
    }

    private static void mergeSortCount(String[] keys, int[] counts, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        mergeSortCount(keys, counts, lo, mid);
        mergeSortCount(keys, counts, mid + 1, hi);
        mergeCount(keys, counts, lo, mid, hi);
    }

    private static void mergeCount(String[] keys, int[] counts, int lo, int mid, int hi) {
        String[] tmpK = new String[hi - lo + 1];
        int[]    tmpC = new int[hi - lo + 1];
        int i = lo, j = mid + 1, k = 0;
        while (i <= mid && j <= hi) {
            /** moves to subsequent string places when first char is the same **/
            if (counts[i] > counts[j] ||
                    (counts[i] == counts[j] && keys[i].compareTo(keys[j]) <= 0)) {
                tmpK[k] = keys[i]; tmpC[k] = counts[i]; k++; i++;
            } else {
                tmpK[k] = keys[j]; tmpC[k] = counts[j]; k++; j++;
            }
        }
        while (i <= mid) { tmpK[k] = keys[i]; tmpC[k] = counts[i]; k++; i++; }
        while (j <= hi)  { tmpK[k] = keys[j]; tmpC[k] = counts[j]; k++; j++; }
        for (int x = 0; x < tmpK.length; x++) {
            keys[lo + x]   = tmpK[x];
            counts[lo + x] = tmpC[x];
        }
    }

    /** array functionality stuff **/
    private static BixiTrip[] toArray(SimpleList<BixiTrip> list) {
        BixiTrip[] arr = new BixiTrip[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static void fromArray(SimpleList<BixiTrip> list, BixiTrip[] arr) {
        for (int i = 0; i < arr.length; i++) list.set(i, arr[i]);
    }
}
