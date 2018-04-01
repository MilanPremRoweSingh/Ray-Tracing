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
    public void intersect( Ray ray, IntersectResult result ) {
    	if( ray == null )
    		return;
    	
    	if( result == null )
    		result = new IntersectResult();
    	
    	//t = (-p . n) / (rayDir . n)
    	double rayDirDotN 	= Sphere.localDot( n, ray.viewDirection );
    	double pDotN		= -1.0*Sphere.localDot( ray.eyePoint, n);
    	double 	t = pDotN / rayDirDotN;
    			t = ( t > threshErr ) ? t :  Double.POSITIVE_INFINITY;
    	result.t = t;
    	result.n = n;

		result.p = new Point3d( ray.eyePoint.x + ray.viewDirection.x * t,
								ray.eyePoint.y + ray.viewDirection.y * t, 
								ray.eyePoint.z + ray.viewDirection.z * t ); 
		
		if ( ( Math.ceil( result.p.x ) + Math.ceil( result.p.z ) ) % 2 == 0 && this.material2!= null  )
			result.material = this.material2; 
		else
			result.material = this.material;
    	
    }
    
}
