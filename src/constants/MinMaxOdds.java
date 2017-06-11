package constants;

import java.util.HashMap;

public class MinMaxOdds {

	public static final String[] DONT = { "E0", "EC", "E1", "E2", "SC0", "I1", "I2", "F1", "T1", "B1" };
	public static final String[] DRAW = { "I1", "I2", "F2", "P1", "D1", "D2", "SP2" };
	public static final String[] SHOTS = { "E0", "E1", "E2", "E3", "EC", "SC0", "D1", "SP1", "I1", "F1" };
	public static final String[] PFS = { "E0", "E1", "E2", "E3", "EC", "SC0", "SP1", "D1", "I1", "F1" };
	public static final String[] SHOTSEQUIVALENTS = { "ENG", "ENG2", "ENG3", "ENG4", "ENG5", "SC0", "GER", "SPA", "IT",
			"FR" };
	public static final String[] SHOTSDONT = { "I1", "F1", "E1", "E3" };
	public static final String[] MANUAL = { "ARG", "ARG2", "BRA", "BRB", "SWE", "NOR", "USA", "ICE", "FIN", "JP", "SWI",
			"DEN", "AUS", "CZE", "RUS", "NED", "POR", "BEL", "FR2", "TUR", "GRE", "HUN", "D2", "IT2", "POL", "IT",
			"SPA", "SPA2", "GER", "GER2", "FR", "ENG", "SCO", "ENG2", "ENG3", "ENG4", "ENG5", "BUL", "CRO", "SLO",
			"SLK" };

	public static final String[] FULL = { "SWI", "BRA", "ENG", "SPA" };

	public static HashMap<String, String> equivalents = new HashMap<>();
	public static HashMap<String, String> reverseEquivalents = new HashMap<>();

	static {
		equivalents.put("IT", "I1");
		equivalents.put("ENG", "E0");
		equivalents.put("ENG2", "E1");
		equivalents.put("ENG3", "E2");
		equivalents.put("ENG4", "E3");
		equivalents.put("ENG5", "EC");
		equivalents.put("GER", "D1");
		equivalents.put("FR", "F1");
		equivalents.put("SCO", "SC0");
		equivalents.put("SPA", "SP1");
		equivalents.put("SPA2", "SP2");
		// -----------------------
		reverseEquivalents.put("I1", "IT");
		reverseEquivalents.put("E0", "ENG");
		reverseEquivalents.put("E1", "ENG2");
		reverseEquivalents.put("E2", "ENG3");
		reverseEquivalents.put("E3", "ENG4");
		reverseEquivalents.put("EC", "ENG5");
		reverseEquivalents.put("D1", "GER");
		reverseEquivalents.put("F1", "FR");
		reverseEquivalents.put("SC0", "SCO");
		reverseEquivalents.put("SP1", "SPA");
		reverseEquivalents.put("SP2", "SPA2");
	}

}