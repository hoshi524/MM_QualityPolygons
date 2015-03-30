import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Problem: QualityPolygons
 * URL: http://community.topcoder.com/longcontest/?module=ViewProblemStatement&rd=14589&pm=11441
 * 
 * オレオレ問題文：
 * 間違ってる可能性あるので、詳しくは上記のURLからProblem Statementみてね！
 * 
 * 2次元平面上で、頂点をNP個、与えられる。
 * ポリゴンを作ることが目的で、スコアは各ポリゴンの頂点数^2の合計
 * ポリゴンには条件があり
 * ・凸であること
 * ・最小の辺/最大の辺 >= 1-(sidesDiff/100)
 * ・各頂点から、最小の重心距離/最大の重心距離 >= 1-(radiiDiff/100)
 * 正n角形に近くないとポリゴンにできない？
 * 
 * 50 <= NP <= 5000
 * 1 <= sidesDiff <= 20
 * 1 <= radiiDiff <= 20
 */
public class QualityPolygons {

	Point points[];
	int sidesDiff;
	int radiiDiff;
	int NP;
	List<List<Integer>> res;

	public String[] choosePolygons(int[] pointsInt, int sidesDiff, int radiiDiff) {
		this.sidesDiff = sidesDiff;
		this.radiiDiff = radiiDiff;
		NP = pointsInt.length / 2;
		points = new Point[NP];
		for (int i = 0; i < NP; i++) {
			points[i] = new Point(i, pointsInt[i * 2], pointsInt[i * 2 + 1]);
		}
		res = new ArrayList<>();

		int rest = NP;
		boolean used[] = new boolean[NP];
		SecureRandom rnd = null;
		try {
			rnd = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		for (int x = 0xffff; x > 0 && rest >= 3; x--) {
			List<Point> plist = new ArrayList<>();
			List<Point> use = new ArrayList<>();
			for (int i = 0; i < NP; i++) {
				if (!used[i]) {
					use.add(points[i]);
				}
			}
			for (int i = 0; i < 3; i++) {
				Point p = use.get(rnd.nextInt(use.size()));
				plist.add(p);
				use.remove(p);
			}
			if (validatePoly(plist)) {
				res.add(getID(plist));
				for (Point p : plist) {
					used[p.id] = true;
				}
			}
		}

		return getResult(res);
	}

	List<Integer> getID(List<Point> plist) {
		List<Integer> ids = new ArrayList<>(plist.size());
		for (Point p : plist) {
			ids.add(p.id);
		}
		return ids;
	}

	String[] getResult(List<List<Integer>> res) {
		String[] s = new String[res.size()];
		for (int i = 0; i < s.length; i++) {
			List<Integer> poly = res.get(i);
			StringBuilder sb = new StringBuilder();
			sb.append(poly.get(0));
			for (int j = 1; j < poly.size(); j++) {
				sb.append(" ").append(poly.get(j));
			}
			s[i] = sb.toString();
		}
		return s;
	}

	boolean validatePoly(List<Point> plist) {
		Point p[] = new Point[plist.size()];
		for (int i = 0; i < p.length; i++) {
			p[i] = plist.get(i);
		}
		return validatePoly(p);
	}

	boolean validatePoly(Point p[]) {
		//check that the polygon satisfies all conditions
		int i, j, n = p.length;
		if (n < 3)
			return false;

		//edges must have approximately equal length
		long maxLen2 = G2D.dist2(p[0], p[n - 1]), minLen2 = maxLen2, len2;
		for (i = 0; i < n - 1; i++) {
			len2 = G2D.dist2(p[i], p[i + 1]);
			maxLen2 = Math.max(maxLen2, len2);
			minLen2 = Math.min(minLen2, len2);
		}
		if (100 * 100 * minLen2 < maxLen2 * sq(100 - sidesDiff)) {
			return false;
		}

		//distance from each vertice to polygon center must be the same
		long sumX = 0, sumY = 0;
		for (i = 0; i < n; i++) {
			sumX += p[i].x;
			sumY += p[i].y;
		}
		long maxR2 = sq(sumX - n * p[0].x) + sq(sumY - n * p[0].y), minR2 = maxR2, R2;
		for (i = 1; i < n; i++) {
			R2 = sq(sumX - n * p[i].x) + sq(sumY - n * p[i].y);
			maxR2 = Math.max(maxR2, R2);
			minR2 = Math.min(minR2, R2);
		}
		if (100 * 100 * minR2 < maxR2 * sq(100 - radiiDiff)) {
			return false;
		}

		//must be convex - for each side all other points lay strictly to one side of it
		Point p1, p2;
		long cross;
		for (i = 0; i < n; i++) {
			//use p[i] and p[i+1] as reference vector
			p1 = G2D.substr(p[(i + 1) % n], p[i]);
			//and check that all other points have the same direction with it
			p2 = G2D.substr(p[(i + 2) % n], p[i]);
			cross = G2D.cross(p1, p2);
			if (cross == 0)
				return false;
			else
				cross /= Math.abs(cross);
			for (j = 3; j < n; j++) {
				if (cross * G2D.cross(p1, G2D.substr(p[(i + j) % n], p[i])) <= 0)
					return false;
			}
		}

		return true;
	}

	long sq(long a) {
		return a * a;
	}

	static class Point {
		int id, x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Point(int id, int x, int y) {
			this.id = id;
			this.x = x;
			this.y = y;
		}
	}

	static class G2D {
		public static Point substr(Point p1, Point p2) {
			return new Point(p1.x - p2.x, p1.y - p2.y);
		}

		public static int norm2(Point p) {
			return p.x * p.x + p.y * p.y;
		}

		public static int dot(Point p1, Point p2) {
			return p1.x * p2.x + p1.y * p2.y;
		}

		public static int cross(Point p1, Point p2) {
			return p1.x * p2.y - p1.y * p2.x;
		}

		public static int dist2(Point p1, Point p2) {
			return norm2(substr(p1, p2));
		}
	}
}
