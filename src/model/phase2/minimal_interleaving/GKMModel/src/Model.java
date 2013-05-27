import java.util.ArrayList;
import java.util.HashMap;

public class Model
{
	static HashMap<String, Double> E;
	static double p2 = 0.5;
	static double p1 = 0.5;
	
	static String canonical(int[][] M) {
		String result = "";
		for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[i].length; j++) {
				result += M[i][j] + ",";
			}
		}
		return result;
	}
	
	static int fact(int x) {
		int f = 1;
		if (x <= 0) {
			return 1;
		} else {
			for (int i = 1; i <= x; i++) {
				f *= i;
			}
		}
		return f;
	}
	
//	static int binom(int n, int k)
//	{
//		double b = 0.0;
//		
//		if (k == 0)
//			return 1;
//		if (n == 0)
//			return 0;
//		
//		double num = (double)fact(n);
//		double denom = (double)(fact(k) * fact(n - k));
//		b = num / denom;
//		
//		return (int)b;
//	}
	
	static int binom(int n, int k) 
	{
		if (k == 0 || n == k)
			return 1;
		else if (n == 0)
			return 0;
		else
			return binom(n-1,k-1) + binom(n-1,k);
	}
	
	// H and S are ALWAYS in expanded form!
	static double probHCol(int[][] H, int[][] S, int k, int m, int j) {
		double prob = 0.0;
		
//		disp("CHECKING");
//		disp("" + Math.pow(1.0 - p2, S[1][1] - H[1][1]));
		
		double prod = 1.0;
		for (int i = 1; i < k; i++) { // was 0, but both H and S include the -1 row, and that's not what the document says
			double innerProd = binom(S[i][j], H[i][j]);
//			disp("" + innerProd);
			innerProd *= (Math.pow(p2, H[i][j]));
//			disp("" + innerProd);
			innerProd *= (Math.pow(1.0 - p2, S[i][j] - H[i][j]));
//			disp("" + innerProd);
//					binom(S[i][j], H[i][j]) * 
//					(Math.pow(p2, H[i][j])) * 
//					(Math.pow(1.0 - p2, S[i][j] - H[i][j]));
			prod *= innerProd;
//			disp("S/H values: " + S[i][j] + " " + H[i][j]);
//			disp("i = " + i + ", " + innerProd);
		}
		prob = prod;
		
		return prob;
	}
	
	static double gh(int[][] S, int[][] H, int k, int m, int b) // equation 14
	{
		double num = 1.0;
		double denom = 0.0;
		for (int i = 1; i < k; i++)
		{
			num *= binom(S[i][0], H[i][0]);
		}
		int sum = 0;
		for (int i = 1; i < k; i++)
		{
			sum += S[i][0];
		}
		denom = binom(sum, b);
		
		return (double)num / (double)denom;
	}
	
	static double probH(int[][] H, int[][] S, int k, int m) {
		double prob = 1.0;
		
		disp("Calculating probH");
		disp(H, true);
		disp(S, true);
		
		// p^0 is different - use equations 11/12/13/14
		int b = 0;
		for (int i = 1; i <k; i++)
		{
			b += H[i][0];
		}
		int sSum = 0;
		for (int i = 1; i < k; i++) {
			sSum += S[i][0];
		}
		int U = sSum < S[0][0] ? sSum : S[0][0];
		
		if (b == U && b == sSum) //equation 12
		{
			double sum = 0;
//			disp("b = " + b);
			for (int i = b; i <= S[0][0]; i++)
			{
				sum += binom(S[0][0], i) * Math.pow(p1, i) * Math.pow(1.0 - p1, S[0][0] - i);
			}
			prob *= sum;
//			disp("b == U sum: " + sum);
		} 
		else// equation 13
		{
			double tmp = gh(S, H, k, m, b) * binom(S[0][0], b) * Math.pow(p1, b) * Math.pow(1 - p1, S[0][0] - b);
//			disp ("b < U prod: " + tmp);
			prob *= tmp;
//			gh(int[][] S, int[][] H, int k, int m, int b)
		}
		
		// Compute the probabilities for the other columns (column 0 is special...)
		for (int j = 1; j < m; j++) {
//			disp("j = " + j + ", " + probHCol(H, S, k, m, j));
			prob *= probHCol(H, S, k, m, j);
		}
		
		return prob;
	}
	
	static void updateE(int[][] D, int k, int m, int n, int a, int N) throws Exception {
		double sum = 0.0;
		
		// already expanded from call into this guy...
		int[][] expandedD = D;
		
		int[][] S = buildS(expandedD, k+1, m, n); // was D
		disp("S for D");
		disp(S, true);
		
//		disp("" + k);
		ArrayList<int[][]> Hset = buildHSet(expandedD, k+1, m, n, N, a); // was D
//		disp("" + Hset.size());
		
		// NOTE: code checks out up to this point...
		System.out.println("|H set for a = " + a + "| = " + Hset.size());
		double probSum = 0.0;
		ArrayList<Double> probHs = new ArrayList<Double>();
		for (int[][] H : Hset) {
			disp(H, true);
			
			// NOTE: the small Ds (i.e. without -1 row) are those that get put in the time map)
			String key = canonical(toSmallD(add(expandedD, H, k+1, m), k+1, m));
			
			// Compute the probabilities now...
//			disp("" + probH(H, S, k+1, m));
			double tmpSum = probH(H, S, k+1, m); 
			disp("prob for this H = " + tmpSum);
			probSum += tmpSum;
			probHs.add(tmpSum);
			sum += tmpSum * E.get(key);
//			sum += 1.0 * E.get(key);
			
//			disp("" + probH(H, S, k, m));
		}
		disp("probability of p(h) = " + probSum);
		for (Double d : probHs) disp("" + d);
		sum += 1; // 1 + (big sum)
		
		// multiply by 1/(1-p(H0))
		double prod = 1 / (1 - probH(buildHzero(k+1, m), S, k+1, m));
//		disp("" + probH(buildHzero(k, m), S, k, m));
		
		// insert the new expected time value
//		disp("" + sum);
//		disp("" + prod);
		disp("Expected time for D^" + a + " matrix: " + (prod * sum) + " = (" + prod + " * " + sum + ") = ((1/1-Pd(H^0)) * [inner sum])");
		E.put(canonical(toSmallD(D,k+1,m)), prod * sum);
	}
	
	static boolean isValidD(int n, int[][] D, boolean expanded) {
//		System.out.println("PRINTING D TO CHECK");
//		disp(D, true);
		
		int k = D.length; 
		int m = D[0].length;
		
//		if (!isDecreasing(D, k, m)) {
//			return false;
//		}
		
		// NOTE: The rows start at i = 1 because these are expanded Ds - 
		// i.e. they include the -1 row, which isn't used in the constraint check
		
		// 17-1 constraint (decreasing) (correct)
		if (expanded) {
			for (int i = 1; i < k; i++) {
				int last = D[i][0];
				for (int j = 1; j < m; j++) {
					if (last < D[i][j]) {
						return false;
					} else {
						last = D[i][j];
					}
				}
			}
		} else{
			for (int i = 0; i < k; i++) {
				int last = D[i][0];
				for (int j = 1; j < m; j++) {
					if (last < D[i][j]) {
						return false;
					} else {
						last = D[i][j];
					}
				}
			}
		}
		
		// Check row/col boundaries (17-2) (correct)
		if (expanded) {
			int last = D[1][m - 1];
			for (int i = 2; i < k; i++) {
				if (last < D[i][0]){
					return false;
				} else {
					last = D[i][m - 1];
				}
			}
		} else {
			int last = D[0][m - 1];
			for (int i = 1; i < k; i++) {
				if (last < D[i][0]){
					return false;
				} else {
					last = D[i][m - 1];
				}
			}
		}
		
		// Check 17-3 constraint (correct)
		if (expanded) {
			int sum = 1;
			for (int i = 1; i < k; i++) { // start at 0, not -1
				sum += D[i][m-1];
			}
			if (sum < D[1][0]) {
				return false;
			}
		} else {
			int sum = 1;
			for (int i = 0; i < k; i++) { // start at 0
				sum += D[i][m-1];
			}
			if (sum < D[0][0]) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isFullValidD(int[][] D, int n) {
		if (!isValidD(n, D, true)) {
//			disp("failed 17-1/2/3 constraints");
			return false;
		}
		
		int k = D.length;
		int m = D[0].length;
		
		// Check 17-4 constraint (correct)
		// check sum of the first column, must be <= n - 1
		int sum = 0;
		for (int i = 1; i < k; i++) {
			sum += D[i][0];
		}
		if (sum > (n - 1)) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isFullValidH(int[][] H, int[][] S, int k, int m) {
		// row sum constraint (7-2)
		int sum = 0;
		for (int i = 1; i < k; i++) {
			sum += H[i][0];
		}
		if (sum > S[0][0]){
			return false;
		}
		
		// cell constraint (7-1)
		for (int i = 1; i < k; i++) {
			for (int j = 0; j < m; j++) {
				if (H[i][j] > S[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static ArrayList<int[][]> filterDSet(ArrayList<int[][]> Dset, int n) {
		ArrayList<int[][]> result = new ArrayList<int[][]>();
		ArrayList<String> seen = new ArrayList<String>();
		
		for (int[][] D : Dset) {
			if (isFullValidD(D, n)) {
				if (!seen.contains(canonical(D)))
				{
					result.add(D);
					seen.add(canonical(D));
				}
			}
		}
		
		return result;
	}
	
	public static ArrayList<int[][]> filterHSet(ArrayList<int[][]> Hset, int[][] S, int k, int m) {
		ArrayList<int[][]> result = new ArrayList<int[][]>();
		ArrayList<String> seen = new ArrayList<String>();
		
		for (int[][] Htmp : Hset) {
//			if (Htmp[0][0] == 0 && Htmp[0][1] == 1 && Htmp[1][0] == 1 && Htmp[1][1] == 1 && Htmp[2][0] == 1 && Htmp[2][1] == 0) {
//				disp("INSIDE FILTERING AND FOUND IT...");
//				disp(Htmp, true);
//				disp(S, true);
//				disp("" + isFullValidH(Htmp,S,k,m));
//			}
			if (isFullValidH(Htmp,S,k,m)) {
				if (!seen.contains(canonical(Htmp)))
				{
					result.add(Htmp);
					seen.add(canonical(Htmp));
				}
			}
		}
		
		return result;
	}
	
	public static ArrayList<int[][]> push(int k, int m, int n, int[][] D, ArrayList<int[][]> space, boolean check)
	{
		if (space == null)
		{
			space = new ArrayList< int[][] >();
		}
		
		// Check constraints...
//		System.out.println("tried: ");
//		disp(D, true);
		if (!isValidD(n, D, false) && check) {
//			disp("returning now...");
//			disp(D, true);
			return space;
		}
		if (!contains(space, D)) space.add(clone(D));
//		System.out.println("passed");
		
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
							space = push(k, m, n, newD, space, check);
						}
					} else {
						// push to next column...
						if (newD[i][j + 1] < newD[i][j]) {
							newD[i][j]--;
							newD[i][j+1]++;
							space = push(k, m, n, newD, space, check);
						}
					}
				}
			}
		}
		
		return space;
	}
	
	public static int[][] buildD(int[][] D, int k, int m) {
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
		return newD;
	}
	
	public static int[][] toSmallD(int[][] D, int k, int m) {
		int[][] result = new int[k-1][m];
		for (int i = 1; i < k; i++) {
			for (int j = 0; j < m; j++) {
				result[i - 1][j] = D[i][j];
			}
		}
		return result;
	}
	
	public static int[][] buildS(int[][] D, int k, int m, int n) throws Exception {
//		disp("BUILDING S FROM D");
//		disp(D, true);
		
		// Finally, create S
		int[][] S = new int[k][m];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < m; j++) {
				if (i == 0 && j == 0) {
					S[i][j] = n - 1 - D[0][0];
				} else if (i == 1 && j == 0) {
					S[i][j] = D[i - 1][m - 1] - D[i][j] + 1;
				} else if (j == 0) {
					S[i][j] = D[i - 1][m - 1] - D[i][j]; 
				} else {
					S[i][j] = D[i][j - 1] - D[i][j];
				}
			}
		}
		
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < m; j++) {
				if (S[i][j] < 0) {
					throw new Exception("Negative value encountered in the S matrix.");
				}
			}
		}
		
		return S;
	}
	
	public static ArrayList<int[][]> buildH(int[][] H, int[][] S, int i, int j, int k, int m, int N, int a) {
		ArrayList<int[][]> Hset = new ArrayList<int[][]>();
		
		// Incremement H at index and then check to see if within the S bound
		H[i][j]++;
		
		// compute the first row of the H matrix
		// TODO: LEAVE OFF HERE FOR NOW MK?
		
		// TODO: THESE CONSTRAINTS ARE INCORRECT - GET THE INDICES WORKING
		// TODO: D's are correct, S calculation is correct, create H's by hand and then check with this result.
		
		for (int c = 0; c < m; c++) {
			int sum = 0;
			for (int r = 1; r < k; r++) {
				sum += H[r][c];
			}
			H[0][c] = sum;
		}
		
		// Check sum...
		int sum = 0;
		for (int r = 1; r < k; r++) {
			for (int c = 0; c < m; c++) {
				sum += H[r][c];
			}
		}
		if (sum > (N - a)) {
//			disp("failed the sum test...");
//			disp(H, true);
			return Hset;
		}
		
		Hset.add(H);
		
		for (int r = 1; r < k; r++) {
			for (int c = 0; c < m; c++) {
				Hset.addAll(buildH(clone(H), S, r, c, k, m, N, a));
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
		int[][] H = new int[k][m];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < m; j++) {
				H[i][j] = 0;
			}
		}
		return H;
	}
	
//	static boolean isDecreasing(int[][] M, int k, int m) {
//		int last = M[0][0];
//		for (int i = 0; i < k; i++) {
//			for (int j = 0; j < m; j++) {
//				if (i == 0 && j == 0) {
//					last = M[i][j];
//					continue;
//				} else {
//					if (M[i][j] > last) {
//						return false; // not decreasing...
//					} else {
//						last = M[i][j]; // update last value seen
//					}
//				}
//			}
//		}
//		
//		// Check Rows (decreasing order...)
//		for (int j = 0; j < m; j++) {
//			int lastRow = M[0][j]; // pull out the last one
//			for (int i = 1; i < k; i++) {
//				if (lastRow < M[i][j]) {
//					return false;
//				} else {
//					lastRow = M[i][m - 1];
//				}
//			}
//		}
//		
//		// Check columns (17-1)
//		for (int i = 0; i < k; i++) {
//			int lastCol = M[i][0]; // D(i,j = 0)
//			for (int j = 1; j < m; j++) {
//				if (lastCol < M[i][j]) { // D(i,j+1)
//					return false;
//				} else {
//					lastCol = M[i][j];
//				}
//			}
//		}
//		return true;
//	}
	
	public static ArrayList<int[][]> buildHSet(int[][] D, int k, int m, int n, int N, int a) throws Exception {
		ArrayList<int[][]> Hset = new ArrayList<int[][]>();
		ArrayList<int[][]> Hfinal = new ArrayList<int[][]>();
		
		int[][] H = buildHzero(k, m);
//		disp(H,true);
		int[][] S = buildS(D, k, m, n);
//		disp(S, true);
		
		// TODO: start here, look at S and all Hs that are built... fix some cases for D...
		
		for (int i = 1; i < k; i++) {
			for (int j = 0; j < m; j++) {
				Hset.addAll(buildH(clone(H), S, i, j, k, m, N, a));
			}
		}
		
//		for (int[][] ht : Hset) {
//			disp(ht, true);
//		}
		
		disp("" + Hset.size());
		Hset = filterHSet(Hset, S, k, m);
		disp("" + Hset.size());
		
//		disp("filtered set");
//		for (int[][] Htmp : Hset) {
//			disp(Htmp, true);
//		}
		
//		for (int[][] Htmp : Hset) {
//			if (Htmp[0][0] == 0 && Htmp[0][1] == 1 && Htmp[1][0] == 0 && Htmp[1][1] == 1 && Htmp[2][0] == 0 && Htmp[2][1] == 0) {
//				disp("Finally...");
//				disp(Htmp, true);
//			}
//		}
		
		// Now, filter out those that are not in the Dspace
//		disp("Checking to see if D+H is in D-" + N + " subspace");
//		disp("" + Hset.size());
//		if (Hset.size() == 0) System.exit(-1); 
//		disp("H set");
		for (int[][] Ht : Hset) {
			// Check to make sure that D+H is in the D space
			int[][] addition = add(D, Ht, k, m);
//			disp("addition test...");
//			disp(D, true);
//			disp(Ht, true);
//			disp(addition, true);
			boolean include = true;
			if (!(isFullValidD(addition, n))) {
				include = false;
			}
			
			if (include) {
//				disp("ADDED!");
//				disp(Ht, true);
				Hfinal.add(Ht);
			}
		}
		
		// TODO: need to uniquify the Hset (they're probably not unique...)
//		disp("" + Hfinal.size());
		return Hfinal;
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
	
	static void disp(String m) {
		System.out.println(m);
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
	
	static int[][] buildDmax(int a, int k, int m, int n) {
		int[][] Dmax = new int[k][m];
		int alloc = 0;
		
		// Create the max'd out configuration
		for (int i = 0; i < k && alloc < a; i++) {
			for (int j = 0; j < m && alloc < a; j++) {
				if (alloc + n - 1 < a) { // max out...
					Dmax[i][j] = n - 1;
					alloc += n - 1;
				} else if (a - alloc <= n - 1) { // fill in the difference...
					Dmax[i][j] = a - alloc;
					alloc += a - alloc; // this will cause us to break out of the loop
				}
			}
		}
		
		// While not valid, continue pushing down one by one
		if (!isValidD(n, Dmax, false)) {
//			System.out.println("DEFAULT MAX IS INVALID - PUSHING DOWN TO GET OTHERS");
			ArrayList<int[][]> Dset = push(k, m, n, Dmax, null, false);
			for (int i = 0; i < Dset.size(); i++) {
				if (isValidD(n, Dset.get(i), false)) {
					Dmax = Dset.get(i);
					break;
				}
			}
		}
		
		return Dmax; // we'll never get here
	}

	public static void main(String[] args) throws Exception
	{
		int k = 2; // num children
		int m = 2; // num messages
		int n = 5; // num nodes
		int N = (n - 1) * m;
		
		disp("" + binom(0, 0));
		disp("" + binom(1,0));
		disp("" + binom(0,1));
		disp("" + fact(0));
		disp("" + fact(1));
		disp("" + fact(2));
		disp("" + fact(5));
		disp("" + Math.pow(1.0 - p2, 100));
		disp("" + Math.pow(1.0, 100));
		disp("" + Math.pow(1.0, 0));
		
		// Create the estimated time collection
		E = new HashMap<String, Double>();
		
		// Create the initial max configuration
		int[][] Dmax = buildDmax(N, k, m, n);
		
		// Generate the list of all matrices in the D^8 (D*)
		ArrayList<int[][]> D8 = push(k, m, n, Dmax, null, true);
		
		// Initialize the ET(D) for all matrices in the d = 8
//		System.out.println("What we got back,...");
//		for (int[][] D : D8) {
//			disp(D, true);
//			E.put(canonical(D), 0.0);
//		}
		
		disp("Inserting initial D^" + N + " times");
		disp(Dmax, true);
		
		// Expand each D so it can be filtered properly...
		ArrayList<int[][]> newD8 = new ArrayList<int[][]>();
		for (int[][] D : D8) {
			newD8.add(buildD(D, k, m));
		}
		
		D8 = filterDSet(newD8, n);
		disp("D^" + N + " subspace");
		for (int[][] D : D8) {
//			int[][] expandedD = buildD(D,k,m);
			disp(D, true);
			E.put(canonical(toSmallD(D,k+1,m)), 0.0);
//			disp("" + E.keySet());
		}
		
//		int[][] Dtest = {{4, 3}, {3, 2}, {1, 1}};
//		disp("IS THIS VALID?");
//		disp("" + isFullValidD(Dtest, n));
//		disp("" + E.keySet());
		
		// Let the recursion begin!
		for (int a = N - 1; a >= 0; a--) {
			Dmax = buildDmax(a, k, m, n);
//			disp(Dmax, true);
			ArrayList<int[][]> Dset = push(k, m, n, Dmax, null, true);
//			Dset = filterDSet(Dset, n);
//			disp("" + Dset.size());
			
			// Expand out the Ds for filtering
			ArrayList<int[][]> newDset = new ArrayList<int[][]>();
//			disp("" + Dset.size());
			for (int[][] D : Dset) {
				newDset.add(buildD(D, k, m));
//				disp(buildD(D, k, m), true);
			}
			
			Dset = filterDSet(newDset, n);
			System.out.println("|D-" + a + " subspace| = " + Dset.size());
//			disp(Dmax, true);
//			System.out.println(isValid(n, Dmax));
//			System.out.println(Dset);
			for (int[][] D : Dset) {
				disp("D^" + a + " matrix");
				disp(D, true);
				updateE(D, k, m, n, a, N);
//				disp("" + E.get(canonical(toSmallD(D,k+1,m))));
			}
//			if (i == 7) return;
//			break;
		}
		
		int[][] zero = buildHzero(k, m);
		disp("");
		disp("Expected time: " + E.get(canonical(zero)));
	}
}



