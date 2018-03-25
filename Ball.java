import java.applet.*;
import java.awt.*;
import java.util.*;

public class Ball{
   //declare variables
   //side lengths of triangles, or distance from center (300,200)
   private int x,y; 
   //current locations
   private int xi,yi;
   //initial radius (preserved for scaling purposes)
   private int initRad;
   //current radius
   private int radius;
   //propotion (out of ten)
   private double scale; 
   //increase in proportion
   private double dScale;
   //color
   private Color color;
   //whether or not the ball has been hit
   private boolean hit;
   //after it has been colored green, it will indicate the ball is to be destroyed
   private boolean remove;
   
   
   
   //methods
   //constructors
   //full constructor
   public Ball(int xVal,int yVal,Color color,double scl, double dscl, int rad){
      color = color;
      xi = xVal;
      yi = yVal;
      x = xVal - 300;
      y = yVal - 200;
      scale = scl;
      dScale = dscl;
      radius = rad;
      initRad = rad;
      System.out.println("new Ball()"); //let's us know a Ball was created  
      hit = false;
      remove = false;
   }
   
   //moves the ball farther out and makes it bigger
   public void scale(){
      scale += dScale;
      xi = (int)(x*scale)+300;
      yi = (int)(y*scale)+200;
      radius = (int)(initRad*scale);
      System.out.println("scale()");
   }
   
   //tells the ball it has been hit
   public void hitBall(){
      hit = true;
      System.out.println("hitBall()");
   }
   
   //tells us if the ball has been hit
   public boolean isHit(){
      return hit;
   }
   
   //tells program to destroy ball after it has been displayed
   public void removeNext(){
      remove = true;
   }
   
   //accessors
   public Color getColor(){
      return color;
   }
   
   public int getRadius(){
      return radius;
   }
   
   public int getXi(){
      return xi;
   }
   
   public int getYi(){
      return yi;
   }
   
   public double getScale(){
      return scale;
   }
   
   public boolean shouldRemove(){
      return remove;
   }
}
   