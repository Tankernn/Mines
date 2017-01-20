package eu.tankernn.mines;

public class Pos {
	public final int x, y;

	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		return x + y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Pos) {
			Pos other = (Pos) obj;
			return other.x == this.x && other.y == this.y;
		} else
			return false;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	public Pos add(Pos other) {
		return new Pos(this.x + other.x, this.y + other.y);
	}
}
