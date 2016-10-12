package main;

import java.util.ArrayList;

public class GoalLines {

	public Line line1;
	public Line line2;
	public Line main;
	public Line line3;
	public Line line4;

	public GoalLines(Line line1, Line line2, Line main, Line line3, Line line4) {
		super();
		this.line1 = line1;
		this.line2 = line2;
		this.main = main;
		this.line3 = line3;
		this.line4 = line4;
	}

	public GoalLines(Line main) {
		super();
		this.main = main;
	}

	public Line[] getArrayLines() {
		Line[] lines = { line1, line2, main, line3, line4 };
		return lines;
	}

	public Line[] get3Lines() {
		Line[] lines = { line2, main, line3 };
		return lines;
	}

}
