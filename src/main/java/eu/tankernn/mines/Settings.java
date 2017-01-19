package eu.tankernn.mines;

import java.util.List;

public class Settings {
	public final int boardWidth, boardHeight, area;
	public final List<Pos> pattern;
	public final int mines;

	public Settings(List<Pos> pattern, int boardWidth, int boardHeight, int mines) {
		this.boardWidth = boardWidth;
		this.boardHeight = boardHeight;
		this.pattern = pattern;
		this.mines = mines;
		this.area = boardHeight * boardWidth;
	}
}
