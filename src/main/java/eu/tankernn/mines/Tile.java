package eu.tankernn.mines;

public class Tile {
	public final boolean isMine;
	private TileState state = TileState.HIDDEN;
	private int minesAround;
	public final Pos pos;

	public Tile(boolean mine, Pos pos) {
		this.isMine = mine;
		this.pos = pos;
	}

	public void setMinesAround(int minesAround) {
		this.minesAround = minesAround;
		this.state = TileState.CHECKED;
	}

	public int getMinesAround() {
		return minesAround;
	}

	public TileState getState() {
		return state;
	}

	public void setState(TileState state) {
		this.state = state;
	}

	public void toggleFlag() {
		if (this.state.equals(TileState.HIDDEN))
			this.setState(TileState.FLAGGED);
		else if (this.state.equals(TileState.FLAGGED))
			this.setState(TileState.HIDDEN);
	}

	public enum TileState {
		HIDDEN('*'), CHECKED(' '), EXPLODED('X'), FLAGGED('P');

		public final char appearance;

		private TileState(char value) {
			appearance = value;
		}
	}

	public String toString() {
		return state.equals(TileState.CHECKED) ? Integer.toString(getMinesAround()) : Character.toString(getState().appearance);
	}
}
