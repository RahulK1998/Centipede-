import javax.swing.ImageIcon;

public class Alien extends Sprite {
	private final String path = "./img/alien.png";
	private int lives = 4;

	public Alien(int x, int y) {
		mX = x;
		mY = y;
		ImageIcon ii = new ImageIcon(path);
		setImage(ii.getImage());
	}
//
//	public void act(int direction) {
//		mX += direction;
//	}
}
