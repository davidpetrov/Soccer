package tables;

public class Position implements Comparable<Position> {
	public String team;
	public int played;
	public int wins;
	public int draws;
	public int losses;
	public int scored;
	public int conceded;
	public int diff;
	public int points;

	public int homeplayed;
	public int homewins;
	public int homedraws;
	public int homelosses;
	public int homescored;
	public int homeconceded;
	public int homediff;
	public int homepoints;

	public int awayplayed;
	public int awaywins;
	public int awaydraws;
	public int awaylosses;
	public int awayscored;
	public int awayconceded;
	public int awaydiff;
	public int awaypoints;

	@Override
	public int compareTo(Position o) {
		if (points != o.points)
			return ((Integer) points).compareTo((Integer) o.points);
		else if (diff != o.diff)
			return ((Integer) diff).compareTo((Integer) o.diff);
		else if (scored != o.scored)
			return ((Integer) scored).compareTo((Integer) o.scored);
		else
			return team.compareTo(o.team);

	}
}
