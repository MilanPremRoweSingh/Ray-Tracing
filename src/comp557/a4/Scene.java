package comp557.a4;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
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
        
        if( true )
        {
	        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
	        
	        for ( int i = 0; i < h && !render.isDone(); i++ ) {
	            for ( int j = 0; j < w && !render.isDone(); j++ ) {
	            	final int idxX = j;
	            	final int idxY = i;
	            	executor.submit( ()-> { drawPixel( idxY, idxX,render); } );
	            }
	        }
	        
	        try 
	        {
	        	while( executor.awaitTermination( 1, TimeUnit.SECONDS ) );
	        }
	        catch( InterruptedException e )
	        {
	        	e.printStackTrace();
	        }
        }
        else
        {
	        for ( int i = 0; i < h && !render.isDone(); i++ ) {
	            for ( int j = 0; j < w && !render.isDone(); j++ ) {
	            	final int idxX = j;
	            	final int idxY = i;
	            	drawPixel( idxY, idxX,render);
	            }
	        }
        }
        
        
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();        
    }
    
    public void drawPixel( int i, int j, Render render )
    {
    	Camera cam = render.camera;
    	Color3f c;
    	if( render.jitter )
    	{
    		int samplesPerDim = (int) Math.ceil( Math.sqrt( render.samples ) );
    		Random rand = new Random();
    		c = new Color3f();
    		for( int y = 0; y < samplesPerDim; y++ )
    		{
    			for( int x = 0; x < samplesPerDim; x++ )
    			{
    				double rx = -0.5 +  ( x + rand.nextDouble() )/samplesPerDim;
    				double ry = -0.5 +  ( y + rand.nextDouble() )/samplesPerDim;

	            	Ray ray = new Ray();

	            	generateRay(i, j, new double[] { rx, ry }, cam, ray);
	                c.add( getPixelColour( render, ray ) );
    			}
    		}
    		c.scale( 1.0f/(float)(samplesPerDim*samplesPerDim) );
    	}
    	else
    	{
        	Ray ray = new Ray();
        	generateRay(i, j, new double[] { 0.0, 0.0 }, cam, ray);

        	
            c = getPixelColour( render, ray );
    	}
		
    	int r = (int)( 255*( c.x ) );
        int g = (int)( 255*( c.y ) );
        int b = (int)( 255*( c.z ) );
    	
        // update the render image
        int a = 255;
        int argb = (a<<24 | r<<16 | g<<8 | b);   
        render.setPixel(j, i, argb);
    	
    }
    
    public Color3f getPixelColour( Render render, Ray ray )
    {

    	IntersectResult result = null;
        double t = Double.POSITIVE_INFINITY;

    	for( Intersectable curr : surfaceList )
    	{
    		IntersectResult temp = new IntersectResult();
    		curr.intersect( ray, temp );
    		
    		if ( temp.t < t ) 
    		{
    			t = temp.t;
    			result = temp;
    		}
    	}
    	// Here is an example of how to calculate the pixel value.


		Vector3d rayDir = new Vector3d();
		rayDir.normalize(ray.viewDirection);
		
		
		double lightingR = 0.0;
		double lightingG = 0.0;
		double lightingB = 0.0;
		
		if( result != null )
		{
    		for( Light light : lights.values() )
    		{
    			Vector3d shadowRayDir = new Vector3d();
    			shadowRayDir.sub( result.p );
    			shadowRayDir.add( light.from );
    			
    			Point3d origin = new Point3d();
    			origin.add(shadowRayDir);
    			origin.scale( 1e-4 );
    			origin.add( result.p );
    			Ray shadowRay = new Ray( origin, shadowRayDir );                

    			boolean shadowed = false;
    			
                t = Double.POSITIVE_INFINITY;
    			for( Intersectable curr : surfaceList )
            	{
            		IntersectResult temp = new IntersectResult();
            		curr.intersect( shadowRay, temp );
            		
            		if ( temp.t < t ) 
            		{
            			t = temp.t;
            		}
            	}
    			
    			if( t < Double.POSITIVE_INFINITY ) 
    			{
    				Vector3d originToIntersectable 	= new Vector3d();
    				originToIntersectable.add( shadowRayDir );
    				originToIntersectable.scale( t );
    				
    				shadowed = ( originToIntersectable.lengthSquared() < shadowRayDir.lengthSquared() ); //Use lengthSquared as its cheaper
    			}
    			
    			if( !shadowed )
    			{
    				Vector3d v = new Vector3d( ray.viewDirection );
    				v.scale( -1.0 );
    				v.normalize();
    				
    				Vector3d l = new Vector3d( shadowRayDir );
    				l.normalize();
    				
    				Vector3d half = new Vector3d();
    				half.add( l );
    				half.add( v );
    				half.normalize();
    				
    				
    				Vector3d n = new Vector3d();
    				n.normalize( result.n);
    				double diffuseIntensity = Math.max( 0,Sphere.localDot( n, l ) ); 
    				double specularIntensity = Math.pow( Math.max( 0,Sphere.localDot( n, half ) ), result.material.shinyness ); 
    				//double intensity = localDot( result.n, );
            		Color4f diffuseColour 	= result.material.diffuse;//new Color3f( 1,1,1 );
            		Color4f specularColour 	= result.material.specular;//new Color3f( 1,1,1 );
                	lightingR += light.power * light.color.x * ( diffuseColour.x*diffuseIntensity + specularColour.x*specularIntensity );
                    lightingG += light.power * light.color.y * ( diffuseColour.y*diffuseIntensity + specularColour.y*specularIntensity );
                    lightingB += light.power * light.color.z * ( diffuseColour.z*diffuseIntensity + specularColour.z*specularIntensity );
    			}
    			
            	lightingR += light.power * light.color.x * ( result.material.diffuse.x * ambient.x);
                lightingG += light.power * light.color.y * ( result.material.diffuse.y * ambient.y);
                lightingB += light.power * light.color.z * ( result.material.diffuse.z * ambient.z);
    		}
		}
		else
		{
			lightingR = render.bgcolor.x;
			lightingG = render.bgcolor.y;
			lightingB = render.bgcolor.z;
		}
		
		lightingR = Math.max( 0.0, Math.min( lightingR, 1.0 ) );
		lightingG = Math.max( 0.0, Math.min( lightingG, 1.0 ) );
		lightingB = Math.max( 0.0, Math.min( lightingB, 1.0 ) );

		return new Color3f( new float[]{ (float)lightingR, (float)lightingG, (float)lightingB } );
		
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
		
		Point3d e = cam.from;
		
		Vector3d w 	= new Vector3d( e.x - cam.to.x, e.y - cam.to.y, e.z - cam.to.z ); //w points away from lookat point at eye point
		w.normalize(); //Normalize as it is a frame axis
		
		Vector3d v = new Vector3d( cam.up );
		v.normalize();
		Vector3d u = new Vector3d();
		u.cross( v, w );
		u.normalize();
		v.cross( w, u );
		
		double fovy = Math.toRadians( cam.fovy ); 
		
		double d 	= e.distance( cam.to );
		double tanTerm 			= Math.tan( fovy / 2.0 );
		double viewPlaneHeight 	= d / ( 3 * tanTerm );
		double viewPlaneWidth 	= viewPlaneHeight * aspectRatio;
		
		double scalar_u = ( ( (float)(j + offset[1] + 0.5f) / (float)imgWidth - 0.5f ) * viewPlaneWidth ); 
		double scalar_v = ( ( (float)( imgHeight - i + offset[0] + 0.5f ) / (float)imgHeight - 0.5f ) * viewPlaneHeight ); 
		
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
	 
}
