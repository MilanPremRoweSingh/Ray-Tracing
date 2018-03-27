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
    	if( ray == null )
    		return;
    	
    	if( result == null )
    		result = new IntersectResult();

    	//Intesects if point p exists such that:
    	//	1) point p is on ray: p = eye + t*rayDir for some t
    	//	2) point p is on sphere: ||p||^2 - r = 0
    	//  Solve for t in dot( ( eye + t*rayDir ), ( eye + t*rayDir ) ) = r -> Quadratic Equation
    	
    	//TEST SPhere at origin
    	Vector3d	d = ray.viewDirection;
    	Point3d		p = ray.eyePoint; // TODO: - sphere centre
    	
    	double pMag = localDot( p, p );	
    	double dMag = localDot( d, d );	
    	double dp 	= localDot( d, p );	
    	
    	double sqrtTerm = dp*dp - dMag*( pMag -  radius );
    	if( sqrtTerm >= 0 )
    	{
    		double t_plus 	= ( -dp + Math.sqrt( sqrtTerm ) ) / dMag; 
    		double t_minus 	= ( -dp - Math.sqrt( sqrtTerm ) ) / dMag; 
    		
    		double t = ( t_minus > thresholdErr ) ? t_minus : ( t_plus > thresholdErr ) ? t_plus : Double.POSITIVE_INFINITY;
    		result.t = t;
    		result.n = new Vector3d( p.x + d.x*t, p.y + d.y*t, p.z + d.z*t );//TODO: adjust for non centered spheres
    		result.p = new Point3d( p.x + d.x*t, p.y + d.y*t, p.z + d.z*t ); //TODO: adjust for non centered spheres
    	}
    	
    }
    
    public static double localDot( Tuple3d a, Tuple3d b )
    {
    	return a.x*b.x + a.y*b.y + a.z*b.z;
    }
    
}
