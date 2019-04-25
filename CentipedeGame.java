import javax.swing.JFrame;

public class CentipedeGame extends JFrame implements Commons {
	public CentipedeGame() {
		add(new Board());
		setTitle("Centipede");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(BOARD_WIDTH, BOARD_HEIGHT);
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);// false
	}

	public static void main(String[] args) {
		new CentipedeGame();
	}
}
