package eu.tankernn.mines;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import eu.tankernn.gameEngine.GameLauncher;
import eu.tankernn.gameEngine.TankernnGame;
import eu.tankernn.gameEngine.loader.font.FontType;
import eu.tankernn.gameEngine.loader.font.GUIText;
import eu.tankernn.gameEngine.loader.textures.Texture;
import eu.tankernn.gameEngine.renderEngine.gui.GuiRenderer;
import eu.tankernn.gameEngine.renderEngine.gui.GuiTexture;
import eu.tankernn.gameEngine.util.InternalFile;
import eu.tankernn.mines.Tile.TileState;

public class Mines extends TankernnGame {
	public static final String GAME_NAME = "Minesweeper";
	public static final Pos[] DEFAULT_PATTERN = { new Pos(0, 1), new Pos(0, -1), new Pos(1, 0), new Pos(1, 1), new Pos(1, -1),
			new Pos(-1, 0), new Pos(-1, -1), new Pos(-1, 1) };

	private Random rand = new Random();
	private DecimalFormat format = new DecimalFormat("0.000 sec");
	private GuiRenderer renderer;
	private PatternEditor editor;

	private Texture hidden, exploded, flagged;
	private Texture[] checked;
	private GUIText timeText;
	private long startTime;

	private int boardWidth = 20, boardHeight = 20;
	private int tileWidth, tileHeight;
	private Tile[][] tiles;
	private Pos[] pattern = DEFAULT_PATTERN;
	private int totalMines = 200;
	private int hiddenTiles;
	private boolean running = false;
	private boolean justClicked = false;

	public Mines(String name) {
		super(name);
		renderer = new GuiRenderer(loader);

		tileWidth = Display.getWidth() / boardWidth;
		tileHeight = Display.getHeight() / boardHeight;

		try {
			hidden = loader.loadTexture("hidden.png");
			checked = new Texture[10];
			for (int i = 0; i < checked.length; i++)
				checked[i] = loader.loadTexture(i + ".png");
			exploded = loader.loadTexture("exploded.png");
			flagged = loader.loadTexture("flagged.png");
			FontType font = new FontType(loader.loadTexture("arial.png"), new InternalFile("arial.fnt"));
			timeText = new GUIText(format.format(0), 1f, font, new Vector2f(0f, 0f), 100, false);
			GUIText helpText = new GUIText("R - reset, \n E - edit pattern", 1f, font, new Vector2f(0.8f, 0f), 0.15f, false);
			textMaster.loadText(timeText);
			textMaster.loadText(helpText);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		startGame(DEFAULT_PATTERN);
	}

	public void startGame(Pos[] pattern) {
		this.pattern = pattern;
		startGame();
	}

	private void startGame() {
		startTime = Sys.getTime();
		hiddenTiles = boardHeight * boardWidth;
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
		hiddenTiles--;

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

	private void win() {
		running = false;
	}

	@Override
	public void update() {
		// { // Command selection
		// String[] command = sc.nextLine().split(" ");
		//
		// int x = Integer.parseInt(command[1]), y =
		// Integer.parseInt(command[2]);
		//
		// switch (command[0]) {
		// case "flag":
		// tiles[x][y].toggleFlag();
		// break;
		// case "check":
		// check(tiles[x][y]);
		// break;
		// default:
		// System.out.println("Unknown command.");
		// }
		// }
		{ // Mouse selection
			if (Mouse.isButtonDown(1) || Mouse.isButtonDown(0)) {
				if (!justClicked && running) {
					justClicked = true;
					int x, y;
					x = Mouse.getX() / tileWidth;
					y = Mouse.getY() / tileHeight;

					if (Mouse.isButtonDown(0)) {
						check(tiles[x][y]);
					} else {
						tiles[x][y].toggleFlag();
					}
				}
			} else
				justClicked = false;
		}

		if (running)
			timeText.setText(format.format(((float) (Sys.getTime() - startTime)) / Sys.getTimerResolution()));
		else if (Keyboard.isKeyDown(Keyboard.KEY_R))
			startGame();
		else if (Keyboard.isKeyDown(Keyboard.KEY_E) && (editor == null || !editor.isShowing()))
			editor = new PatternEditor(this);

		if (hiddenTiles == totalMines)
			win();

		super.update();
	}

	@Override
	public void render() {
		List<GuiTexture> toRender = new ArrayList<GuiTexture>();

		for (int y = 0; y < boardHeight; y++) {
			for (int x = 0; x < boardWidth; x++) {
				Tile t = tiles[x][y];
				System.out.print(t.getState().equals(TileState.CHECKED) ? Integer.toString(t.getMinesAround())
						: t.getState().appearance);
				Texture tex;
				switch (t.getState()) {
				case CHECKED:
					tex = checked[t.getMinesAround()];
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
				Vector2f scale = new Vector2f(1f / boardWidth, 1f / boardHeight);
				toRender.add(new GuiTexture(tex,
						new Vector2f(2 * scale.x * t.pos.x + scale.x - 1, 2 * scale.y * t.pos.y + scale.y - 1), scale));
			}
			System.out.println();
		}
		System.out.println("-------------------");

		renderer.render(toRender);

		super.render();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	public static void main(String[] args) {
		GameLauncher.init(GAME_NAME, 800, 800);
		GameLauncher.launch(new Mines(GAME_NAME));
	}
}
