
package edu.berkeley.nlp.assignments.rerank.student;

public class quicksort{
	double[] main;
	int[] index;
	
	public quicksort(double[] main, int[] index) {
		this.main = main;
		this.index = index;
	}
	
	public  void sort() {
	    sort(main, index, 0, index.length - 1);
	}

	// quicksort a[left] to a[right]
	public static void sort(double[] a, int[] index, int left, int right) {
	    if (right <= left) return;
	    int i = partition(a, index, left, right);
	    sort(a, index, left, i-1);
	    sort(a, index, i+1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition(double[] a, int[] index, 
	int left, int right) {
	    int i = left - 1;
	    int j = right;
	    while (true) {
	        while (greater(a[++i], a[right]))      // find item on left to swap
	            ;                               // a[right] acts as sentinel
	        while (greater(a[right], a[--j]))      // find item on right to swap
	            if (j == left) break;           // don't go out-of-bounds
	        if (i >= j) break;                  // check if pointers cross
	        exch(a, index, i, j);               // swap two elements into place
	    }
	    exch(a, index, i, right);               // swap with partition element
	    return i;
	}

	// is x < y ?
	private static boolean greater(double x, double y) {
	    return (x > y);
	}

	// exchange a[i] and a[j]
	private static void exch(double[] a, int[] index, int i, int j) {
	    double swap = a[i];
	    a[i] = a[j];
	    a[j] = swap;
	    int b = index[i];
	    index[i] = index[j];
	    index[j] = b;
	}
	
	public static void main(String args[]) {
		double [] main = {1.0, 0.5, 3.2, 4.5, -1.0,-2.0};
		int[] idx = {0,1,2,3,4,5};
		quicksort qs = new quicksort(main, idx);
		qs.sort();
		for(int i = 0; i < idx.length; i++) {
			System.out.println(qs.index[i]);
		}
	}
}