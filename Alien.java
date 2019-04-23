import javax.swing.ImageIcon;

public class Alien extends Sprite {
	private final String path = "./img/mushroom1.png";
	private int lives = 3;

	public Alien(int x, int y) {
		mX = x;
		mY = y;
		ImageIcon ii = new ImageIcon(path);
		setImage(ii.getImage());
	}
	public int getLives(){
		return lives;
	}
	public void setLives(int remaining){
		lives = remaining;
	}
//
//	public void act(int direction) {
//		mX += direction;
//	}
}
