package eu.tankernn.mines;

import java.awt.EventQueue;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
	public static final Pos[] DEFAULT_PATTERN = { new Pos(0, 1), new Pos(0, -1), new Pos(1, 0), new Pos(1, 1),
			new Pos(1, -1), new Pos(-1, 0), new Pos(-1, -1), new Pos(-1, 1) };
	public static final Settings DEFAULT_SETTINGS = new Settings(Arrays.asList(DEFAULT_PATTERN), 9, 9, 10);
	public static final boolean DEBUG = false;

	// Utility
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
	private Map<Pos, Tile> tiles;
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
			GUIText helpText = new GUIText("Press R to reset board", 1f, font, new Vector2f(0.8f, 0f), 0.15f,
					false);
			textMaster.loadText(timeText);
			textMaster.loadText(helpText);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		setSettings(DEFAULT_SETTINGS);
		
		EventQueue.invokeLater(() -> {
			editor = new SettingsEditor(DEFAULT_SETTINGS, this::setSettings);
		});
		
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

	private Map<Pos, Tile> generateBoard(int width, int height, int mines) {
		assert width * height < mines;

		List<Pos> minePositions = new ArrayList<Pos>(allPositions(width, height).collect(Collectors.toList()));
		Collections.shuffle(minePositions);
		minePositions.subList(0, minePositions.size() - mines).clear();

		return allPositions(width, height).map(p -> new Tile(minePositions.contains(p), p))
				.collect(Collectors.toMap(t -> t.pos, t -> t));
	}

	public void check(Tile tile) {
		if (running)
			if (tile.isMine)
				lose();
			else
				calculateMinesAround(tile, settings.pattern);
	}

	public void calculateMinesAround(Tile tile, List<Pos> pattern) {
		hiddenTiles--;
		
		List<Tile> testTiles = pattern.stream().map(p -> p.add(tile.pos))
				.map(tiles::get).filter(t -> t != null).collect(Collectors.toList());

		int minesAround = (int) testTiles.stream().filter(t -> t.isMine).count();

		tile.setMinesAround(minesAround);

		// Keep checking if there are no mines around
		if (minesAround == 0)
			testTiles.stream().filter(t -> t.getState().equals(Tile.TileState.HIDDEN))
					.forEach(t -> calculateMinesAround(t, pattern));

	}

	private void lose() {
		running = false;
		// Expose all mines
		for (Tile t : tiles.values())
			if (t.isMine)
				t.setState(TileState.EXPLODED);
	}

	private void win() {
		running = false;
	}

	@Override
	public void update() {
		if (Mouse.isButtonDown(1) || Mouse.isButtonDown(0)) {
			if (!justClicked && running) {
				justClicked = true;
				Pos p = new Pos(Mouse.getX() / tileWidth, Mouse.getY() / tileHeight);
				if (!tiles.containsKey(p)) {
					System.out.println(p);
					dumpMap();
					return;
				}
				if (Mouse.isButtonDown(0)) {
					check(tiles.get(p));
				} else {
					tiles.get(p).toggleFlag();
				}
			}
		} else
			justClicked = false;

		// Handle keyboard
		if (running)
			timeText.setText(format.format(((float) (Sys.getTime() - startTime)) / Sys.getTimerResolution()));
		else if (Keyboard.isKeyDown(Keyboard.KEY_R))
			startGame();

		if (hiddenTiles == settings.mines)
			win();

		super.update();
	}

	@Override
	public void render() {
		List<GuiTexture> toRender = new ArrayList<GuiTexture>();

		for (Tile t : tiles.values()) {
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
			default:
				tex = hidden;
				break;
			}
			Vector2f scale = new Vector2f(1f / settings.boardWidth, 1f / settings.boardHeight);
			toRender.add(new GuiTexture(tex,
					new Vector2f(2 * scale.x * t.pos.x + scale.x - 1, 2 * scale.y * t.pos.y + scale.y - 1), scale));

		}

		renderer.render(toRender);
		super.render();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		if (editor != null)
			editor.dispose();
	}

	public static Stream<Pos> allPositions(int width, int height) {
		Stream<Integer> xs = IntStream.range(0, width).boxed();
		return xs.flatMap(x -> IntStream.range(0, height).mapToObj(y -> new Pos(x, y)));
	}

	private void dumpMap() {
		// Text output
		System.out.println("Size: " + tiles.size());
		for (Pos p : tiles.keySet()) {
			Tile t = tiles.get(p);
			System.out.println("Pos: " + p + " Tile: " + t);
		}
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
