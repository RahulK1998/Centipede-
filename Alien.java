import javax.swing.ImageIcon;

public class Alien extends Sprite {
	private Bomb mBomb;
	private final String path = "./img/alien.png";

	public Alien(int x, int y) {
		mX = x;
		mY = y;
		mBomb = new Bomb(x,y);
		ImageIcon ii = new ImageIcon(path);
		setImage(ii.getImage());
	}

	public void act(int direction) {
		mX += direction;
	}

	public Bomb getBomb() {
		return mBomb;
	}

	public class Bomb extends Sprite {
		private final String path = "./img/bomb.png";
		private boolean mDestroyed;

		public Bomb(int x, int y) {
			setDestroyed(true);
			mX = x;
			mY = y;
			ImageIcon ii = new ImageIcon(path);
			mImage = ii.getImage();
		}

		public void setDestroyed(boolean destroyed) {
			mDestroyed = destroyed;
		}

		public boolean isDestroyed() {
			return mDestroyed;
		}
	}
}
