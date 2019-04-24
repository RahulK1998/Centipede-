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

	private boolean ingame = true;
	private final String explImg = "./img/explosion.png";
	private final String alienImg = "./img/mushroom1.png";
	private final String m3 = "./img/mushroom2.png";
	private final String m2 = "./img/mushroom3.png";
	private final String m1 = "./img/mushroom4.png";
	private final String centImg = "./img/centipede2.png";
	private final String cent2Img = "./img/centipede3.png";
	private final String sp2 = "./img/spider2.png";

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
//		shot = new Shot();
//		shots.add(shot);
//		Creating shots
//		ImageIcon iii = new ImageIcon(alienImg);


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
			//wrong random number, mushroom out of bound handling

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
			}
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
		if (deaths == noAliens) {
			ingame = false;
			message = "You win! Your HighScore: " + score ;
		}
		//System.out.println(noAliens+" "+ deaths);

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

		player.act();

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


//		if ((x >= BOARD_WIDTH - BORDER_RIGHT) && (ct.getDirection() != -1)) {
//			ct.setDirection(-1);
//			if(!(y > BOARD_HEIGHT - BORDER_RIGHT - (3* SPRITE_HEIGHT))){
//				ct.setY(ct.getY() + GO_DOWN);
//			}
//
//		}
//		else if ((x <= BORDER_LEFT) && (ct.getDirection() != 1)) {
//			ct.setDirection(1);
//			if(!(y > BOARD_HEIGHT - BORDER_RIGHT - (3* SPRITE_HEIGHT))){
//				ct.setY(ct.getY() + GO_DOWN);
//			}
//		}




		//Mushroom Collision Avoidance
		Iterator segit = segments.iterator();
		while(segit.hasNext()) {
			Centipede segOne = (Centipede) segit.next();
			if(segOne.isVisible()) {
				Iterator alit = aliens.iterator();
				while (alit.hasNext()){
					Alien shroom = (Alien) alit.next();
					if (shroom.isVisible()) {
						//System.out.println("I'm in 1" + (shroom.isVisible()));
						if ((segOne.getX() <= shroom.getX() + ALIEN_WIDTH) && (segOne.getX() + ALIEN_WIDTH >= shroom.getX())) {
							if ((segOne.getY() + ALIEN_HEIGHT >= shroom.getY()) && (segOne.getY() <= shroom.getY() + ALIEN_HEIGHT)) {
								//System.out.println("I'm in 2");
								segOne.setY(segOne.getY() + GO_DOWN);
								//System.out.println("x -> "+x +" x + Alien ->" +(x+ ALIEN_WIDTH )+ " shroom->" + shroom.getX() );
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
						//player.setDying(true); //will not matter
						System.out.println("Head-on Collision with Centipede ");
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
					//player.setDying(true); //will not matter
					System.out.println("Head-on Collision with Spider");
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
			player.keyPressed(e);
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
					//create iterator
					Shot sh = new Shot(x,y);
					shots.add(sh);
				}
			}
		}
	}
}
