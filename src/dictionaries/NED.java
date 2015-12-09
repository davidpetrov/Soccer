package dictionaries;

public class NED {
	public static String getAlias(String apiName) {
		switch (apiName) {
		case "SC Heerenveen":
			return "Heerenveen";
		case "SC Cambuur-Leeuwarden":
			return "Cambuur";
		case "NAC Breda":
			return "NAC Breda";
		case "Heracles Almelo":
			return "Heracles";
		case "ADO Den Haag":
			return "Den Haag";
		case "Ajax Amsterdam":
			return "Ajax";
		case "Go Ahead Eagles Deventer":
			return "Go Ahead Eagles";
		case "Willem II":
			return "Willem II";
		case "Feyenoord Rotterdam":
			return "Feyenoord";
		case "PSV Eindhoven":
			return "PSV Eindhoven";
		case "FC Twente Enschede":
			return "Twente";
		case "FC Dordrecht":
			return "Dordrecht";
		case "Vitesse Arnhem":
			return "Vitesse";
		case "AZ Alkmaar":
			return "AZ Alkmaar";
		case "FC Utrecht":
			return "Utrecht";
		case "Excelsior":
			return "Excelsior";
		case "FC Groningen":
			return "Groningen";
		case "PEC Zwolle":
			return "Zwolle";
		default:
			return "";
		}
	}
}
