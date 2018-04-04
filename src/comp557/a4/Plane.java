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

        
    Vector3d helper = new Vector3d();
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: finish this class   
        // zero is on the plane therefore we only need to set the point as the vector 
        helper.set(ray.eyePoint);
        helper.scale(-1);
        
        
        double t = helper.dot(n)/ray.viewDirection.dot(n);
        
        if(!(t > 0.000001) || result.t < t) return;
        
        result.t =t;
        result.n.set(n); 
        //result.p.set(ray.eyePoint.x + result.t * ray.viewDirection.x, ray.eyePoint.y+ result.t * ray.viewDirection.y, ray.eyePoint.z + result.t * ray.viewDirection.z);
        ray.getPoint(t, result.p);
        
        // if only material one is available
        if(this.material2 == null && this.material != null) { result.material = this.material; }
        
        // if only material2 is available 
        else if(this.material == null && this.material2!= null) { result.material = this.material2;}
        
        // if we are in the top right or bottom left quadrants 
        else if( (result.p.x > 0 && result.p.z>0 ) || result.p.x < 0 && result.p.z < 0 ) {
            if ((((int)result.p.x) % 2 == 0 && ((int)result.p.z) % 2 == 0) || 
                    (((int)result.p.x) % 2 != 0 && ((int)result.p.z) % 2 != 0)) {
                result.material = this.material;
            }
            else {
                result.material = this.material2;
            }            
        }  
        
        // if we are in the top left or bottom right quadrants 
        else {
            if ((((int)result.p.x) % 2 == 0 && ((int)result.p.z) % 2 == 0) || 
                    (((int)result.p.x) % 2 != 0 && ((int)result.p.z) % 2 != 0)) {
                result.material = this.material2;
            }
            else {
                result.material = this.material;
            }   
        }
}
    
}
