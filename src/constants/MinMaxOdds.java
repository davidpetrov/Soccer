package constants;

import java.lang.reflect.Field;
import java.util.Arrays;

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

	public static float getMinOdds(String league) {
		switch (league) {
		case "E0":
			return 1.76f;
		case "E1":
			return 1.8f;
		case "E2":
			return 1.9f;
		case "E3":
			return 1.96f;
		case "EC":
			return 1.86f;
		case "SC0":
			return 1.68f;
		case "SC1":
			return 1.84f;
		case "SC2":
			return 1f;
		case "SC3":
			return 1.68f;
		case "D1":
			return 1.70f;
		case "D2":
			return 1.62f;
		case "SP1":
			return 1.94f;
		case "SP2":
			return 1.65f;
		case "I1":
			return 1.95f;
		case "I2":
			return 1.66f;
		case "F1":
			return 1.6f;
		case "F2":
			return 1.58f;
		case "N1":
			return 1.5f;
		case "B1":
			return 1.75f;
		case "P1":
			return 1.6f;
		case "G1":
			return 1.6f;
		case "T1":
			return 1.78f;
			
		default:
			return 1.7f;
		}
	}

	public static float getMaxOdds(String league) {
		switch (league) {
		case "E0":
			return 2.26f;
		case "E1":
			return 2.2f;
		case "E2":
			return 2.14f;
		case "E3":
			return 2.1f;
		case "EC":
			return 2.08f;
		case "SC0":
			return 2.2f;
		case "SC1":
			return 10f;
		case "SC2":
			return 10f;
		case "SC3":
			return 2.2f;
		case "D1":
			return 2.2f;
		case "D2":
			return 2.2f;
		case "SP1":
			return 2.22f;
		case "SP2":
			return 2.25f;
		case "I1":
			return 2.3f;
		case "I2":
			return 2.3f;
		case "F1":
			return 2.3f;
		case "F2":
			return 2.2f;
		case "N1":
			return 2.5f;
		case "B1":
			return 2.1f;
		case "P1":
			return 2.5f;
		case "G1":
			return 2.2f;
		case "T1":
			return 10f;
		default:
			return 2.1f;
		}
	}

	public static void main(String... arfds) {
		System.out.println(getMinOdds("E11"));
	}
}
