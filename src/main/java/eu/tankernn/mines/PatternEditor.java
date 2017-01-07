package eu.tankernn.mines;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class PatternEditor extends JFrame {

	private JPanel contentPane;
	private int radius = 3, size = radius * 2 + 1;
	private Mines gameInstance;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PatternEditor frame = new PatternEditor(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PatternEditor(Mines instance) {
		gameInstance = instance;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(size, size));

		JCheckBox[][] boxes = new JCheckBox[size][size];

		for (int x = 0; x < boxes.length; x++) {
			for (int y = 0; y < boxes[x].length; y++) {
				JCheckBox box = new JCheckBox();
				boxes[x][y] = box;
				grid.add(box);
			}
		}

		boxes[radius][radius].setEnabled(false);

		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				List<Pos> pattern = new ArrayList<Pos>();
				for (int x = 0; x < boxes.length; x++) {
					for (int y = 0; y < boxes[x].length; y++) {
						if (boxes[x][y].isSelected())
							pattern.add(new Pos(x - radius, y - radius));
					}
				}
				if (gameInstance != null) {
					gameInstance.startGame(pattern.toArray(new Pos[pattern.size()]));
					dispose();
				}
			}
		});

		contentPane.add(grid, BorderLayout.CENTER);
		contentPane.add(save, BorderLayout.SOUTH);

		pack();
		setTitle("Minesweeper pattern editor");
		setVisible(true);
	}
}
