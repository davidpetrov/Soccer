package settings;

public class ShotsSettings {
	float thold;
	float cotOvers;
	float cotUnders;
	public boolean onlyOvers;
	public boolean onlyUnders;
	public boolean doNotPlay;

	public ShotsSettings() {
	}

	public ShotsSettings withTH(float th) {
		thold = th;
		return this;
	}

	public ShotsSettings withCot(float cotOvers, float cotUnders) {
		this.cotOvers = cotOvers;
		this.cotUnders = cotUnders;
		return this;
	}

	public void onlyUnders() {
		onlyUnders = true;
	}

	public void onlyOvers() {
		onlyOvers = true;
	}

	public void doNotPlay() {
		doNotPlay = true;
	}

}
