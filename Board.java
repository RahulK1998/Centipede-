import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*; //Audio file stuff
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Board extends JPanel implements Runnable, Commons {
	private Dimension d;
	private ArrayList<Alien> aliens;
	private ArrayList<Player> lives;
	private ArrayList<Centipede> segments;
	private ArrayList<Shot> shots;
	private Player player;
	private Spider spider;

	private Shot shot;

	private int alienX = BORDER_LEFT+SPRITE_WIDTH;
	private int alienY = 3*SPRITE_HEIGHT; //1st  2nd  and 3rd row empty
	private int centiX = BOARD_WIDTH; //10 is segment length
	private int centiY = 2*SPRITE_HEIGHT;
	private int direction = 1;
	private int centiDirection = -1;
	private int deaths = 0;
	private int centNew = 0;
	private int currentLife = 3; //Current limit for life
	private int score = 0;
	private int noAliens = 0;

	private Point mouseLocation;
	private Point centerLocation;
	//	private Component comp;
	private Robot robot;
	private boolean isRecentering;

	private boolean ingame = true;
	private final String explImg = "./img/explosion.png";
	private final String alienImg = "./img/mushroom1.png";
	private final String m3 = "./img/mushroom2.png";
	private final String m2 = "./img/mushroom3.png";
	private final String m1 = "./img/mushroom4.png";
	private final String centR = "./img/centipede1rev.png";
	private final String centL = "./img/centipede1.png";
	private final String centImg = "./img/centipede2.png";
	private final String cent2Img = "./img/centipede3.png";
	private final String sp2 = "./img/spider2.png";

	private String message = "Game Over";

	private Thread animator;

	public Board() {


		//invisible cursor
		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				cursorImg, new Point(0, 0), "blank cursor");

		// Set the blank cursor to the JFrame.
		this.setCursor(blankCursor);


		mouseLocation = new Point();
		centerLocation = new Point();

		addKeyListener(new TAdapter());
		MouseListeners listeners = new MouseListeners();
		addMouseListener(listeners);
		addMouseMotionListener(listeners);


		setFocusable(true);
		d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
		setBackground(Color.BLACK);

		gameInit();
		setDoubleBuffered(true);
	}



	//don't know if this is needed ??
	public void addNotify() {
		super.addNotify();
		//gameInit();
	}

	public void centInit() {
		centNew = 0;
		segments = new ArrayList<Centipede>(); //flushes all existing object
		ImageIcon ii = new ImageIcon(centImg);
		for (int j = 0; j < CENT_LENGTH; j++) {
			Centipede centipede = new Centipede(centiX - SPRITE_WIDTH*j, centiY );
			centipede.setImage(ii.getImage());
			segments.add(centipede);
		}
	}

	public void spiderInit(){
		spider = new Spider(BORDER_LEFT,BOARD_WIDTH-BORDER_RIGHT-5*SPRITE_HEIGHT);
		Random generator1 = new Random();
		int xNext = generator1.nextInt(BOARD_WIDTH - 2* BORDER_RIGHT) + 2* BORDER_LEFT;
		int yNext = generator1.nextInt(BOARD_HEIGHT - 2* BORDER_RIGHT) + 2* BORDER_LEFT;
		spider.setXDirection(xNext);
		spider.setYDirection(yNext);

	}


	public void lifeInit(){
		lives = new ArrayList<Player>();
		for (int k = 0; k < currentLife; k++){
			Player life = new Player();
			life.setX(BORDER_LEFT+ k*SPRITE_WIDTH);
			life.setY(0);
			lives.add(life);
		}
	}

	public void mushroomRestore(){
		Iterator it = aliens.iterator();
		ImageIcon ii = new ImageIcon(alienImg);
		while(it.hasNext()){
			Alien alien = (Alien) it.next();
			if(alien.isVisible() && alien.getLives() < 3 ){
				alien.setLives(3);
				alien.setImage(ii.getImage());
				score+=10;
			}
		}
	}

	public void gameInit() {

		centInit();

		spiderInit();


		aliens = new ArrayList<Alien>();
		ImageIcon iii = new ImageIcon(alienImg);
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 18; j++) {
				Alien alien = new Alien(alienX + 18*j, alienY + 18*i);
				alien.setImage(iii.getImage());
				alien.setVisible(false);
				aliens.add(alien);
			}
		}

		//mushroom selection code

		Random generator = new Random();
		int removeCount = 0;
		int indx = 0;
		ArrayList<Integer> list;

		for (int i = 0; i < 13; i++) {
			list = new ArrayList<Integer>();
			removeCount = generator.nextInt(9); //min value for random

			for (int j = 0; j < removeCount; j++){
				indx = generator.nextInt(18);
				while(list.contains(indx)){
					indx = generator.nextInt(18);
				}
				list.add(indx);
				Alien a = (Alien) aliens.get(indx+i*18);
				a.setVisible(true);
				noAliens++;
			}
		}


		int rowIndx = 0;
		int colIndx = 0;
		//last row does'nt matter 14// inside col check
		//TODO - some improvements in mushroom placement
		while ( rowIndx < 12){
			while (colIndx < 18 && colIndx > -1){
				if(rowIndx >= 12){
					break;
				}
				if (((colIndx == 0 && direction == 1)  || (colIndx == 17 && direction == -1)) && aliens.get(colIndx + 18*rowIndx ).isVisible() ){
					rowIndx++;
				}
				else if (aliens.get(colIndx + 18*rowIndx ).isVisible() ){
					if(aliens.get((colIndx-(direction)) + 18*(rowIndx+1)).isVisible()){
						noAliens--;
					}
					aliens.get((colIndx-(direction)) + 18*(rowIndx+1)).setVisible(false);

					rowIndx++;
				}
				else{
					colIndx+= direction;
				}
			}
			direction *= -1;
			if(direction==1){
				colIndx = 0;
			}
			else{
				colIndx = 17;
			}
			rowIndx++;
		}


		player = new Player();

		lifeInit();


		shots = new ArrayList<Shot>(); //empties old list in new game

		if ((animator == null) || (!ingame)) {
			animator = new Thread(this);
			animator.start();
		}
	}

	public void drawAliens(Graphics g) {
		Iterator it = aliens.iterator();
		while (it.hasNext()) {
			Alien alien = (Alien)it.next();
			if (alien.isVisible()) {
				if(( BORDER_LEFT <= alien.getX() )&&  (alien.getX()  <= (BOARD_WIDTH - BORDER_RIGHT - SPRITE_WIDTH ))) {
					g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
				}
				else{
					alien.setVisible(false);
				}
			}
			if (alien.isDying()) {
				alien.die();
			}
		}
	}

	public void drawCentipede(Graphics g) {
//		Iterator it = segments.iterator();
		ImageIcon ir = new ImageIcon(centR);
		ImageIcon il = new ImageIcon(centL);
		for(int i = 0; i < CENT_LENGTH; i ++)
		{
			Centipede centipede = (Centipede) segments.get(i);
			if (centipede.isVisible()) {
				if((i == (CENT_LENGTH-1)) || !(segments.get(i+1).isVisible())){
					if(centipede.getDirection() == 1){
						centipede.setImage(ir.getImage());
					}else if(centipede.getDirection() == -1){
						centipede.setImage(il.getImage());
					}
				}
				g.drawImage(centipede.getImage(), centipede.getX(), centipede.getY(), this);
			}

			if (centipede.isDying()) {
				centipede.die();
			}
		}
//		while (it.hasNext()) {
//			Centipede centipede = (Centipede) it.next();
//			if (centipede.isVisible()) {
//
//				g.drawImage(centipede.getImage(), centipede.getX(), centipede.getY(), this);
//			}
//
//			if (centipede.isDying()) {
//				 centipede.die();
//			}
//		}
	}


	public void drawSpider(Graphics g) {
		if (spider.isVisible()) {
			g.drawImage(spider.getImage(), spider.getX(), spider.getY(), this);
		}

		if (spider.isDying()) {
			spider.die();
		}
	}


	public void drawScore(Graphics g) {
		Font small = new Font("Helvetica", Font.BOLD, 14);
		FontMetrics metr = this.getFontMetrics(small);

		g.setColor(Color.GREEN);
		g.setFont(small);
		g.drawString(score + "", SPRITE_WIDTH*4, 12);
	}

	public void drawPlayer(Graphics g) {

		if (player.isVisible()) {
			g.drawImage(player.getImage(), player.getX(), player.getY(), this);
		}

		if(player.getLives() < currentLife){
			try {
				String soundName = "./sounds/Collision.wav";
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName));
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			} catch (UnsupportedAudioFileException ex1) {
//				ex1.printStackTrace();
			} catch (IOException ex2) {
//				ex2.printStackTrace();
			} catch (LineUnavailableException ex3) {
//				ex3.printStackTrace();
			}
		}


		Iterator itLife = lives.iterator();
		while(itLife.hasNext()){
			Player life = (Player) itLife.next();
			g.drawImage(life.getImage(), life.getX(), life.getY(), this);
		}

		if (player.isDying()) {
			player.die();
			ingame = false;
		}
	}

	public void drawShot(Graphics g) {
		// create an iterator
		Iterator its = shots.iterator();
		while (its.hasNext()) {
			Shot shot = (Shot) its.next();
			if (shot.isVisible()) {
				g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);


				if ((Math.abs(shot.getX() - player.getX()) <= 10) && (Math.abs(shot.getY() - player.getY()) <= 10)) {
					try {
						String soundName = "./sounds/playerShot.wav";
						AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName));
						Clip clip = AudioSystem.getClip();
						clip.open(audioInputStream);
						clip.start();
					} catch (UnsupportedAudioFileException ex1) {
//						ex1.printStackTrace();
					} catch (IOException ex2) {
//						ex2.printStackTrace();
					} catch (LineUnavailableException ex3) {
//						ex3.printStackTrace();
					}
				}
			}

		}
	}



	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.BLACK);
		g.fillRect(0,0,d.width, d.height);
		g.setColor(Color.GREEN);

		if (ingame) {
			//g.drawLine(0, GROUND, BOARD_WIDTH, GROUND);
			drawAliens(g);
			drawPlayer(g);
			drawShot(g);
			drawCentipede(g);
			drawSpider(g);
			drawScore(g);
		}

		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}

	public void gameOver() {
		Graphics g = this.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0,0,BOARD_WIDTH, BOARD_HEIGHT);
		
		g.setColor(new Color(0, 32, 48));
		g.fillRect(50, BOARD_WIDTH/2 - 30, BOARD_WIDTH-100, 50);

		Font small = new Font("Helvetica", Font.BOLD, 14);
		FontMetrics metr = this.getFontMetrics(small);

		g.setColor(Color.WHITE);
		g.setFont(small);
		g.drawString(message, (BOARD_WIDTH - metr.stringWidth(message))/2, BOARD_WIDTH/2);
	}

	public void animationCycle() {
		//does not matter even if all mushrooms are gone
//		if (deaths == noAliens) {
//			ingame = false;
//			message = "You win! Your HighScore: " + score ;
//		}

		if (centNew == CENT_LENGTH) {
			score+= 600;
			centInit();
			drawCentipede(this.getGraphics());
		}

		//if spider dies -> make new spider
		if(!spider.isVisible()){
			spiderInit();
		}

		//Seing if the player lost a life
		if(player.getLives() < currentLife){
			currentLife = player.getLives();
			player = new Player();

			//Player starts where they left off
			if (mouseLocation.x <= 2) {
				player.setX(2);
			} else if (mouseLocation.x >= BOARD_WIDTH - 2 * PLAYER_WIDTH) {
				player.setX(BOARD_WIDTH - 2 * PLAYER_WIDTH);
			} else {
				player.setX(mouseLocation.x);
			}

			if (mouseLocation.y >= 340) {
				player.setY(340);
			} else if (mouseLocation.y <= 275) {
				player.setY(275);
			} else {
				player.setY(mouseLocation.y);
			}

			centInit();
			spiderInit();
			lifeInit();
			mushroomRestore();
			//gameInit(); - for new game

			player.setLives(currentLife);
			if(currentLife == 0){
				ingame = false;
				message = "Game Over! Your HighScore: " + score;
			}
		}

		//player.act();

		//create an iterator
		Iterator itShots = shots.iterator();
		while (itShots.hasNext()) {
			Shot shot = (Shot) itShots.next();
			if (shot.isVisible()) {
				Iterator it = aliens.iterator();
				int shotX = shot.getX();
				int shotY = shot.getY();

				while (it.hasNext()) {
					Alien alien = (Alien) it.next();
					int alienX = alien.getX();
					int alienY = alien.getY();
					ImageIcon ii;
					if (alien.isVisible() && (shot.isVisible())) {
						if ((shotX >= alienX) && (shotX <= alienX + ALIEN_WIDTH) && (shotY >= alienY) && (shotY <= alienY + ALIEN_HEIGHT)) {
							switch (alien.getLives()) {
								case 3:
									alien.setLives(2);
									ii = new ImageIcon(m2);
									alien.setImage(ii.getImage());
									shot.die();
									score++;
									break;
								case 2:
									alien.setLives(1);
									ii = new ImageIcon(m1);
									alien.setImage(ii.getImage());
									shot.die();
									score++;
									break;
								case 1:
									alien.setLives(0);
									ii = new ImageIcon(explImg);
									alien.setImage(ii.getImage());
									alien.setDying(true);
									deaths++;
									shot.die();
									score += 5;
									break;
							}
						}
					}
				}

				Iterator it2 = segments.iterator();
				while (it2.hasNext()) {
					Centipede centipede = (Centipede) it2.next();
					int centiX = centipede.getX();
					int centiY = centipede.getY();
					ImageIcon ii;
					if (centipede.isVisible() && (shot.isVisible())) {
						if ((shotX >= centiX) && (shotX <= centiX + ALIEN_WIDTH) && (shotY >= centiY) && (shotY <= centiY + ALIEN_HEIGHT)) {
							switch (centipede.getLives()) {
								case 2:
									centipede.setLives(1);
									ii = new ImageIcon(cent2Img);
									centipede.setImage(ii.getImage());
									score += 2;
									shot.die();
									break;
								case 1:
									centipede.setLives(0);
									ii = new ImageIcon(explImg);
									centipede.setImage(ii.getImage());
									centipede.setDying(true);
									centNew++;
									score += 5;
									shot.die();
									break;
							}
						}
					}
				}


				int spX = spider.getX();
				int spY = spider.getY();
				ImageIcon im;
				if (spider.isVisible() && (shot.isVisible())) {
					if ((shotX >= spX) && (shotX <= spX + ALIEN_WIDTH) && (shotY >= spY) && (shotY <= spY + ALIEN_HEIGHT)) {
						switch (spider.getLives()) {
							case 2:
								spider.setLives(1);
								im = new ImageIcon(sp2);
								spider.setImage(im.getImage());
								score += 100;
								shot.die();
								break;
							case 1:
								spider.setLives(0);
								im = new ImageIcon(explImg);
								spider.setImage(im.getImage());
								spider.setDying(true);
								score += 600;
								shot.die();
								break;
						}
					}
				}

				int y = shot.getY();
				y -= 4;
				if (y <= 0) {
					shot.die();
				} else {
					shot.setY(y);
				}
			}
		}






		//Combine iterators

		Iterator itl = segments.iterator();
		while (itl.hasNext()) {
			Centipede ct = (Centipede) itl.next();
			int x = ct.getX();
			int y = ct.getY();
			if ((x >= BOARD_WIDTH - BORDER_RIGHT) && (ct.getDirection() != -1)) {
				ct.setDirection(-1);
				if(!(y > BOARD_HEIGHT - BORDER_RIGHT - (3* SPRITE_HEIGHT))){
					ct.setY(ct.getY() + GO_DOWN);
				}

			}
			else if ((x <= BORDER_LEFT) && (ct.getDirection() != 1)) {
				ct.setDirection(1);
				if(!(y > BOARD_HEIGHT - BORDER_RIGHT - (3* SPRITE_HEIGHT))){
					ct.setY(ct.getY() + GO_DOWN);
				}
			}

			if (ct.isVisible()) {
				ct.act();
			}
		}

		//spider.act -> movement
		Random generator2 = new Random();
		//Random coord
		int xNext = generator2.nextInt(BOARD_WIDTH - 2* BORDER_RIGHT) + 2* BORDER_LEFT;
		int yNext = generator2.nextInt(BOARD_HEIGHT - 2* BORDER_RIGHT) + 2* BORDER_LEFT;

		if (spider.isVisible()) {
			if (spider.reached()){
				spider.setXDirection(generator2.nextInt(BOARD_WIDTH - 2* BORDER_RIGHT) + 2* BORDER_LEFT);
				spider.setYDirection(generator2.nextInt(BOARD_HEIGHT - 2* BORDER_RIGHT) + 2* BORDER_LEFT);
			}
			spider.act();
		}






		//Mushroom Collision Avoidance
		Iterator segit = segments.iterator();
		while(segit.hasNext()) {
			Centipede segOne = (Centipede) segit.next();
			if(segOne.isVisible()) {
				Iterator alit = aliens.iterator();
				while (alit.hasNext()){
					Alien shroom = (Alien) alit.next();
					if (shroom.isVisible()) {
						if ((segOne.getX() <= shroom.getX() + ALIEN_WIDTH) && (segOne.getX() + ALIEN_WIDTH >= shroom.getX())) {
							if ((segOne.getY() + ALIEN_HEIGHT >= shroom.getY()) && (segOne.getY() <= shroom.getY() + ALIEN_HEIGHT)) {
								segOne.setY(segOne.getY() + GO_DOWN);
								break;
							}
						}
					}
				}
			}
		}

		//Player collison with centipede
		Iterator segit2 = segments.iterator();
		while(segit2.hasNext()){
			Centipede segTwo = (Centipede) segit2.next();
			if(segTwo.isVisible()) {
				if(segTwo.getY() + SPRITE_HEIGHT >=275){
					if(segTwo.getX() <= (player.getX()+PLAYER_WIDTH)
							&& segTwo.getX()+ALIEN_WIDTH >= (player.getX())
							&& segTwo.getY() + ALIEN_HEIGHT >= (player.getY())
							&& segTwo.getY() <= (player.getY() + PLAYER_HEIGHT)){
						ImageIcon iv = new ImageIcon(explImg);
						player.setImage(iv.getImage());
						player.die();
						player.setLives(player.getLives()-1);
						break;
					}
				}
			}
		}

		//Player collision with Spider;
		if(spider.isVisible()) {
			if(spider.getY() + SPRITE_HEIGHT >=275){
				if(spider.getX() <= (player.getX()+PLAYER_WIDTH)
						&& spider.getX()+ALIEN_WIDTH >= (player.getX())
						&& spider.getY() + ALIEN_HEIGHT >= (player.getY())
						&& spider.getY() <= (player.getY() + PLAYER_HEIGHT)){
					ImageIcon iv = new ImageIcon(explImg);
					player.setImage(iv.getImage());
					player.die();
					player.setLives(player.getLives()-1);
				}
			}
		}

	}

	public void run() {
		long beforeTime, timeDiff, sleep;
		beforeTime = System.currentTimeMillis();
		while (ingame) {
			//catching concurrent modification and other thread errors
			try {
				repaint();
				animationCycle();
			}
			catch(Exception e){
				//System.out.println("Error was caught "+ e);
				continue;
			}

			timeDiff = System.currentTimeMillis() - beforeTime;
			sleep = DELAY - timeDiff;
			if (sleep < 0) {
				sleep = 2;
			}
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.out.println("interrupted");
			}

			beforeTime = System.currentTimeMillis();
		}
		gameOver();
	}

	private class TAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			player.keyReleased(e);

		}

		public void keyPressed(KeyEvent e) {
//			player.keyPressed(e);
//			int x = player.getX();
//			int y = player.getY();
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_Q) {
				ingame = false;
				message = "Thank You for Playing! Your Score: "+ score;
			}
			if (key == KeyEvent.VK_N) {
				deaths = 0;
				centNew = 0;
				currentLife = 3; //Current limit for life
				score = 0;
				gameInit();
				if(ingame == false){
					ingame = true;
				}
			}
		}
	}



	public void setRelativeMouseMode(boolean mode) {
		if (mode == isRelativeMouseMode()) {
			return;
		}

		if (mode) {
//			robot = null;
			try {
				robot = new Robot();

				recenterMouse();
			}
			catch (AWTException ex) {
				// couldn't create robot!
				robot = null;
			}
		}
		else {
			robot = null;
		}
	}


	/**
	 Returns whether or not relative mouse mode is on.
	 */
	public boolean isRelativeMouseMode() {
		return (robot != null);
	}


	private synchronized void recenterMouse() {
		//Window window = screen.getFullScreenWindow();
		if (robot != null && this.isShowing()) {
			centerLocation.x = BOARD_WIDTH / 2 ;
			centerLocation.y = BOARD_HEIGHT / 2;
			SwingUtilities.convertPointToScreen(centerLocation,
					this);
			isRecentering = true;
			robot.mouseMove(centerLocation.x, centerLocation.y);
		}
	}



	private class MouseListeners extends MouseAdapter{
		@Override
		public void mousePressed(MouseEvent m){
			int x = player.getX();
			int y = player.getY();
			if (ingame) {
				if (m.getButton() == MouseEvent.BUTTON1) {
					Shot sh = new Shot(x,y);
					shots.add(sh);
				}
			}
		}


		// from the MouseListener interface
		public void mouseEntered(MouseEvent m) {
			mouseMoved(m);
		}


		// from the MouseListener interface
		public void mouseExited(MouseEvent m) {
			//player.mouseM(0,0);
			mouseMoved(m);
		}


		// from the MouseMotionListener interface
		public void mouseDragged(MouseEvent m) {
			mouseMoved(m);
		}


		@Override
		public synchronized void mouseMoved(MouseEvent m) {
			if (isRecentering &&
					centerLocation.x == m.getX() &&
					centerLocation.y == m.getY())
			{
				isRecentering = false;
			}
			else {
				int dx = m.getX() - mouseLocation.x;
				int dy = m.getY() - mouseLocation.y;
				if(ingame) {
					if (m.getX() <= 2) {
						player.setX(2);
					} else if (m.getX() >= BOARD_WIDTH - 2 * PLAYER_WIDTH) {
						player.setX(BOARD_WIDTH - 2 * PLAYER_WIDTH);
					} else {
						player.setX(m.getX());
					}

					if (m.getY() >= 340) {
						player.setY(340);
					} else if (m.getY() <= 275) {
						player.setY(275);
					} else {
						player.setY(m.getY());
					}
				}
				if (isRelativeMouseMode()) {
					recenterMouse();
				}
			}
			mouseLocation.x = m.getX();
			mouseLocation.y = m.getY();
		}
	}
}
