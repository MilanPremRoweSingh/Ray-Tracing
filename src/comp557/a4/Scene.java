package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();

    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        
        render.init(w, h, showPanel);
        
        for ( int i = 0; i < h && !render.isDone(); i++ ) {
            for ( int j = 0; j < w && !render.isDone(); j++ ) {
            	
                // TODO: Objective 1: generate a ray (use the generateRay method)
            	Ray ray = new Ray();
            	generateRay(i, j, new double[] { 0.0, 0.0 }, cam, ray);
                // TODO: Objective 2: test for intersection with scene surfaces
            	
                // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
                
            	// Here is an example of how to calculate the pixel value.

        		final int imgWidth 			= cam.imageSize.width;
        		final int imgHeight 		= cam.imageSize.height;
        		final double aspectRatio 	= (double) imgWidth / (double) imgHeight;
        		
        		Point3d e = cam.from;
        		Vector3d w0 	= new Vector3d( e.x - cam.to.x, e.y - cam.to.y, e.z - cam.to.z ); //w points away from lookat point at eye point
        		double d 	= w0.length();
        		double fovy = Math.toRadians( 45 ); //TEST
        		double tanTerm 			= Math.tan( fovy / 2.0 );
        		double viewPlaneHeight 	= d / ( 2 * tanTerm );
        		double viewPlaneWidth 	= viewPlaneHeight * aspectRatio;

            	double rayX = 2.0 * ray.viewDirection.x / viewPlaneWidth;
            	double rayY = 2.0 * ray.viewDirection.y / viewPlaneHeight;
            	
            	Color3f c = new Color3f(render.bgcolor);
            	int r = (int)(255*( 0.0 + rayX + 1.0 )/2.0);
                int g = (int)(255*( 0.0 + rayY + 1.0 )/2.0);
                int b = (int)(255*0);
                int a = 255;
                int argb = (a<<24 | r<<16 | g<<8 | b);    
                
                // update the render image
                render.setPixel(j, i, argb);
            }
        }
        
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();
        
    }
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {

		final int imgWidth 			= cam.imageSize.width;
		final int imgHeight 		= cam.imageSize.height;
		final double aspectRatio 	= (double) imgWidth / (double) imgHeight;
		
		//Point3d e = new Point3d( 0, 0, -10 ); //TEST
		//Vector3d u = new Vector3d( 1, 0, 0 ); // TEST 
		//Vector3d v = new Vector3d( 0, 1, 0 ); // TEST 
		//Vector3d w = new Vector3d( 0, 0, 1 ); // TEST 
		//double d = 10; //TEST
		//double fovy = Math.toRadians( 45 ); //Test
		
		Point3d e = cam.from;
		
		Vector3d w 	= new Vector3d( e.x - cam.to.x, e.y - cam.to.y, e.z - cam.to.z ); //w points away from lookat point at eye point
		double d 	= w.length();
		w.normalize(); //Normalize as it is a frame axis
		
		Vector3d v = cam.up;
		Vector3d u = new Vector3d();
		u.cross( v, w );
		u.normalize();
		
		double fovy = Math.toRadians( cam.fovy ); 
		
		double tanTerm 			= Math.tan( fovy / 2.0 );
		double viewPlaneHeight 	= d / ( 2 * tanTerm );
		double viewPlaneWidth 	= viewPlaneHeight * aspectRatio;
		
		double scalar_u = ( ( (float)j / (float)imgWidth - 0.5f ) * viewPlaneWidth ); //TODO offset
		double scalar_v = ( ( (float)( imgHeight - i ) / (float)imgHeight - 0.5f ) * viewPlaneHeight ); //TODO offset
		
		Vector3d rayDir = new Vector3d();
		Vector3d temp	= new Vector3d();
		
		temp.set( u );
		temp.scale( scalar_u );
		rayDir.add( temp );

		temp.set( v );
		temp.scale( scalar_v );
		rayDir.add( temp );
		
		temp.set( w );
		temp.scale( -d );
		rayDir.add( temp );
		
		ray.set( e, rayDir );
	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: check for shdows and use it in your lighting computation
		
		return false;
	}    
}
