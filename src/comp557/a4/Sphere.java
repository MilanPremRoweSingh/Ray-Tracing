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
    
private double dx, dy, dz, ex, ey, ez, cx, cy, cz,  a, b, c, discriminant, t, t0, t1 = 0.0;
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: finish this class
        
        // don't have to dereference each time 
        dx = ray.viewDirection.x; 
        dy = ray.viewDirection.y; 
        dz = ray.viewDirection.z;
        
        ex = ray.eyePoint.x; 
        ey = ray.eyePoint.y; 
        ez = ray.eyePoint.z;
        
        cx = center.x; 
        cy = center.y; 
        cz = center.z; 
        
        
                
        // check the discriminant  
        discriminant = (dx*(ex-cx) + dy*(ey-cy) + dz*(ez-cz))*(dx*(ex-cx) + dy*(ey-cy) + dz*(ez-cz)) - (dx*dx + dy*dy + dz*dz)*((ex-cx)*(ex-cx) + (ey-cy)*(ey-cy) + (ez-cz)*(ez-cz) -this.radius*this.radius);
        
        // there is no intersection then don't bother continuing                 
        if(discriminant < 0.0) return;    
        
        discriminant = Math.sqrt(discriminant);
        // if there are two interesections find the one that hits 
        if(discriminant > 0 ){
            t = -1*(dx*(ex-cx) + dy*(ey-cy) + dz*(ez-cz));             
            t0 = (t - discriminant)/(dx*dx + dy*dy + dz*dz);
            t1 = (t + discriminant)/(dx*dx + dy*dy + dz*dz);
          
          if(t0 < t1 && t0 > 0) t = t0; 
          else if (t1 < t0 && t1 >0 ) t = t1;
          else return;
            
        }
        // if there is only one find the one that hits 
        else t  =  -1*(dx*(ex-cx) + dy*(ey-cy) + dz*(ez-cz))/(dx*dx + dy*dy + dz*dz);

        if(!(t > 0.000001) || result.t < t) return;
        // set the result in 
        result.t = t; 
        result.material = this.material;
        ray.getPoint(t, result.p);
        result.n.set((result.p.x-cx)/radius, (result.p.y-cy)/radius, (result.p.z-cz)/radius);           
        result.n.normalize();
        
        
        
}
    public static double localDot( Tuple3d a, Tuple3d b )
    {
    	return a.x*b.x + a.y*b.y + a.z*b.z;
    }
    
}
