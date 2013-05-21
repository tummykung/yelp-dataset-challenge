/*
 * CHIRP: A new classifier based on Composite Hypercubes on Iterated Random Projections.
 *
 * Copyright 2010 by Leland Wilkinson.
 *
 * The contents of this file are subject to the Mozilla Public License Version 2.0 (the "License")
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 */

package weka.classifiers.misc.chirp;

import java.util.Arrays;
import java.util.Comparator;

public class Sorter {

    private Sorter() {}

    public static int[] ascendingSort(double[] x) {
        Integer[] sortOrder = sort(x);
        int[] result = new int[x.length];
        for (int i = 0; i < result.length; i++)
            result[i] = sortOrder[i].intValue();
        return result;
    }

    public static int[] descendingSort(double[] x) {
        Integer[] sortOrder = sort(x);
        int[] result = new int[x.length];
        for (int i = 0; i < result.length; i++)
            result[i] = sortOrder[result.length - i - 1].intValue();
        return result;
    }

    @SuppressWarnings("unchecked")
	private static Integer[] sort(final double[] x) {
        Integer[] sortOrder = new Integer[x.length];
        for (int i = 0; i < x.length; i++)
            sortOrder[i] = new Integer(i);
        Arrays.sort(sortOrder, 0, x.length, new Comparator() {
            public int compare(Object object1, Object object2) {
                int firstIndex = ((Integer) object1).intValue();
                int secondIndex = ((Integer) object2).intValue();
                return Double.compare(x[firstIndex], x[secondIndex]);
            }
        });
        return sortOrder;
    }
}
