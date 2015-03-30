
public class testCase {
	public static void main(String[] args) {
		new testCase().test();
	}

	void test() {
		QualityPolygonsVis.vis = false;
		for (long seed = 2, end = seed + 10; seed < end; seed++) {
			new QualityPolygonsVis(seed);
			System.out.println();
		}
	}
}
