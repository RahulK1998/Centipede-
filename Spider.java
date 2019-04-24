import javax.swing.ImageIcon;

public class Spider extends Sprite {
    private final String path = "./img/Spider1.png";//centipede
    private int xDirection = 0;
    private int yDirection = 0;

    private int lives = 2;

    public Spider(int x, int y) {
        mX = x;
        mY = y;
        ImageIcon ii = new ImageIcon(path);
        setImage(ii.getImage());
    }

    public int getXDirection(){
        return xDirection;
    }

    public int getYDirection(){
        return yDirection;
    }

    public void setXDirection(int direction){
        xDirection = direction;
    }


    public void setYDirection(int direction){
        yDirection = direction;
    }

    public void act() {
        if(mX < xDirection){
            mX++;
        }else if (mX > xDirection){
            mX--;
        }
        if(mY < yDirection){
            mY++;
        }else if (mY > yDirection){
            mY--;
        }

    }

    public int getLives(){
        return lives;
    }
    public void setLives(int remaining){
        lives = remaining;
    }

    public boolean reached(){
        if(mX +mY - xDirection -yDirection == 0){
            return true;
        }
        return false;
    }
}
