import javax.swing.ImageIcon;

public class Centipede extends Sprite {
    private final String path = "./img/Centipede2.png";//centipede
    private int mDirection = 1;

    public Centipede(int x, int y) {
        mX = x;
        mY = y;
        ImageIcon ii = new ImageIcon(path);
        setImage(ii.getImage());
    }

    public int getDirection(){
        return mDirection;
    }

    public void setDirection(int direction){
        mDirection = direction;
    }

    public void act() {
        mX += mDirection;
    }
}
