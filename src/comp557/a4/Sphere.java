package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;


/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	private final double thresholdErr = 1e-4;
	
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
    	double t = Double.POSITIVE_INFINITY;
    	
        Vector3d p = new Vector3d( ray.eyePoint );
        p.sub( center );
        
        Vector3d d = new Vector3d( ray.viewDirection );
        
        double dotDD = localDot( d, d );
        double dotDP = localDot( d, p );
        double dotPP = localDot( p, p );
        
        double sqrtTerm = dotDP * dotDP - dotDD * ( dotPP - radius*radius );
                
                       
        if( sqrtTerm < 0.0 ) return;    
        
        sqrtTerm = Math.sqrt(sqrtTerm);
        
        if( sqrtTerm > 0 )
        {
            t = -1* dotDP;            
            double t_minus = (t - sqrtTerm )/( dotDD );
            double t_plus = (t + sqrtTerm )/( dotDD );
          
            t = ( t_minus > 0 ) ? t_minus : ( t_plus > 0 ) ? t_plus : Double.POSITIVE_INFINITY;
        }
        // if there is only one find the one that hits 
        else t  = -1 * dotDP / dotDD;

        if(!(t > 0.000001) || result.t < t) return;
        // set the result in 
        result.t = t; 
        result.material = this.material;
        result.brdf = brdf;
        ray.getPoint(t, result.p);
        result.n.set((result.p.x-center.x), (result.p.y-center.y), (result.p.z-center.z));           
        result.n.normalize();    
    }
    
    public static double localDot( Tuple3d a, Tuple3d b )
    {
    	return a.x*b.x + a.y*b.y + a.z*b.z;
    }
    
}
