package dictionaries;

public class EN {

	public static String getAlias(String apiName) {
		switch (apiName) {
		case "Manchester United FC":
			return "Man United";
		case "West Bromwich Albion FC":
			return "West Brom";
		case "Queens Park Rangers":
			return "QPR";
		case "West Ham United FC":
			return "West Ham";
		case "Stoke City FC":
			return "Stoke";
		case "Leicester City FC":
			return "Leicester";
		case "Arsenal FC":
			return "Arsenal";
		case "Liverpool FC":
			return "Liverpool";
		case "Newcastle United FC":
			return "Newcastle";
		case "Burnley FC":
			return "Burnley";
		case "Aston Villa FC":
			return "Aston Villa";
		case "Swansea City FC":
			return "Swansea";
		case "Southampton FC":
			return "Southampton";
		case "Chelsea FC":
			return "Chelsea";
		case "Crystal Palace FC":
			return "Crystal Palace";
		case "Everton FC":
			return "Everton";
		case "Tottenham Hotspur FC":
			return "Tottenham";
		case "Hull City FC":
			return "Hull";
		case "Sunderland AFC":
			return "Sunderland";
		case "Manchester City FC":
			return "Man City";
		default:
			return "";
		}
	}
}
