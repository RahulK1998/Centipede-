import java.awt.Image;

public class Sprite {
	protected boolean mVisible;
	protected int mX, mY;
	protected Image mImage;
	protected boolean mDying;
	protected int mDx;
	protected int mDy;

	public Sprite() {
		mVisible = true;
	}

	public void die() {
		mVisible = false;
	}

	public boolean isVisible() {
		return mVisible;
	}

	protected void setVisible(boolean visible) {
		mVisible = visible;
	}

	public void setImage(Image image) {
		mImage = image;
	}

	public Image getImage() {
		return mImage;
	}

	public void setX(int x) {
		mX = x;
	}

	public void setY(int y) {
		mY = y;
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	public void setDying(boolean dying) {
		mDying = dying;
	}

	public boolean isDying() {
		return mDying;
	}
}
