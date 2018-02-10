package utils;

public class ParlayStats {
	float profit;
	float winBets;
	float loseBets;

	public ParlayStats(float profit, float winBets, float loseBets) {
		super();
		this.profit = profit;
		this.winBets = winBets;
		this.loseBets = loseBets;
	}

	public float getProfit() {
		return profit;
	}

	public void setProfit(float profit) {
		this.profit = profit;
	}

	public float getWinBets() {
		return winBets;
	}

	public void setWinBets(float winBets) {
		this.winBets = winBets;
	}

	public float getLoseBets() {
		return loseBets;
	}

	public void setLoseBets(float loseBets) {
		this.loseBets = loseBets;
	}

	public void add(ParlayStats parlayStats) {
		setProfit(profit + parlayStats.getProfit());
		setWinBets(winBets + parlayStats.getWinBets());
		setLoseBets(loseBets + parlayStats.getLoseBets());
	}

	public String toString() {
		return "Total " + profit + " " + winBets + "W " + loseBets + "L " + "rate: "
				+ String.format("%.2f", 100 * (winBets / loseBets)) + "%" + " yield: "
				+ String.format("%.2f", 100 * (profit / (loseBets + winBets))) + "%";
	}

}
