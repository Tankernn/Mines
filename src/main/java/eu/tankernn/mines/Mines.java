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
	public static final Pos[] DEFAULT_PATTERN = { new Pos(0, 1), new Pos(0, -1), new Pos(1, 0), new Pos(1, 1), new Pos(1, -1), new Pos(-1, 0), new Pos(-1, -1), new Pos(-1, 1) };
	public static final Settings DEFAULT_SETTINGS = new Settings(DEFAULT_PATTERN, 9, 9, 20);
	public static final boolean DEBUG = false;

	// Utility
	private Random rand = new Random();
	private DecimalFormat format = new DecimalFormat("0.000 sec");
	private GuiRenderer renderer;
	private SettingsEditor editor;

	// Graphics
	private Texture hidden, exploded, flagged;
	private Texture[] checked;
	private GUIText timeText;
	private int tileWidth, tileHeight;

	/**
	 * Next game settings.
	 */
	private Settings nextSettings;
	/**
	 * Current game settings.
	 */
	private Settings settings;

	// Game state
	private long startTime;
	private Tile[][] tiles;
	private int hiddenTiles;
	private boolean running = false;
	private boolean justClicked = false;

	public Mines(String name) {
		super(name);
		renderer = new GuiRenderer(loader);

		try {
			hidden = loader.loadTexture("hidden.png");
			checked = new Texture[10];
			for (int i = 0; i < checked.length; i++)
				checked[i] = loader.loadTexture(i + ".png");
			exploded = loader.loadTexture("exploded.png");
			flagged = loader.loadTexture("flagged.png");
			FontType font = new FontType(loader.loadTexture("arial.png"), new InternalFile("arial.fnt"));
			timeText = new GUIText(format.format(0), 1f, font, new Vector2f(0f, 0f), 100, false);
			GUIText helpText = new GUIText("R - reset, E - edit settings", 1f, font, new Vector2f(0.8f, 0f), 0.15f, false);
			textMaster.loadText(timeText);
			textMaster.loadText(helpText);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		setSettings(DEFAULT_SETTINGS);
		startGame();
	}

	public void setSettings(Settings settings) {
		this.nextSettings = settings;
	}

	/**
	 * Starts game with next settings.
	 */
	private void startGame() {
		if (!nextSettings.equals(settings)) {
			this.settings = nextSettings;
			updateBoardSize();
		}
		startTime = Sys.getTime();
		hiddenTiles = settings.area;
		tiles = generateBoard(settings.boardWidth, settings.boardHeight, settings.mines);
		running = true;
	}

	private Tile[][] generateBoard(int width, int height, int mines) {
		if (width * height < mines) {
			throw new IllegalArgumentException("The mines will not fit on the board.");
		}

		List<Pos> minePositions = new ArrayList<Pos>();
		for (int i = 0; i < mines; i++) {
			Pos p;
			do {
				p = new Pos(rand.nextInt(width), rand.nextInt(height));
			} while (minePositions.contains(p));
			minePositions.add(p);
		}

		tiles = new Tile[width][height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				tiles[x][y] = new Tile(minePositions.contains(new Pos(x, y)), new Pos(x, y));
			}
		}
		return tiles;
	}

	public void check(Tile tile) {
		if (running)
			if (tile.isMine)
				lose();
			else
				calculateMinesAround(tile, settings.pattern);
	}

	public void calculateMinesAround(Tile tile, Pos[] pattern) {
		hiddenTiles--;

		int minesAround = 0;
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
				minesAround++;

		tile.setMinesAround(minesAround);

		// Keep checking if there are no mines around
		if (minesAround == 0)
			for (Tile testTile : testTiles)
				if (testTile.getState().equals(Tile.TileState.HIDDEN))
					calculateMinesAround(testTile, pattern);
	}

	private void lose() {
		running = false;
		// Expose all mines
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

		// Handle keyboard
		if (running)
			timeText.setText(format.format(((float) (Sys.getTime() - startTime)) / Sys.getTimerResolution()));
		else if (Keyboard.isKeyDown(Keyboard.KEY_R))
			startGame();

		if (Keyboard.isKeyDown(Keyboard.KEY_E) && (editor == null || !editor.isShowing()))
			editor = new SettingsEditor(this);

		if (hiddenTiles == settings.mines)
			win();

		super.update();
	}

	@Override
	public void render() {
		List<GuiTexture> toRender = new ArrayList<GuiTexture>();

		for (int y = 0; y < settings.boardHeight; y++) {
			for (int x = 0; x < settings.boardWidth; x++) {
				Tile t = tiles[x][y];
				// Text output
				if (DEBUG)
					System.out.print(t.getState().equals(TileState.CHECKED) ? Integer.toString(t.getMinesAround()) : t.getState().appearance);
				// OpenGL output
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
				Vector2f scale = new Vector2f(1f / settings.boardWidth, 1f / settings.boardHeight);
				toRender.add(new GuiTexture(tex, new Vector2f(2 * scale.x * t.pos.x + scale.x - 1, 2 * scale.y * t.pos.y + scale.y - 1), scale));
			}
			if (DEBUG)
				System.out.println();
		}
		if (DEBUG)
			System.out.println("-------------------");

		renderer.render(toRender);

		super.render();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		if (editor != null)
			editor.dispose();
	}

	private void updateBoardSize() {
		tileWidth = Display.getWidth() / settings.boardWidth;
		tileHeight = Display.getHeight() / settings.boardHeight;
	}

	public static void main(String[] args) {
		GameLauncher.init(GAME_NAME, 800, 800);
		GameLauncher.launch(new Mines(GAME_NAME));
	}

	public Settings getSettings() {
		return settings;
	}
}
