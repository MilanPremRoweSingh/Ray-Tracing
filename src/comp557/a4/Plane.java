package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
	
	private static final double threshErr = 1e-4;
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

    @Override
    public void intersect( Ray ray, IntersectResult result ) 
    {
    
    	Vector3d invEye = new Vector3d( ray.eyePoint );
    	invEye.scale( -1.0 );
        
    	n.normalize();
        double t = invEye.dot( n )/ray.viewDirection.dot( n );
        
        if(!( t > 1e-6 ) || result.t < t) 
        	return;
        
        result.t =t;
        result.n.set( n ); 
        ray.getPoint(t, result.p);
        
        // if only material one is available
        if(this.material2 == null && this.material != null) { result.material = this.material; }
        
        // if only material2 is available 
        else if(this.material == null && this.material2!= null) { result.material = this.material2;}
        
        // if we are in the top right or bottom left quadrants 
        else if( ( result.p.x > 0 && result.p.z > 0 ) || result.p.x < 0 && result.p.z < 0 ) 
        {
            if ( ( ( (int)result.p.x ) % 2 == 0 && ( (int)result.p.z ) % 2 == 0 ) || ( ( (int)result.p.x ) % 2 != 0 && ( (int)result.p.z ) % 2 != 0) ) 
            {
                result.material = this.material;
            }
            else 
            {
                result.material = this.material2;
            }            
        }  
        else 
        {
            if ((((int)result.p.x) % 2 == 0 && ((int)result.p.z) % 2 == 0) || (((int)result.p.x) % 2 != 0 && ((int)result.p.z) % 2 != 0)) 
            {
                result.material = this.material2;
            }
            else 
            {
                result.material = this.material;
            }   
        }
}
    
}
