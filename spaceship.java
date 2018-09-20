import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.*;

public class spaceship
{
	public static void main(String...args) throws IOException
	{
		JFrame j = new JFrame();  
		MyPanel m = new MyPanel();
		j.setSize(m.getSize());
		j.add(m); 
		j.setVisible(true);
		j.setResizable(false);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
class MyPanel extends JPanel implements ActionListener, KeyListener, MouseListener
{
	private Timer time;
	private static int x;
	private static int y;
	private int rxcol;
	private int bycol;
	private int add;
	private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean shieldup = false;
    private int shield = 0;
    private boolean gameOn = true;
	public static ArrayList<SpaceObj> objs;
	private int playticks = 0;
	private int difficulty = 30; //30
	private boolean shieldpower = false;
	private int shieldcount = 0;
	private int deadticks = 0;
	private boolean explosion = true;
	private boolean startmusic = true;
	private int sx, sy;
	private boolean yesEvent = false;
	private boolean noSpawn = false;
	private int tempticks;
	private int eventTicks;
	private int eventCount;
	Iterator<SpaceObj> iter;
	private int dec;
	
	MyPanel()
	{
		time = new Timer(15, this);
		setSize(1600,850);
		setVisible(true);
		time.start();
		add=7; //speed of ship
		x=800;
		y=425;
		objs = new ArrayList<SpaceObj>();
		addMouseListener(this);
		setFocusable(true);
		addKeyListener(this);
		
	}
	
	public void paintComponent(Graphics g)
	{
		if (gameOn)
		{
			g.setColor(Color.BLACK);
			g.fillRect(0,0,1600,850);
			g.setColor(Color.WHITE);
			g.setFont(new Font("HELLO",1,20));
			if (shieldpower && !shieldup)
				g.drawString("SHIELD POWER: 100% Click to activate!",1,21);
			else g.drawString("SHIELD POWER: " + shield/5 + "%",1,21);
			g.drawString("Score: " + playticks,1,50);
			for (int k=0; k<40; k++){ //draw background stars
				sx = (int) (Math.random()*1600);
				sy = (int) (Math.random()*850);
				g.drawOval(sx, sy, 2, 2);
			}
			if (shieldup && shieldpower)
			{
				drawSS(g,x,y);
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				InputStream input = classLoader.getResourceAsStream("wowally.png");
				BufferedImage meme = null;
		        try {
		        	meme = ImageIO.read(input);
		        } catch (IOException ex){
		        	System.out.println("sad face");
		        }
		        g.drawImage(meme,180,625,null);
		        g.drawImage(meme,980,625,null);
				if (shield >= 1)
					shield--;
				else{
					shieldup = false;
					shieldpower = false;
					shieldcount--;
				}
			}
			else drawShip(g,x,y);
			iter = objs.iterator();
			while (iter.hasNext()){
				SpaceObj x = iter.next();
				if (checkCollision() && x.isShield()){ //picking up shield
					iter.remove();
					shieldpower = true;
					shield = 500;
				}
			}
			iter = objs.iterator();
			while (iter.hasNext()){
				SpaceObj x = iter.next();
        		x.draw(g, x.getX(), x.getY());
        		if (checkCollision())
        			if (!shieldup)
        				gameOn = false;
        		x.move();
        		if (x.despawn())
        			iter.remove();
			}
		}
		
		else{
			if (explosion){
			/*	try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e1) {
					System.out.println("wake me up inside");
				}
			*/	
				new Thread(new Runnable() {
		            public void run() {
		                try {
		                    Clip explosion = AudioSystem.getClip();
		                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(classLoader.getResourceAsStream("boomw.wav"));
		                    explosion.open(inputStream);
		                    explosion.start();
		                } catch (Exception e) {
		                    System.out.println("fizzled explosion :(");
		                }
		            }
		        }).start();
				explosion = false;
			}
			g.setColor(Color.BLACK);
			g.fillRect(0,0,1600,850);
			g.setColor(Color.WHITE);
			for (int k=0; k<40; k++){ //draw background stars
				sx = (int) (Math.random()*1600);
				sy = (int) (Math.random()*850);
				g.drawOval(sx, sy, 2, 2);
			}
			iter = objs.iterator();
			while (iter.hasNext()){
				SpaceObj x = iter.next();//moves all remaining objects
        		x.draw(g, x.getX(), x.getY());
        		x.move();
        		if (x.despawn()) //gets rid of off-screen objects
        			iter.remove();
			}
			deadticks++;
			drawDeath(g,x,y);
			if (deadticks > 65){ //delays timing
				g.setColor(Color.WHITE);
				g.setFont(new Font("HELLO",1,100));
				g.drawString("GAME OVER MAN",400,375);
				if (deadticks > 130){
					g.setFont(new Font("HELLO",1,50));
					g.drawString("Score: " + playticks,700,250);
					g.drawString("Press R to play again",600,450);
				}
			}
		}	
	}
	public void drawShip(Graphics g, int x, int y)
	{
		g.setColor(Color.GRAY);
		g.fillRect(x+12,y+24,16,4);
		g.drawLine(x+12,y+24,x,y+30);
		g.drawLine(x+28,y+24,x+40,y+30);
		g.drawLine(x+28,y+8,x,y);
		g.drawLine(x+12,y+8,x+40,y);
		g.setColor(Color.GRAY);
		g.fillOval(x+12,y,16,16);
		g.setColor(Color.GREEN);//alien
		g.fillOval(x+16,y+4,6,6);
		g.setColor(Color.MAGENTA);
		g.fillOval(x,y+8,40,16);
		g.setColor(Color.WHITE);
		g.fillRect(x+4, y+13, 12, 1);
		g.fillRect(x+24, y+13, 12, 1);
		g.setColor(Color.ORANGE);
		g.fillArc(x+16,y+24,8,8,0,-180);
		g.setColor(Color.RED);
		g.fillArc(x+18,y+26,4,4,0,-180);
		g.setColor(Color.CYAN);
		g.fillOval(x,y+14,2,2);
		g.fillOval(x+4,y+14,2,2);
		g.fillOval(x+8,y+14,2,2);
		g.fillOval(x+16,y+14,2,2);
		g.fillOval(x+23,y+14,2,2);
		g.fillOval(x+30,y+14,2,2);
		g.fillOval(x+34,y+14,2,2);
		g.fillOval(x+38,y+14,2,2);
	}
	
	public void drawSS(Graphics g, int x, int y)
	{
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillRect(x+12,y+24,16,4);
		g.drawLine(x+12,y+24,x,y+30);
		g.drawLine(x+28,y+24,x+40,y+30);
		g.drawLine(x+28,y+8,x,y);
		g.drawLine(x+12,y+8,x+40,y);
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillOval(x+12,y,16,16);
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillOval(x+16,y+4,6,6);
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillOval(x,y+8,40,16);
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillRect(x+4, y+13, 12, 1);
		g.fillRect(x+24, y+13, 12, 1);
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillArc(x+16,y+24,8,8,0,-180);
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillArc(x+18,y+26,4,4,0,-180);
		g.setColor(new Color((int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1,(int)(Math.random() * 255) + 1));
		g.fillOval(x,y+14,2,2);
		g.fillOval(x+4,y+14,2,2);
		g.fillOval(x+8,y+14,2,2);
		g.fillOval(x+16,y+14,2,2);
		g.fillOval(x+23,y+14,2,2);
		g.fillOval(x+30,y+14,2,2);
		g.fillOval(x+34,y+14,2,2);
		g.fillOval(x+38,y+14,2,2);
	}	
	public void drawDeath(Graphics g,int x, int y)
	{
		g.setColor(new Color(255, (int)(Math.random()*256), 0));
		g.fillOval(x-300, y-300,600,600);
		g.setColor(new Color(255, (int)(Math.random()*256), 0));
		g.fillOval(x-((int)(Math.random()*100)+150),y-((int)(Math.random()*100)+150),400,400);
		for( int a=0; a<5; a++)
		{
			g.setColor(new Color(255, (int)(Math.random()*256), 0));
			g.fillOval(x-((int)(Math.random()*200)-50),y-((int)(Math.random()*200)-50),80,80);
			g.setColor(new Color(255, (int)(Math.random()*256), 0));
			g.fillOval(x-((int)(Math.random()*200)-50),y-((int)(Math.random()*200)-50),100,100);
		}
	}
	public void actionPerformed(ActionEvent e)
	{
		if (gameOn){
			playticks++;
			
			if (leftDirection && x > 2) {
				x -= add;
			}
			
			if (rightDirection && x < 1400) {
				x += add;
			}

			if (upDirection && y > 4) {
				y -= add;
			}

			if (downDirection && y < 795) {
				y += add;
			}
        rxcol = x+40;
        bycol = y+30;
    if (!noSpawn){
        if (playticks % 100 == 0 && difficulty > 16){ //increasing difficulty, capped at 1spawn/16ticks
        	difficulty--;
        	System.out.println(difficulty);
        }
        if (playticks%difficulty == 0){ //random spawning
        	int rng = (int) (Math.random()*100);
        	if (rng < 42)  // 42/100
        		objs.add(new smallAsteroid()); 
        	if (rng > 41 && rng < 74) // 32/100
        		objs.add(new largeAsteroid());
        	if (rng > 73 && rng < 89) // 15/100
        		objs.add(new spaceJunk());
        	if (rng > 88 && rng < 98) // 9/100
        		objs.add(new Laser());
        	if (rng == 98 && shieldcount == 0){ // 1/100
        		objs.add(new shield());
        		shieldcount++;
        	}
        	if (rng > 98 && difficulty < 20){ // 1/100; event
        		noSpawn = true;
        		eventTicks = 882;
        	}
        	if (rng < 2)  // 2/100
        		objs.add(new Andy());
        	if (rng > 1 && rng < 4)
        		objs.add(new andyzork());
        }
    }
        	if (playticks > 100 && objs.isEmpty() && !yesEvent && noSpawn){
        		eventCount = 0;
        		tempticks = playticks;
        		yesEvent = true;
        		dec = (int)(Math.random()*2);
        	}
        	if (yesEvent){
        		if (dec == 0)
        			event("laserEvent");
        		else event("andEvent");
        	}
        	if (yesEvent && playticks > tempticks+eventTicks){
        		yesEvent = false; noSpawn = false;
        	}
        if (startmusic){
        	new Thread(new Runnable() {
	            public void run() {
	                try {
	                    Clip music = AudioSystem.getClip();
	                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(classLoader.getResourceAsStream("Darude.wav"));
	                    music.open(inputStream);
	                    music.start(); music.loop(music.LOOP_CONTINUOUSLY);
	                } catch (Exception e) {
	                    System.out.println("what song is this? :(");
	                }
	            }
	        }).start();
        	startmusic = false;
        }
		} //if gameOn}
        repaint();
	}
	public boolean checkCollision()
	{	
		for (SpaceObj so:objs)
			if ((so.getX() >= x && so.getX() <= rxcol || so.getrxcol() >= x && so.getrxcol() <= rxcol || so.getX() <= x && rxcol <= so.getrxcol())
				 && (so.getY() >= y && so.getY() <= bycol || so.getbycol() >= y && so.getbycol() <= bycol || so.getY() <= y && bycol <= so.getbycol()))
				return true;
		return false;
	}
	
	public void keyPressed(KeyEvent e)
	{
            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
            }

            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
            // game reset
            if (key == KeyEvent.VK_R)
            	if (!gameOn){
            		gameOn = true;
            		objs.clear();
            		x = 800; y = 425;
            		leftDirection = false;
            	    rightDirection = true;
            	    upDirection = false;
            	    downDirection = false;
            	    explosion = true;
            	    shieldup = false;
            	    shieldpower = false;
            	    yesEvent = false;
            		playticks = 0;
            		difficulty = 30;
            		shield = 0;
            		shieldcount = 0;
            		deadticks = 0;
            		yesEvent = false;
            		noSpawn = false;
            	}	

	}
	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	public void mousePressed(MouseEvent e){
		if (shieldpower)
			shieldup = true;
		repaint();
	}
	public void mouseReleased(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	
	public void event(String e){
		if (e.equals("laserEvent")){
			if (eventCount == 0){
				objs.add(new Laser(200, 360));
				eventCount++;
			}	
			if (playticks > tempticks + 50 && eventCount == 1){
				objs.add(new Laser(400, 320));
				eventCount++;
			}
			if (playticks > tempticks + 100 && eventCount == 2){
				objs.add(new Laser(600, 280));
				eventCount++;
			}
			if (playticks > tempticks + 400 && eventCount == 3){
				objs.add(new Laser(760, 110));
				eventCount++;
			}
			if (playticks > tempticks + 410 && eventCount == 4){
				objs.add(new Laser(720, 110));
				eventCount++;
			}
			if (playticks > tempticks + 420 && eventCount == 5){
				objs.add(new Laser(680, 110));
				eventCount++;
			}
			if (playticks > tempticks + 430 && eventCount == 6){
				objs.add(new Laser(640, 110));
				eventCount++;
			}
			if (playticks > tempticks + 440 && eventCount == 7){
				objs.add(new Laser(600, 110));
				eventCount++;
			}
			if (playticks > tempticks + 450 && eventCount == 8){
				objs.add(new Laser(560, 110));
				eventCount++;
			}
			if (playticks > tempticks + 460 && eventCount == 9){
				objs.add(new Laser(520, 110));
				eventCount++;
			}
			if (playticks > tempticks + 470 && eventCount == 10){
				objs.add(new Laser(480, 110));
				eventCount++;
			}
			if (playticks > tempticks + 480 && eventCount == 11){
				objs.add(new Laser(440, 110));
				eventCount++;
			}
			if (playticks > tempticks + 600 && eventCount == 12){
				objs.add(new Laser(0, 110));
				eventCount++;
			}
			if (playticks > tempticks + 608 && eventCount == 13){
				objs.add(new Laser(45, 110));
				eventCount++;
			}
			if (playticks > tempticks + 616 && eventCount == 14){
				objs.add(new Laser(90, 110));
				eventCount++;
			}
			if (playticks > tempticks + 624 && eventCount == 15){
				objs.add(new Laser(135, 110));
				eventCount++;
			}
			if (playticks > tempticks + 632 && eventCount == 16){
				objs.add(new Laser(180, 110));
				eventCount++;
			}
			if (playticks > tempticks + 640 && eventCount == 17){
				objs.add(new Laser(225, 110));
				eventCount++;
			}
			if (playticks > tempticks + 648 && eventCount == 18){
				objs.add(new Laser(270, 110));
				eventCount++;
			}
			if (playticks > tempticks + 656 && eventCount == 19){
				objs.add(new Laser(315, 110));
				eventCount++;
			}
			if (playticks > tempticks + 664 && eventCount == 20){
				objs.add(new Laser(350, 110));
				eventCount++;
			}
			if (playticks > tempticks + 672 && eventCount == 21){
				objs.add(new Laser(395, 110));
				eventCount++;
			}
		}
		if (e.equals("andEvent")){
			if (eventCount == 0){
				objs.add(new andyzork(1));
				eventCount++;
			}
			if (eventCount == 1){
				objs.add(new andyzork(2));
				eventCount++;
			}
			if (eventCount == 2){
				objs.add(new andyzork(3));
				eventCount++;
			}
			if (eventCount == 3){
				objs.add(new andyzork(4));
				eventCount++;
			}
		}
	}
	public static int getSX(){
		return x;
	}
	public static int getSY(){
		return y;
	}
}
class SpaceObj{
	private int x, y, rxcol, bycol;
	
	public SpaceObj(){
	}	
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return rxcol;
	}
	public int getbycol(){
		return bycol;
	}
	public boolean despawn(){
		return false;
	}
	public void move(){
		
	}
	public void draw(Graphics g, int x, int y){
		
	}
	public boolean isShield(){
		return false;
	}
}

class andyzork extends SpaceObj{
	private int x, y, place, lifetime;
	
	public andyzork(){
		super();
		place = (int) (Math.random()*4+1); //determines spawning side, same for rest of space objects
		if (place == 1){ //left
			x = -90; y = (int)(Math.random()*771);
		}
		if (place == 2){ //bottom
			x = (int)(Math.random()*1521); y = -90;
		}
		if (place == 3){ //right
			x = 1600; y = (int)(Math.random()*771);
		}
		if (place == 4){ //top
			x = (int)(Math.random()*1521); y = 800;
		}
		lifetime = 0;
	}
	public andyzork(int z){
		super();
		place = z;
		if (z == 1){
			x = -90; y = 400;
		}
		if (z == 2){
			x = 800; y = -90;
		}
		if (z == 3){
			x = 1600; y = 400;
		}
		if (z == 4){
			x = 800; y = 800;
		}
		lifetime = 0;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return x+90;
	}
	public int getbycol(){
		return y+90;
	}
	public boolean despawn(){
		if (place % 2 == 0)
			return lifetime > 450;
		else return lifetime > 700;
	}	
	public void move(){
		if (place == 1)
			x+=2;
		if (place == 2)
			y+=2;
		if (place == 3)
			x-=2;
		if (place == 4)
			y-=2;
		if (lifetime % 50 == 0)
			MyPanel.objs.add(new shot(x+10,y+5));
		lifetime++;
	}
	public void draw(Graphics g, int x, int y){
		g.setColor(new Color(169,169,169));
		g.fillRect(x-5,y-5,100,100);
		g.setColor(Color.GRAY);
		g.drawLine(x-10, y-10, x-5, y-5);
		g.drawLine(x+100, y-10, x+95, y-5);
		g.drawLine(x-10, y+100, x-5, y+95);
		g.drawLine(x+100, y+100, x+95, y+95);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("andzork.png");
		BufferedImage az = null;
        try {
        	az = ImageIO.read(input);
        } catch (IOException ex){
        	System.out.println("boosted zork");
        }
        g.drawImage(az,x,y,null);
	}
}

class shot extends SpaceObj{
	private int x, y, xdis, ydis, dis, lifetime;
	private double slope, factor;
	
	public shot(int ax, int ay){
		x = ax;
		y = ay;
		xdis = MyPanel.getSX() - x;
		ydis = MyPanel.getSY() - y;
		slope = Math.abs((ydis*1.0)/(xdis*1.0));
		if (slope > 1.0)
			factor = 10.0 / Math.sqrt(slope*slope+1);
		else factor = 10.0 / Math.sqrt((1/slope)*(1/slope)+1);
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return x+20;
	}
	public int getbycol(){
		return y+20;
	}
	public boolean despawn(){
		return lifetime > 180;
	}	
	public void move(){
	//	System.out.println(slope);
	//	if (slope > 10.0)
	//		slope = 10.0;
	//	if (1.0/slope > 10.0)
	//		slope = 0.1;
		System.out.println(slope);
	//	for (int k=0;k<factor;k++){
			if (ydis > 0){
				if (slope >= 1.0)
					y+=(slope*factor);
				else y+=factor;
			}
			else if (ydis < 0){
				if (slope >= 1.0)
					y-=(slope*factor);
				else y-=factor;
			}
			if (xdis > 0){
				if (slope >=1.0)
					x+=factor;
				else x+= (1.0/slope*factor);
			}
			else if (xdis < 0){
				if (slope >= 1.0)
					x-=factor;
				else x-= (1.0/slope*factor);
			}
	//	}
		lifetime++;
	}
	public void draw(Graphics g, int x, int y){
			g.setColor(Color.RED);
			g.fillOval(x+30,y+75,20,20);
			g.setColor(Color.ORANGE);
			g.fillOval(x+32,y+77,16,16);
	}
}

class Andy extends SpaceObj{
	private int x, y, place, lifetime;
	
	public Andy(){
		super();
		place = (int) (Math.random()*4+1); //determines spawning side, same for rest of space objects
		if (place == 1){ //left
			x = -50; y = (int)(Math.random()*801);
		}
		if (place == 2){ //bottom
			x = (int)(Math.random()*1601); y = -50;
		}
		if (place == 3){ //right
			x = 1600; y = (int)(Math.random()*801);
		}
		if (place == 4){ //top
			x = (int)(Math.random()*1601); y = 800;
		}
		lifetime = 0;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return x+50;
	}
	public int getbycol(){
		return y+50;
	}
	public boolean despawn(){
		if (place % 2 == 0)
			return lifetime > 100;
		else return lifetime > 150;
	}	
	public void move(){
		if (place == 1)
			x+=10;
		if (place == 2)
			y+=10;
		if (place == 3)
			x-=10;
		if (place == 4)
			y-=10;
		lifetime++;
	}
	public void draw(Graphics g, int x, int y){
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("Andystruction.png");
		BufferedImage ando = null;
        try {
        	ando = ImageIO.read(input);
        } catch (IOException ex){
        	System.out.println("boosted boi");
        }
        g.drawImage(ando,x,y,null);
	}
}

class smallAsteroid extends SpaceObj{
	private int x, y, place, lifetime;
	
	public smallAsteroid(){
		super();
		place = (int) (Math.random()*4+1);
		if (place == 1){ //left
			x = -40; y = (int)(Math.random()*811);
		}
		if (place == 2){ //bottom
			x = (int)(Math.random()*1601); y = -40;
		}
		if (place == 3){ //right
			x = 1600; y = (int)(Math.random()*811);
		}
		if (place == 4){ //top
			x = (int)(Math.random()*1601); y = 800;
		}
		lifetime = 0;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return x+40;
	}
	public int getbycol(){
		return y+40;
	}
	public boolean despawn(){
		if (place % 2 == 0)
			return lifetime > 147;
		else return lifetime > 272;
	}	
	public void move(){
		if (place == 1)
			x+=6;
		if (place == 2)
			y+=6;
		if (place == 3)
			x-=6;
		if (place == 4)
			y-=6;
		lifetime++;
	}
	public void draw(Graphics g, int x, int y){
		g.setColor(Color.GRAY);
		g.fillOval(x,y,40,35);
		g.fillOval(x+10,y+20, 40, 30);
		g.setColor(new Color(150,150,150));
		g.fillOval(x+30, y,20,40);
		g.setColor(new Color(105,105,105));
		g.fillOval(x,y+20,40,30);
		g.setColor(new Color(140,140,140));
		g.fillOval(x+25,y+10, 15, 30);
		g.setColor(new Color(105,105,105));
		g.fillOval(x+20, y+20, 10,10);
		g.fillOval(x+35, y+10, 5, 5);
		g.fillOval(x+15, y+15, 7, 5);
		g.setColor(new Color(100,100,100));
		g.fillOval(x+14, y+30, 9, 9);
	}
}
	
class largeAsteroid extends SpaceObj{
	private int x, y, place, lifetime;
	
	public largeAsteroid(){
		super();
		place = (int) (Math.random()*4+1);
		if (place == 1){
			x = -60; y = (int)(Math.random()*791);
		}
		if (place == 2){
			x = (int)(Math.random()*1601); y = -60;
		}
		if (place == 3){
			x = 1600; y = (int)(Math.random()*791);
		}
		if (place == 4){
			x = (int)(Math.random()*1601); y = 800;
		}
		lifetime = 0;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return x+60;
	}
	public int getbycol(){
		return y+60;
	}
	public boolean despawn(){
		if (place % 2 == 0)
			return lifetime > 300;
		else return lifetime > 570; //rip blaze
	}	
	public void move(){
		if (place == 1)
			x+=3;
		if (place == 2)
			y+=3;
		if (place == 3)
			x-=3;
		if (place == 4)
			y-=3;
		lifetime++;
	}
	public void draw(Graphics g, int x, int y){
		g.setColor(Color.GRAY);
		g.fillOval(x,y,60,45);
		g.fillOval(x+15,y+30, 60, 45);
		g.setColor(new Color(150,150,150));
		g.fillOval(x+45, y,30,60);
		g.setColor(new Color(105,105,105));
		g.fillOval(x,y+30,60,45);
		g.setColor(new Color(140,140,140));
		g.fillOval(x+30,y+15, 22, 45);
		g.setColor(new Color(105,105,105));
		g.fillOval(x+30, y+30, 15,15);
		g.fillOval(x+50, y+15, 7, 7);
		g.fillOval(x+22, y+22, 8, 7);
		g.setColor(new Color(100,100,100));
		g.fillOval(x+21, y+45, 11, 11);
	}
}

class spaceJunk extends SpaceObj{
	private int x, y, place, lifetime;
	
	public spaceJunk(){
		super();
		place = (int) (Math.random()*2+1);
		if (place == 1){
			x = (int)(Math.random()*1571); y = -40;
		}
		if (place == 2){
			x = (int)(Math.random()*1571); y = 850;
		}
		lifetime = 0;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return x+30;
	}
	public int getbycol(){
		return y+45;
	}
	public boolean despawn(){
		return lifetime > 185;
	}	
	public void move(){
		if (place == 1)
			y+=9;
		if (place == 2)
			y-=9;
		lifetime++;
	}
	public void draw(Graphics g, int x, int y){
		g.setColor(Color.GRAY);
		g.drawRect(x+10,y,10,2);
		g.setColor(new Color(169,169,169));
		g.fillRect(x,y+2,15,5);
		g.setColor(Color.GRAY);
		g.fillRect(x+15,y+2,15,5);
		g.setColor(new Color(192,192,192));
		g.fillRect(x+2,y+7,14,28);
		g.setColor(new Color(169,169,169));
		g.fillRect(x+6,y+7,10,28);
		g.setColor(new Color(192,192,192));
		g.fillRect(x+10,y+7,6,28);
		g.setColor(Color.GRAY);
		g.fillRect(x+14,y+7,14,28);
		g.setColor(new Color(169,169,169));
		g.fillRect(x+18,y+7,10,28);
		g.setColor(Color.GRAY);
		g.fillRect(x+22,y+7,6,28);
		g.setColor(new Color(169,169,169));
		g.fillRect(x+26,y+7,2,28);
	}
}

class Laser extends SpaceObj{
	private int x, y, lifetime, lifeEnd;
	
	public Laser(){
		super();
		y = (int)(Math.random()*811);
		lifetime = 0;
		lifeEnd = 180;
	}
	public Laser(int sety, int l){
		super();
		y = sety;
		lifetime = 0;
		lifeEnd = l;
	}
	public int getX(){
		return 0;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		if (lifetime < 90)
			return 0;
		else return 1600;
	}
	public int getbycol(){
		if (lifetime < 90)
			return 0;
		else return y + 40;
	}
	public boolean despawn(){
		return lifetime > lifeEnd;
	}	
	public void move(){
		lifetime++;
	}
	public void draw(Graphics g, int x, int y){
		if (lifetime < 90){
			if (lifetime % 10 > 4){}
			else {	
				g.setColor(Color.RED);
				g.fillRect(5,y,5,30);
				g.fillRect(5,y+35,5,5);
				g.fillRect(15,y,5,30);
				g.fillRect(15,y+35,5,5);
				g.fillRect(1400,y,5,30); //1590
				g.fillRect(1400,y+35,5,5);
				g.fillRect(1390,y,5,30); //1580
				g.fillRect(1390,y+35,5,5);
			}
		}
		else {
		g.setColor(new Color((int)(Math.random() * 255) + 1,255,255));
		g.fillRect(0,y,1600,40);
		g.setColor(Color.WHITE);
		g.fillRect(0,y+5,1600,30);
		g.setColor(new Color((int)(Math.random() * 255) + 1,200,255));
		for (int l=1;l<=42;l++) //42 is 6*7
			g.fillRect((int)(Math.random()*1600),y+5+(int)(Math.random()*27),(int)(Math.random()*60)+10,4);
		}
	}
}

class shield extends SpaceObj{
	private int x, y;
	
	public shield(){
		super();
		x = (int)(Math.random()*1601);
		y = (int)(Math.random()*811);
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;	
	}
	public int getrxcol(){
		return x+40;
	}
	public int getbycol(){
		return y+40;
	}
	public boolean isShield(){
		return true;
	}
	public void draw(Graphics g, int x, int y){
		g.setColor(Color.CYAN);
		g.fillOval(x,y,40,40);
		g.setColor(Color.WHITE);
		g.fillOval(x+10, y+10, 20, 20);
	}
}