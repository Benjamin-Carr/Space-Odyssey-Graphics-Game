import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;

public class ShooterGame extends Applet implements Runnable, MouseListener, MouseMotionListener, KeyListener {

   int width, height;
   int i = 0;
   Thread t = null;
   boolean threadSuspended;
   
   //backbuffer stuff
   Image backbuffer;
   Graphics backg;
   
   //colors for the balls
   Color[] colors;
   
   //holds all the balls
   ArrayList<Ball> balls;
   
   //interval is the time between ball generatings
   //max and min intervals are used for generating a pseudo-random interval number
   int maxInterval, minInterval, interval;
   
   //mouse coordinates 
   int mx, my; 
   
   //crosshair thickness
   int cThickness;
   //crosshair width
   int cWidth;
   
   //lives and score (duh)
   int lives, score;
   
   //if the game should be running i.e. you haven't lost yet
   private boolean playing;
	
	//extra life every time you get this many points
	int extraLife;
	
	//sounds for background, shooting, hitting and being hit
	AudioClip music, shoot, green, red;


   // Executed when the applet is first created.
   public void init() {
      System.out.println("init(): begin");
      width = getSize().width;
      height = getSize().height;
      setBackground( Color.black );
      System.out.println("init(): end");
      
      
      backbuffer = createImage(width, height);
      backg = backbuffer.getGraphics();
      
      
      balls = new ArrayList<Ball>();
      
      colors = new Color[7]; //the colors don't seem to be working atm...
      colors[0] = Color.white;
      colors[1] = Color.red;
      colors[2] = Color.yellow;
      colors[3] = Color.blue;
      colors[4] = Color.green;
      colors[5] = Color.orange;
      colors[6] = Color.pink;
      
      minInterval = 15;
      maxInterval = 25;
      interval = 40;
      
      //mouse stuff
      mx = width/2; 
      my = height/2; 
      addMouseListener( this ); 
      addMouseMotionListener( this); 
      cThickness = 3;
      cWidth = 50;
		
		addKeyListener(this);
      
      lives = 4;
		extraLife = 5000;
      score = 0;
      
      playing = true;
		
		music = getAudioClip(getDocumentBase(), "Ashes.wav"); 
		//	http://www.soundzabound.com/sabcs?terms=1690
		shoot = getAudioClip(getDocumentBase(), "laserShoot.wav"); 
		//	https://www.freesound.org/people/AlienXXX/sounds/195681/
		red = getAudioClip(getDocumentBase(), "badSound.wav"); 
		//	https://www.freesound.org/people/simon.rue/sounds/49948/
		green = getAudioClip(getDocumentBase(), "goodSound.wav");
		//	https://www.freesound.org/people/Bertrof/sounds/131662/
		music.play();

   
   }

   // Executed when the applet is destroyed.
   public void destroy() {
      System.out.println("destroy()");
   }

   // Executed after the applet is created; and also whenever
   // the browser returns to the page containing the applet.
   public void start() {
      System.out.println("start(): begin");
      if ( t == null ) {
         System.out.println("start(): creating thread");
         t = new Thread( this );
         System.out.println("start(): starting thread");
         threadSuspended = false;
         t.start();
      }
      else {
         if ( threadSuspended ) {
            threadSuspended = false;
            System.out.println("start(): notifying thread");
            synchronized( this ) {
               notify();
            }
         }
      }
      System.out.println("start(): end");
   }

   // Executed whenever the browser leaves the page containing the applet.
   public void stop() {
      System.out.println("stop(): begin");
      threadSuspended = true;
   }

   // Executed within the thread that this applet created.
   public void run() {
      System.out.println("run(): begin");
      try {
         while (true) {
            System.out.println("run(): awake");
            
            
            // i is pretty much just here to let us see how fast the steps are progressing
            ++i;  
            if ( i == colors.length ) {
               i = 0;
            }
            showStatus( "i is " + i + "     interval = " + interval + "     Mouse at (" + mx + "," + my + ")");
            if(playing){
            //moves the balls out and makes them bigger
               for(int i=0; i<balls.size(); i++){
                  balls.get(i).scale();
               }
            
            //random Ball generation
               if(interval<=0){
                  balls.add(new Ball((int)(Math.random()*60+270),(int)(Math.random()*30+185),colors[i], 1.0, 0.25,15));
                  interval = (int)(Math.random()*(maxInterval-minInterval)+minInterval);
                  System.out.println("new Ball()");
               }
               else{
                  interval--;
               }
            
            //deals with the balls
               int j = 0;
               while(j < balls.size()){
                  if(balls.get(j).shouldRemove()){
                     balls.remove(j);  //actually removes the ball (if it was hit)
                  }
                  else if(balls.get(j).isHit()){
                     balls.get(j).removeNext(); //tells program to remove ball after another step (so the player can see it turn green)
                     j++;
                     score += 50;   //increases score for destoying a ball
                     if(score%700==0 && minInterval>1){//every 14 balls decrease minInterval
                        minInterval--;
                     }
                     if(score%1400==0 && maxInterval>1){//every 28 balls decreases maxInterval
                        maxInterval--;
                     }
							if(score%extraLife==0 && score>=extraLife){	//new life every 10000 points
								lives++;
							}
                  }
                  else if(balls.get(j).getScale()>10){
                     balls.remove(j);  //removes the ball (if it hits the player)
							red.play();
                     lives--; //lose a life for letting a ball hit you
                     if(lives<=0){
                        playing = false;//lose all your lives and you lose
                     }
                  }
                  else{
                     j++;
                  }
               }
            }
            
            // thread checks to see if it should suspend itself
            if ( threadSuspended ) {
               synchronized( this ) {
                  while ( threadSuspended ) {
                     System.out.println("run(): waiting");
                     wait();
                  }
               }
            }
            
            //updates the image
            System.out.println("run(): requesting repaint");
            repaint();
            System.out.println("run(): sleeping");
            
            t.sleep( 25 );  // interval given in milliseconds
         }
      }
      catch (InterruptedException e) { }
      System.out.println("run(): end");
   }
   
   public void mouseEntered( MouseEvent e ) {}
   public void mouseExited( MouseEvent e ) {}
   public void mousePressed( MouseEvent e ) {
		shoot.play();
      if(!playing){
         //init();
      }
      else{
         int ballX, ballY;
         int j = 0;
         boolean hitABall = false;
         while(j < balls.size() && !hitABall){
            System.out.println("ballHit()");
            ballX = balls.get(j).getXi();
            ballY = balls.get(j).getYi();
            if(Math.sqrt(Math.pow(ballX-mx,2) + Math.pow(ballY-my,2)) <= balls.get(j).getRadius()){
               balls.get(j).hitBall();
					green.play();
               hitABall = true;
            }
            j++;
         }
      }
         
   }
   public void mouseClicked( MouseEvent e ) {}
   public void mouseReleased( MouseEvent e ) {}
   public void mouseMoved( MouseEvent e ) {
      mx = e.getX();
      my = e.getY();
      e.consume();
   }
   public void mouseDragged( MouseEvent e ) {
      mx = e.getX();
      my = e.getY();
      e.consume();
   }
	
	public void keyPressed( KeyEvent e ) { }
   public void keyReleased( KeyEvent e ) { }
   public void keyTyped( KeyEvent e ) {
		System.out.print("keyTyped()");
		char c = e.getKeyChar();
		if(!playing){
			init();
		}
		e.consume();
	}


   
   public void paint( Graphics g ) {
      update(g);
   }
   
   public void update( Graphics g){
      backbuffer = createImage(width, height);
      backg = backbuffer.getGraphics();
     
      if(lives>0){
      //paints each ball on the backbuffer
         for(int i=balls.size()-1; i>=0; i--){
         //backg.setColor(balls.get(i).getColor());
         //System.out.println(balls.get(i).getColor());
            if(balls.get(i).isHit()){
               backg.setColor(Color.green);
            }
            else if(balls.get(i).getScale() <= 10 && balls.get(i).getScale() >= 9.75){
               backg.setColor(Color.red);
            }
            else{
               backg.setColor(Color.white);
            }
            System.out.println((balls.get(i).getXi() - balls.get(i).getRadius())+ ", " +(balls.get(i).getYi() - balls.get(i).getRadius()) + ", " + balls.get(i).getRadius()*2);
            backg.fillOval(balls.get(i).getXi() - balls.get(i).getRadius(), balls.get(i).getYi() - balls.get(i).getRadius(), balls.get(i).getRadius()*2, balls.get(i).getRadius()*2);
            System.out.println("drawBall()"); //let's us know the method executed
         }
      }
      else{
         Font scoreFont = new Font( "Monospaced", Font.BOLD, 48 );
         backg.setFont(scoreFont);
         backg.setColor(Color.blue);
         lives = 0;
         backg.drawString("GAME OVER",170,190);
         scoreFont = new Font("Monospaced",Font.BOLD,24);
         backg.setFont(scoreFont);
         backg.drawString("press any key to restart",140,215);
			scoreFont = new Font("Monospaced",Font.BOLD,18);
			backg.setFont(scoreFont);
			backg.drawString("Game design/programming: Benjamin Carr",95,240);
			backg.drawString("Music: \"Ashes\" by Claire Baum from soundzabound.com",20,260);
			backg.drawString("Sound effects from freesound.org:",115,280);
			backg.drawString("Space Gun_006a by AlienXXX",150,300);
			backg.drawString("Game Sound Correct_v2 by Bertrof",125,320);
			backg.drawString("misslyckad bana v5", 200, 340);
      }
      //draws the lives and score on the backbuffer
      Font scoreFont = new Font( "Monospaced", Font.BOLD, 36 );
      backg.setFont(scoreFont);
      backg.setColor(Color.blue);
      backg.drawString("Score: " + score,50,390);
      String tempStr;
      if(lives<10){
         tempStr = "0";
      }
      else{
         tempStr = "";
      }
      backg.drawString("Lives: " + tempStr + lives,350,390);
		if(score>=extraLife && score%extraLife>=0 && score%extraLife <=50){
			backg.drawString("EXTRA LIFE!",175,50);
		}
   
      
      //draws crosshairs on the backbuffer
      backg.setColor(Color.gray);
      for(int j = 0; j < cThickness; j++){
         backg.drawOval((mx-(cWidth/2)+j),(my-(cWidth/2)+j),(cWidth-2*j),(cWidth-2*j));
      }
      backg.fillOval(mx-cThickness,my-cThickness,cThickness*2,cThickness*2);
      backg.fillRect(mx-(int)(cThickness/2),my-cWidth/2,cThickness,cWidth/4);
      backg.fillRect(mx-(int)(cThickness/2),my+cWidth/4,cThickness,cWidth/4);
      backg.fillRect(mx-cWidth/2,my-(int)(cThickness/2),cWidth/4,cThickness);
      backg.fillRect(mx+cWidth/4,my-(int)(cThickness/2),cWidth/4,cThickness);
      //draws backbuffer over image
      g.drawImage(backbuffer, 0, 0, this);
      System.out.println("update()"); //let's us know it updated
   }
}
