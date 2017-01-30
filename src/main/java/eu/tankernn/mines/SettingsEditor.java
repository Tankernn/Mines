package eu.tankernn.mines;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class SettingsEditor extends JFrame {

	private JPanel contentPane;
	private int radius = 3, size = radius * 2 + 1;
	private Consumer<Settings> settingsConsumer;
	private JSpinner minesSpinner = new JSpinner(),
			sizeSpinner = new JSpinner();
	JCheckBox[][] boxes;

	/**
	 * Create the frame.
	 */
	public SettingsEditor(Settings initialSettings, Consumer<Settings> consumer) {
		settingsConsumer = consumer;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel options = new JPanel();
		options.setLayout(new GridLayout(2, 2));

		options.add(new JLabel("Board size: "));
		options.add(sizeSpinner);
		options.add(new JLabel("Number of mines: "));
		options.add(minesSpinner);

		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(size, size));

		boxes = new JCheckBox[size][size];

		for (int x = 0; x < boxes.length; x++) {
			for (int y = 0; y < boxes[x].length; y++) {
				JCheckBox box = new JCheckBox();
				boxes[x][y] = box;
				grid.add(box);
			}
		}

		importSettings(initialSettings);

		boxes[radius][radius].setEnabled(false);

		JButton save = new JButton("Save");
		save.addActionListener(e -> {
			Pos delta = new Pos(-radius, -radius);
			List<Pos> pattern = Mines.allPositions(size, size).filter(p -> boxes[p.x][p.y].isSelected()).map(p -> p.add(delta)).collect(Collectors.toList());

			settingsConsumer.accept(new Settings(pattern, (int) sizeSpinner.getValue(), (int) sizeSpinner.getValue(), (int) minesSpinner.getValue()));

		});

		contentPane.add(options, BorderLayout.NORTH);
		contentPane.add(grid, BorderLayout.CENTER);
		contentPane.add(save, BorderLayout.SOUTH);

		pack();
		setTitle("Minesweeper pattern editor");
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void importSettings(Settings settings) {
		sizeSpinner.setValue((int) Math.sqrt(settings.area));
		minesSpinner.setValue(settings.mines);

		for (Pos pos : settings.pattern)
			boxes[pos.x + radius][pos.y + radius].setSelected(true);
	}
}
