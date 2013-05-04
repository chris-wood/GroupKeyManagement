import java.util.ArrayList;
import java.util.HashMap;

public class Model
{
	static HashMap<Integer, Double> E;
	static double p2 = 1.0;
	
	static double fact(double x) {
		double f = 1.0;
		if (x <= 0) {
			return 1.0;
		} else {
			for (int i = 1; i <= x; i++) {
				f *= i;
			}
		}
		return f;
	}
	
	static double binom(int n, int k)
	{
		double b = 0.0;
		
		double num = fact((double)n);
		double denom = fact((double)n) * fact((double)(n - k));
		b = num / denom;
		
		return b;
	}
	
	static double probHCol(int[][] H, int[][] S, int k, int m, int j) {
		double prob = 0.0;
		
		double prod = 1.0;
		for (int i = 0; i < k; i++) {
			double innerProd = 
					binom(S[i][j], H[i][j]) * 
					(Math.pow(p2, H[i][j])) * 
					Math.pow(1 - p2, S[i][j] - H[i][j]);
			prod *= innerProd;
		}
		prob = prod;
		
		return prob;
	}
	
	static double probH(int[][] H, int[][] S, int k, int m) {
		double prob = 0.0;
		
		for (int j = 0; j < m; j++) {
			prob *= probHCol(H, S, k, m, j);
		}
		
		return prob;
	}
	
	static void updateE(int[][] D, int k, int m, int n) {
		double sum = 0.0;
		ArrayList<int[][]> Hset = buildHSet(D, k, m, n);
		int[][] S = buildS(D, k, m, n);
		System.out.println("Update E - " + Hset.size() + " Hs in the Hset");
		disp(S, true);
		for (int[][] H : Hset) {
			disp(H, true);
			sum += probH(H, S, k, m) * E.get(add(D, H, k, m).hashCode());
		}
		sum += 1; // 1 + (big sum)
		
		// multilpy by 1/(1-p(H0))
		double prod = 1 / (1 - probH(buildHzero(k, m), S, k, m));
		
		// insert the new expected time value
		E.put(D.hashCode(), prod * sum);
	}
	
	static boolean isValid(int k, int m, int n, int[][] D) {
		// Check Rows
		for (int j = 0; j < m; j++) {
			int lastRow = D[0][j]; // pull out the last one
			for (int i = 1; i < k; i++) {
				if (lastRow < D[i][j])
				{
					return false;
				}
				else {
					lastRow = D[i][m - 1];
				}
			}
		}
		
		// Check columns
		for (int i = 0; i < k; i++) {
			int lastCol = D[i][0];
			for (int j = 1; j < m; j++) {
				if (lastCol < D[i][j]) {
					return false;
				} else {
					lastCol = D[i][j];
				}
			}
		}
		
		// Check row/col boundaries
		int last = D[0][m - 1];
		for (int i = 1; i < k; i++) {
			if (last < D[i][0]){
				return false;
			} else {
				last = D[i][m - 1];
			}
		}
		
		return true;
	}

	public static ArrayList< int[][] > pushLast(int k, int m, int n, int[][] Dmax, ArrayList<int[][]> space)
	{
		if (space == null)
		{
			space = new ArrayList< int[][] >();
		} 
		
		// Pass, so append the matrix.
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < m; j++) {
				Dmax[i][j] = Dmax[i][m - 1]; // each elem in the same row as the same value
			}
		}
		
		// Check constraints
		if (!isValid(k, m, n, Dmax)) {
			return space;
		}
		
//		disp(Dmax, true);
		space.add(clone(Dmax));
		
		// Start to push from the bottom
		for (int i = k - 2; i >= 0; i--) {
			if (Dmax[i][m - 1] > 0)
			{
				Dmax[i][m-1] -= 1;
				Dmax[i + 1][m-1] += 1;
//				disp(Dmax,true);
				space = pushLast(k, m, n, Dmax, space);
			}
		}

		return space;
	}
	
	public static ArrayList<int[][]> push(int k, int m, int n, int[][] D, ArrayList<int[][]> space)
	{
		if (space == null)
		{
			space = new ArrayList< int[][] >();
		}
		
		// Check constraints...
//		System.out.println("tried: ");
//		disp(D, true);
		if (!isValid(k, m, n, D)) return space;
		if (!contains(space, D)) space.add(clone(D));
		
		for (int index = 0; index < k * m; index++) {
			int count = 0;
			
			int[][] newD = clone(D);
//			System.out.println("trying: " + index);
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < m; j++) {
					count++;
					if (count < index) {
//						System.out.println("skipping...");
						// pass...
						continue;
					}
					if (i == k - 1 && j == m - 1) {
						// pass...
						continue;
					} else if (j == m - 1) {
						// push down to the next row...
						if (newD[i+1][0] < newD[i][j]) {
							newD[i][j]--;
							newD[i+1][0]++;
							space = push(k, m, n, newD, space);
						}
					} else {
						// push to next column...
						if (newD[i][j + 1] < newD[i][j]) {
							newD[i][j]--;
							newD[i][j+1]++;
							space = push(k, m, n, newD, space);
						}
					}
				}
			}
		}
		
		return space;
	}
	
	public static int[][] buildS(int[][] D, int k, int m, int n) {
		int[] D1 = new int[m];
		for (int j = 0; j < m; j++) {
			int sum = 0;
			for (int i = 0; i < k; i++) {
				sum += D[i][j];
			}
			D1[j] = sum;
		}
		
		// Create the new D matrix...
		int[][] newD = new int[k + 1][m];
		for (int j = 0; j < m; j++) newD[0][j] = D1[j];
		for (int i = 1; i < k + 1; i++) {
			for (int j = 0; j < m; j++) {
				newD[i][j] = D[i - 1][j];
			}
		}
		
		
		// Finally, create S
		int[][] S = new int[k + 1][m];
		for (int i = 0; i < k + 1; i++) {
			for (int j = 0; j < m; j++) {
				if (i == 0 && j == 0) {
					S[i][j] = n - 1 - newD[0][0];
				} else if (j == 0) {
					S[i][j] = newD[i - 1][m - 1] - newD[i][j]; 
				} else {
					S[i][j] = newD[i][j - 1] - newD[i][j];
				}
			}
		}
		return S;
	}
	
	public static boolean validH(int[][] H, int[][] S, int k, int m) {
//		System.out.println("validH");
//		disp(H, true);
//		disp(S, true);
		// row sum constraint
		for (int j = 0; j < m; j++) {
			int sum = 0;
			for (int i = 0; i < k + 1; i++) {
				sum += H[i][j];
			}
			if (sum > S[0][j]) return false;
		}
		// cell constraint
		for (int i = 0; i < k + 1; i++) {
			for (int j = 0; j < m; j++) {
				if (!(H[i][j] >= 0 && H[i][j] <= S[i][j])) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static ArrayList<int[][]> buildH(int[][] H, int[][] S, int i, int j, int k, int m) {
		ArrayList<int[][]> Hset = new ArrayList<int[][]>();
		
		// Incremement H at index and then check to see if within the S bound
		H[i][j]++;
//		System.out.println("buildH");
//		disp(S, true);
//		disp(H, true);
		if (!validH(H, S, k, m)) {
//			System.out.println("early return");
			return Hset;
		}
		
		Hset.add(H);
		
		for (int r = 0; r < k + 1; r++) {
			for (int c = 0; c < m; c++) {
				Hset.addAll(buildH(H, S, r, c, k, m));
			}
		}
		
		return Hset;
	}
	
	public static int[][] add(int[][] D, int[][] H, int k, int m) {
		int[][] sum = new int[k][m];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < m; j++) {
				sum[i][j] = D[i][j] + H[i][j];
			}
		}
		return sum;
	}
	
	static int[][] buildHzero(int k, int m) {
		int[][] H = new int[k + 1][m];
		for (int i = 0; i < k + 1; i++) {
			for (int j = 0; j < m; j++) {
				H[i][j] = 0;
			}
		}
		return H;
	}
	
	public static ArrayList<int[][]> buildHSet(int[][] D, int k, int m, int n) {
		ArrayList<int[][]> Hset = new ArrayList<int[][]>();
		
		int[][] H = buildHzero(k, m);
		int[][] S = buildS(D, k, m, n);
		
		for (int i = 0; i < m + 1; i++) {
			for (int j = 0; j < m; j++) {
				Hset.addAll(buildH(H, S, i, j, k, m));
			}
		}
		
		// TODO: need to uniquify the Hset (they're probably not unique...)
		
		return Hset;
	}
	
	public static boolean isZero(int[][] H, int k, int m) {
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < m; j++) {
				if (H[i][j] > 0) return false;
			}
		}
		return true;
	}
	
	public static boolean contains(ArrayList<int[][]> space, int[][] M) {
		for (int[][] tmp : space) {
			boolean match = true;
			for (int i = 0; i < tmp.length && match; i++) {
				for (int j = 0; j < tmp[i].length && match; j++) {
					if (tmp[i][j] != M[i][j]) {
						match = false;
					}
				}
			}
			if (match) return true;
		}
		return false;
	}
	
	public static int[][] clone(int[][] M) {
		int[][] copy = new int[M.length][M[0].length];
		for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[i].length; j++) {
				copy[i][j] = M[i][j];
			}
		}
		return copy;
	}
	
	public static void disp(int[][] m, boolean box)
	{
		if (box) System.out.println("-----");
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				System.out.print(m[i][j] + " ");
			}
			System.out.println();
		}
		if (box) System.out.println("-----");
	}
	
	static int[][] buildDmax(int N, int k, int m, int n) {
		int[][] Dmax = new int[k][m];
		int alloc = 0;
		
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < m; j++) {
				if (alloc + n - 1 < N) { // max out...
					Dmax[i][j] = n - 1;
					alloc += n - 1;
				} else if (N - alloc <= n - 1) { // fill in the difference...
					Dmax[i][j] = N - alloc;
					alloc += N - alloc;
					return Dmax;
				}
			}
		}
		
		return Dmax; // we'll never get here
	}

	public static void main(String[] args)
	{
		int k = 2; // num children
		int m = 2; // num messages
		int n = 5; // num nodes
		int N = (n - 1) * m;
		
		// Create the estimated time collection
		// TODO: is there a better way to use the entire matrix as a key to lookup the time?...
		E = new HashMap<Integer, Double>();
		
		// Create the initial max configuration
//		int[][] Dmax = new int[k][m];
//		for (int c = 0; c < m; c++)
//		{
//			Dmax[0][c] = n - 1;  
//		}
		int[][] Dmax = buildDmax(N, k, m, n);
		disp(Dmax, true);
		
		// Generate the list of all matrices in the D^8 (D*)
		ArrayList<int[][]> D8 = push(k, m, n, Dmax, null);
		
		// Initialize the ET(D) for all matrices in the d = 8
//		System.out.println("What we got back,...");
		for (int[][] D : D8) {
			disp(D, true);
			E.put(D.hashCode(), 0.0);
		}
		
		Dmax = buildDmax(N - 1, k, m, n);
//		ArrayList<int[][]> D7 = push(k, m, n, Dmax, null);
//		System.out.println("What we got back,...");
//		for (int[][] D : D7) {
//			disp(D, true);
//			disp(buildS(D, k, m, n), true);
//		}
		
		// Let the recursion begin!
		for (int i = N - 1; i >= 0; i--) {
			Dmax = buildDmax(i, k, m, n);
			ArrayList<int[][]> Dset = push(k, m, n, Dmax, null);
			for (int[][] D : Dset) {
				updateE(D, k, m, n);
			}
		}
	}
}