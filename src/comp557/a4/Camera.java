package comp557.a4;

import java.awt.Dimension;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple camera object, which could be extended to handle a variety of 
 * different camera settings (e.g., aperature size, lens, shutter)
 */
public class Camera {
	
	/** Camera name */
    public String name = "camera";

    /** The eye position */
    public Point3d from = new Point3d(0,0,10);
    
    /** The "look at" position */
    public Point3d to = new Point3d(0,0,0);
    
    /** Up direction, default is y up */
    public Vector3d up = new Vector3d(0,1,0);
    
    /** Vertical field of view (in degrees), default is 45 degrees */
    public double fovy = 45.0;
    
    /** The rendered image size */
    public Dimension imageSize = new Dimension(640,480);

    /**
     * Default constructor
     */
    public Camera() {
    	// do nothing
    }
    
    public Vector3d u = new Vector3d(1.0,0,0); 
    public Vector3d v = new Vector3d(0,1.0,0); 
    public Vector3d w = new Vector3d(0,0,1.0);
    
    public double top, bottom, left, right, distance, aspectRatio = 0.0;  
    
    private double angleRatio =  Math.PI/180;
    public void updateTransform() {
        
        // w is negative the look at direction 
        w.x = from.x - to.x ; 
        w.y = from.y - to.y; 
        w.z = from.z - to.z;            
        w.normalize();
        
        u.cross(up, w);
        u.normalize();        
        v.cross(u, w);       
        
         
        
        distance = to.distance(from);
        aspectRatio = imageSize.getWidth()/imageSize.getHeight();        

        // this gives us the distance to the top 
        top = Math.tan(angleRatio*fovy/2.0) ; 
        
        // this is just negative of the top 
        bottom= -top; 
        
        // width/height ratio should be preserved 
        right= aspectRatio*top; 
        left= -right; 
} 
}

