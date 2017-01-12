package eu.tankernn.mines;

public class Settings {
	public final int boardWidth, boardHeight, area;
	public final Pos[] pattern;
	public final int mines;

	public Settings(Pos[] pattern, int boardWidth, int boardHeight, int mines) {
		this.boardWidth = boardWidth;
		this.boardHeight = boardHeight;
		this.pattern = pattern;
		this.mines = mines;
		this.area = boardHeight * boardWidth;
	}
}
