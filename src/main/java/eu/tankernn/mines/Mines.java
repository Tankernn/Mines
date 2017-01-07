package eu.tankernn.mines;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.lwjgl.util.vector.Vector2f;

import eu.tankernn.gameEngine.GameLauncher;
import eu.tankernn.gameEngine.TankernnGame;
import eu.tankernn.gameEngine.loader.textures.Texture;
import eu.tankernn.gameEngine.renderEngine.gui.GuiRenderer;
import eu.tankernn.gameEngine.renderEngine.gui.GuiTexture;
import eu.tankernn.mines.Tile.TileState;

public class Mines extends TankernnGame {
	public static String GAME_NAME = "Minesweeper";

	private Random rand = new Random();
	private Scanner sc = new Scanner(System.in);
	private GuiRenderer renderer;
	
	private Texture hidden, checked, exploded, flagged;

	private int boardWidth, boardHeight;
	private Tile[][] tiles;
	private Pos[] pattern = {new Pos(0, 1), new Pos(0, -1), new Pos(1, 0), new Pos(1, 1), new Pos(1, -1), new Pos(-1, 0), new Pos(-1, -1), new Pos(-1, 1)};
	private int totalMines;
	private boolean running;

	public Mines(String name) {
		super(name);
		renderer = new GuiRenderer(loader);

		totalMines = 10;
		boardWidth = boardHeight = 7;

		startGame();
	}

	public void startGame() {
		List<Pos> minePositions = new ArrayList<Pos>();
		for (int i = 0; i < totalMines; i++) {
			minePositions.add(new Pos(rand.nextInt(boardWidth), rand.nextInt(boardHeight)));
		}

		tiles = new Tile[boardWidth][boardHeight];

		for (int y = 0; y < boardHeight; y++) {
			for (int x = 0; x < boardWidth; x++) {
				tiles[x][y] = new Tile(minePositions.contains(new Pos(x, y)), new Pos(x, y));
			}
		}
		
		running = true;
	}

	public void check(Tile tile) {
		if (running)
			if (tile.isMine)
				lose();
			else
				calculateMinesAround(tile);
	}

	public void calculateMinesAround(Tile tile) {
		int count = 0;
		List<Tile> testTiles = new ArrayList<Tile>();

		for (int i = 0; i < pattern.length; i++) {
			try {
				testTiles.add(tiles[tile.pos.x + pattern[i].x][tile.pos.y + pattern[i].y]);
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
		}

		for (Tile testTile : testTiles)
			if (testTile.isMine)
				count++;

		tile.setMinesAround(count);

		if (count == 0)
			for (Tile testTile : testTiles)
				if (testTile.getState().equals(Tile.TileState.HIDDEN))
					calculateMinesAround(testTile);
	}

	private void lose() {
		running = false;
		for (Tile[] tArr : tiles)
			for (Tile t : tArr)
				if (t.isMine)
					t.setState(TileState.EXPLODED);
	}

	@Override
	public void update() {
		
		String[] command = sc.nextLine().split(" ");
		
		int x = Integer.parseInt(command[1]), y = Integer.parseInt(command[2]);
		
		switch (command[0]) {
		case "flag":
			tiles[x][y].toggleFlag();
			break;
		case "check":
			check(tiles[x][y]);
			break;
		default:
			System.out.println("Unknown command.");
		}
		
		super.update();
	}

	@Override
	public void render() {
		List<GuiTexture> toRender = new ArrayList<GuiTexture>();
		
		for (int y = 0; y < boardHeight; y++) {
			for (int x = 0; x < boardWidth; x++) {
				Tile t = tiles[x][y];
				System.out.print(t.getState().equals(TileState.CHECKED) ? Integer.toString(t.getMinesAround()) : t.getState().appearance);
				Texture tex;
				switch (t.getState()) {
				case CHECKED:
					tex = checked;
					break;
				case EXPLODED:
					tex = exploded;
					break;
				case FLAGGED:
					tex = flagged;
					break;
				case HIDDEN:
					tex = hidden;
					break;
				default:
					tex = hidden;
					break;
				}
				toRender.add(new GuiTexture(tex, new Vector2f(), new Vector2f()));
			}
			System.out.println();
		}
		System.out.println("-------------------");
		
		
		renderer.render(toRender);
		

		super.render();
	}
	
	@Override
	public void cleanUp() {
		sc.close();
		super.cleanUp();
	}

	public static void main(String[] args) {
		GameLauncher.init(GAME_NAME, 800, 800);
		GameLauncher.launch(new Mines(GAME_NAME));
	}
}
