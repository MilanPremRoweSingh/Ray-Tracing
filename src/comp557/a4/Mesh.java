package comp557.a4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import comp557.a4.PolygonSoup.Vertex;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
	
	private final static double EPSILON = 1e-6;
		
	@Override
	public void intersect(Ray ray, IntersectResult result) 
	{
		System.out.println("");
		// TODO: Objective 7: ray triangle intersection for meshes
		List<int[]> faces = soup.faceList;
		List<Vertex> verts = soup.vertexList;
		
		double t = Double.POSITIVE_INFINITY;
		for( int faceIdx = 0; faceIdx < faces.size(); faceIdx++ )
		{
			
			int[] face = faces.get( faceIdx );
			
			if( face.length != 3 )
			{
				System.out.println( "Non triangle mesh" );
				return;
			}
			
			Vertex[] faceVerts = new Vertex[ face.length ];
			for( int i = 0; i < face.length; i++ )
			{
				faceVerts[ i ] = verts.get( face[ i ] );
			}
			
			// use Möller-Trumbore algorithm to detect intersection
			Vector3d e0 = new Vector3d( faceVerts[2].p );
			e0.sub( faceVerts[0].p );
			
			Vector3d e1 = new Vector3d( faceVerts[1].p );
			e1.sub( faceVerts[0].p );
			
			Vector3d h = new Vector3d();
			h.cross( ray.viewDirection, e1 );
			
			double a = e0.dot( h );
			if( a > -EPSILON && a < EPSILON ) //If h is not perpendicular to e0 i.e. lies on another plane
				continue;
			
			double f = 1/a;
			
			Vector3d s = new Vector3d( ray.eyePoint );
			s.sub( faceVerts[ 0 ].p );
			
			double u = f * s.dot( h );
			if( u < 0.0 || u > 1.0 )
				continue;
			
			Vector3d q = new Vector3d();
			q.cross( s, e0 );
			
			double v = f * ray.viewDirection.dot( q );
			if( v < 0.0 || u + v > 1.0 )
				continue;
			
			t = f * e1.dot( q );
			if( t > EPSILON && t < result.t )
			{
				result.t = t;
				ray.getPoint( t, result.p );
				result.material = material;
				result.n.cross( e0, e1 );
			}
			else
				continue;
		}
	}
	
}
