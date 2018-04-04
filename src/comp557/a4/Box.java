package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) 
	{
		Vector3d rayDir = new Vector3d( ray.viewDirection );
		
		double tMin;
		double tMax;
		double txMin;
		double txMax;
		
		if( ray.viewDirection.x >= 0 )
		{
			txMin = ( min.x - ray.eyePoint.x ) / rayDir.x;
			txMax = ( max.x - ray.eyePoint.x ) / rayDir.x;
		}
		else
		{
			txMin = ( max.x - ray.eyePoint.x ) / rayDir.x;
			txMax = ( min.x - ray.eyePoint.x ) / rayDir.x;
		}
		
		tMin = txMin;
		tMax = txMax;
		double tyMin;
		double tyMax;
		
		if( ray.viewDirection.y >= 0 )
		{
			tyMin = ( min.y - ray.eyePoint.y ) / rayDir.y;
			tyMax = ( max.y - ray.eyePoint.y ) / rayDir.y;
		}
		else
		{
			tyMin = ( max.y - ray.eyePoint.y ) / rayDir.y;
			tyMax = ( min.y - ray.eyePoint.y ) / rayDir.y;
		}
		
		if (( tMin > tyMax ) || (tyMin > tMax)) return;        
        if (tyMin > tMin) { tMin = tyMin; }        
        if (tyMax < tMax) { tMax = tyMax; }

		double tzMin;
		double tzMax;
		
		if( ray.viewDirection.z >= 0 )
		{
			tzMin = ( min.z - ray.eyePoint.z ) / rayDir.z;
			tzMax = ( max.z - ray.eyePoint.z ) / rayDir.z;
		}
		else
		{
			tzMin = ( max.z - ray.eyePoint.z ) / rayDir.z;
			tzMax = ( min.z - ray.eyePoint.z ) / rayDir.z;
		}
		
		if ((tMin > tzMax) || (tzMin > tMax))
			return;        
		
        tMin = Math.max(tzMin, Math.max(txMin, tyMin));
        tMax = Math.min(tzMax, Math.max(txMax, tyMax));
		
		if ( result.t < tMin || tMin < 1e-6 )
			return;
		
		result.t = tMin;
		result.material = material;
		ray.getPoint( tMin, result.p );
		result.n.set( getNormal( result.p ) );
	}	

	
	private Vector3d normal = new Vector3d();
	private Vector3d getNormal(Point3d point) {
	    normal.set(0,0,0);
        if(Math.abs(point.x - min.x) < 1e-9) normal.set( -1, 0, 0); 
        else if (Math.abs(point.x - max.x) < 1e-9) normal.set(1, 0, 0); 
        else if(Math.abs(point.y - min.y) < 1e-9 ) normal.set(0, -1, 0); 
        else if(Math.abs(point.y - max.y) < 1e-9 ) normal.set(0, 1, 0); 
        else if(Math.abs(point.z - min.z) < 1e-9 ) normal.set(0, 0, -1); 
        else if(Math.abs(point.z - max.z) < 1e-9 ) normal.set(0, 0, 1); 	    
	    return normal; 
}
}
