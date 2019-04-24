import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Player extends Sprite implements Commons {
	private int START_X = 270;//270
	private int START_Y = 315;//280

	private final String path = "./img/ship.png";
	private int mWidth;
	private int mHeight;
	private int lives = 3;

	public Player() {
		ImageIcon ii = new ImageIcon(path);
		setImage(ii.getImage());
		mWidth = ii.getImage().getWidth(null);
		mHeight = ii.getImage().getHeight(null);
		setX(START_X);
		setY(START_Y);
	}

	public void act() {
		mX += mDx;
		if (mX <= 2) {
			mX = 2;
		}
		if (mX >= BOARD_WIDTH - 2*mWidth) {
			mX = BOARD_WIDTH - 2*mWidth;
		}

		mY += mDy;
		if (mY >= 340) {
			mY = 340;
		}
		//initially 275
		if (mY <= 275) {
			mY = 275;
		}
	}

	public int getLives(){
		return lives;
	}
	public void setLives(int remaining){
		lives = remaining;
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_LEFT) {
			mDx = -2;
		}
		else if (key == KeyEvent.VK_RIGHT) {
			mDx = 2;
		}
		else if (key == KeyEvent.VK_UP) {
			mDy = -2;
		}
		else if (key == KeyEvent.VK_DOWN) {
			mDy = 2;
		}

	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if ((key == KeyEvent.VK_LEFT) || (key == KeyEvent.VK_RIGHT)) {
			mDx = 0;
		}
		else if ((key == KeyEvent.VK_UP) || (key == KeyEvent.VK_DOWN)) {
			mDy = 0;
		}
	}
}
