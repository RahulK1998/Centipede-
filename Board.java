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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Board extends JPanel implements Runnable, Commons {
	private Dimension d;
	private ArrayList<Alien> aliens;
	private ArrayList<Centipede> segments;
	private Player player;
	private Shot shot;

	private int alienX = 18;
	private int alienY = 36;
	private int centiX = 2;
	private int centiY = 0;
	private int direction = 1;
	private int centiDirection = 1;
	private int deaths = 0;

	private boolean ingame = true;
	private final String explImg = "./img/explosion.png";
	private final String alienImg = "./img/mushroom1.png";
	private final String m3 = "./img/mushroom2.png";
	private final String m2 = "./img/mushroom3.png";
	private final String m1 = "./img/mushroom4.png";
	private final String centImg = "./img/centipede2.png";

	private String message = "Game Over";

	private Thread animator;

	public Board() {
		addKeyListener(new TAdapter());
		addMouseListener(new MAdapter());

		setFocusable(true);
		d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
		setBackground(Color.BLACK);

		gameInit();
		setDoubleBuffered(true);
	}

	public void addNotify() {
		super.addNotify();
		gameInit();
	}

	public void gameInit() {

		segments = new ArrayList<Centipede>();
		ImageIcon ii = new ImageIcon(centImg);

			for (int j = 0; j < 10; j++) {
				Centipede centipede = new Centipede(centiX + 18*j, centiY );
				centipede.setImage(ii.getImage());
				segments.add(centipede);
			}



		aliens = new ArrayList<Alien>();
		ImageIcon iii = new ImageIcon(alienImg);

		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 18; j++) {
				Alien alien = new Alien(alienX + 18*j, alienY + 18*i);
				alien.setImage(iii.getImage());
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
			removeCount = generator.nextInt(10) + 8; //min value for random

			for (int j = 0; j < removeCount; j++){
				indx = generator.nextInt(18);
				while(list.contains(indx)){
					indx = generator.nextInt(18);
				}
				list.add(indx);
				Alien a = (Alien) aliens.get(indx+i*18);
				a.die();
			}
		}


		int rowIndx = 0;
		int colIndx = 0;
		//last row does'nt matter 13// inside col check
		while ( rowIndx < 12){
			while (colIndx < 18 && colIndx > -1){
				if(rowIndx >= 12){
					break;
				}
				if (((colIndx == 0 && direction == 1)  || (colIndx == 17 && direction == -1)) && aliens.get(colIndx + 18*rowIndx ).isVisible() ){
					rowIndx++;
				}
				else if (aliens.get(colIndx + 18*rowIndx ).isVisible() ){
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
		shot = new Shot();
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
				g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
			}

			if (alien.isDying()) {
				alien.die();
			}
		}
	}

	public void drawCentipede(Graphics g) {
		Iterator it = segments.iterator();
		while (it.hasNext()) {
			Centipede centipede = (Centipede) it.next();
			if (centipede.isVisible()) {
				g.drawImage(centipede.getImage(), centipede.getX(), centipede.getY(), this);
			}

			if (centipede.isDying()) {
				 centipede.die();
			}
		}
	}

	public void drawPlayer(Graphics g) {
		if (player.isVisible()) {
			g.drawImage(player.getImage(), player.getX(), player.getY(), this);
		}

		if (player.isDying()) {
			player.die();
			ingame = false;
		}
	}

	public void drawShot(Graphics g) {
		if (shot.isVisible()) {
			g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
		}
	}

//	public void drawBombing(Graphics g) {
//		Iterator i3 = aliens.iterator();
//		while (i3.hasNext()) {
//			Alien a = (Alien)i3.next();
//			Alien.Bomb b = a.getBomb();
//			if (!b.isDestroyed()) {
//				g.drawImage(b.getImage(), b.getX(), b.getY(), this);
//			}
//		}
//	}

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
		if (deaths == NO_ALIENS) {
			ingame = false;
			message = "You win!";
		}

		player.act();

		if (shot.isVisible()) {
			Iterator it = aliens.iterator();
			Iterator it2 = segments.iterator();
			int shotX = shot.getX();
			int shotY = shot.getY();

			while (it.hasNext()) {
				Alien alien = (Alien)it.next();
				int alienX = alien.getX();
				int alienY = alien.getY();
				ImageIcon ii;
				if (alien.isVisible() && (shot.isVisible())) {
					if ((shotX >= alienX) && (shotX <= alienX + ALIEN_WIDTH) && (shotY >= alienY) && (shotY <= alienY+ALIEN_HEIGHT)) {
						switch(alien.getLives()){
							case 4: alien.setLives(3);
									ii = new ImageIcon(m3);
									alien.setImage(ii.getImage());
									shot.die();
									break;
							case 3: alien.setLives(2);
									ii = new ImageIcon(m2);
									alien.setImage(ii.getImage());
									shot.die();
									break;
							case 2: alien.setLives(1);
									ii = new ImageIcon(m1);
									alien.setImage(ii.getImage());
									shot.die();
									break;
							case 1: alien.setLives(0);
									ii = new ImageIcon(explImg);
									alien.setImage(ii.getImage());
									alien.setDying(true);
									deaths++;
									shot.die();
									break;
						}
					}
				}
			}

			while (it2.hasNext()) {
				Centipede centipede = (Centipede) it2.next();
				int centiX = centipede.getX();
				int centiY = centipede.getY();
				ImageIcon ii;
				if (centipede.isVisible() && (shot.isVisible())) {
					if ((shotX >= centiX) && (shotX <= centiX + ALIEN_WIDTH) && (shotY >= centiY) && (shotY <= centiY+ALIEN_HEIGHT)) {
						Alien alien = new Alien(centiX, centiY);
						ii = new ImageIcon(alienImg);
						alien.setImage(ii.getImage());
						aliens.add(alien);
						Graphics m = this.getGraphics();
						m.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);

						centipede.setDying(true);
						shot.die();
					}
				}
			}

			int y = shot.getY();
			y -= 4;
			if (y <= 0) {
				shot.die();
			}
			else {
				shot.setY(y);
			}
		}

		Iterator itl = segments.iterator();
		while (itl.hasNext()) {
			Centipede ct = (Centipede) itl.next();
			int x = ct.getX();
			if ((x >= BOARD_WIDTH - BORDER_RIGHT) && (ct.getDirection() != -1)) {
				ct.setDirection(-1);
				ct.setY(ct.getY() + GO_DOWN);
			}
			else if ((x <= BORDER_LEFT) && (ct.getDirection() != 1)) {
				ct.setDirection(1);
				ct.setY(ct.getY() + GO_DOWN);
			}

			if (ct.isVisible()) {
				int y = ct.getY();
				if (y > GROUND - ALIEN_HEIGHT) {
					ingame = false;
					message = "Invasion!";
				}

				ct.act();
			}
		}


//		Iterator i3 = aliens.iterator();
//		Random generator = new Random();

//		while (i3.hasNext()) {
//			int shot = generator.nextInt(25);
//			Alien a = (Alien)i3.next();
//			Alien.Bomb b = a.getBomb();
//			if ((shot == CHANCE) && (a.isVisible()) && (b.isDestroyed())) {
//				b.setDestroyed(false);
//				b.setX(a.getX());
//				b.setY(a.getY());
//			}
//
//			int bombX = b.getX();
//			int bombY = b.getY();
//			int playerX = player.getX();
//			int playerY = player.getY();
//
//			if ((player.isVisible()) && (!b.isDestroyed())) {
//				if ((bombX >= playerX) && (bombX <= playerX+PLAYER_WIDTH) && (bombY >= playerY) && (bombY <= playerY+PLAYER_HEIGHT)) {
//					ImageIcon ii = new ImageIcon(explImg);
//					player.setImage(ii.getImage());
//					player.setDying(true);
//					b.setDestroyed(true);
//				}
//			}
//
//			if (!b.isDestroyed()) {
//				b.setY(b.getY() + 1);
//				if (b.getY() >= 325 - BOMB_HEIGHT) {
//					b.setDestroyed(true);
//				}
//			}
//		}
	}

	public void run() {
		long beforeTime, timeDiff, sleep;
		beforeTime = System.currentTimeMillis();
		while (ingame) {
			repaint();
			animationCycle();

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
			player.keyPressed(e);
			int x = player.getX();
			int y = player.getY();
//			if (ingame) {
//				if (e.isAltDown()) {
//					if (!shot.isVisible()) {
//						shot = new Shot(x,y);
//					}
//				}
//			}
		}
	}


	private class MAdapter extends MouseAdapter{
		public void mouseClicked(MouseEvent m){
			int x = player.getX();
			int y = player.getY();
			if (ingame) {
				if (m.getButton() == MouseEvent.BUTTON1) {
					if (!shot.isVisible()) {
						shot = new Shot(x,y);
					}
				}
			}
		}
	}
}
