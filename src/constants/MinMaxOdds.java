package constants;

public class MinMaxOdds {
	public static final Float[] E0 = { 1.76f, 2.26f };
	public static final Float[] E1 = { 1.8f, 2.2f };
	public static final Float[] E2 = { 1.9f, 2.14f };
	public static final Float[] E3 = { 1.96f, 2.1f };
	public static final Float[] EC = { 1.86f, 2.08f };
	public static final Float[] SC0 = { 1.68f, 2.2f };
	public static final Float[] SC1 = { 1.84f, 10f };
	public static final Float[] SC2 = { 1f, 10f };
	public static final Float[] SC3 = { 1.68f, 2.2f };

	public static final String[] DONT = { "E1","E2", "E3", "SC0", /*"D1", "D2",*/ "I1", "I2", /*"F1", "F2",*/ "N1" };

	public static float getMinOdds(String league) {
		switch (league) {
		case "E0":
			return 1.66f;
		case "E1":
			return 1.96f;
		case "E2":
			return 1.9f;
		case "E3":
			return 2.22f;
		case "EC":
			return 1.78f;
		case "SC0":
			return 1.82f;
		case "SC1":
			return 1.88f;
		case "SC2":
			return 1.74f;
		case "SC3":
			return 1.68f;
		case "D1":
			return 1.86f;
		case "D2":
			return 1.88f;
		case "SP1":
			return 1.36f;
		case "SP2":
			return 1.76f;
		case "I1":
			return 2.04f;
		case "I2":
			return 2.24f;
		case "F1":
			return 1.6f;
		case "F2":
			return 2.22f;
		case "N1":
			return 1.62f;
		case "B1":
			return 1.74f;
		case "P1":
			return 1.66f;
		case "G1":
			return 1.76f;
		case "T1":
			return 1.94f;

		default:
			return 1.7f;
		}
	}

	public static float getMaxOdds(String league) {
		switch (league) {
		case "E0":
			return 10f;
		case "E1":
			return 10f;
		case "E2":
			return 10f;
		case "E3":
			return 2.36f;
		case "EC":
			return 2.3f;
		case "SC0":
			return 2.36f;
		case "SC1":
			return 10f;
		case "SC2":
			return 2.26f;
		case "SC3":
			return 2.4f;
		case "D1":
			return 2.3f;
		case "D2":
			return 2.22f;
		case "SP1":
			return 2.1f;
		case "SP2":
			return 10f;
		case "I1":
			return 2.22f;
		case "I2":
			return 10f;
		case "F1":
			return 10f;
		case "F2":
			return 2.4f;
		case "N1":
			return 2.5f;
		case "B1":
			return 2.3f;
		case "P1":
			return 10f;
		case "G1":
			return 2.22f;
		case "T1":
			return 2.34f;
		default:
			return 2.1f;
		}
	}

	public static void main(String... arfds) {
		System.out.println(getMinOdds("E11"));
	}
}