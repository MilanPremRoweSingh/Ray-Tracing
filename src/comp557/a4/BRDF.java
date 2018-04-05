package comp557.a4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.print.DocFlavor.BYTE_ARRAY;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class BRDF 
{
	private static final int BRDF_SAMPLING_RES_THETA_H 	= 90;
	private static final int BRDF_SAMPLING_RES_THETA_D 	= 90;
	private static final int BRDF_SAMPLING_RES_PHI_D 	= 360;
	
	private static final double RED_SCALE 	= (1.0/1500.0);
	private static final double GREEN_SCALE	= (1.15/1500.0);
	private static final double BLUE_SCALE	= (1.66/1500.0);
	
	
	private static String dir = "brdfs/";
	double[] data;
	
	BRDF(String filename )
	{
		String strPath = dir + filename;
		
		Path path = Paths.get( strPath );
		byte[] byteData;
		try {
			 byteData = Files.readAllBytes( path );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	    
		
		int times = Double.SIZE / Byte.SIZE;
		int head = 3*Integer.SIZE / Byte.SIZE;
		byte[] intBuff = new byte[4];
		intBuff[0] = byteData[3];
		intBuff[1] = byteData[2];
		intBuff[2] = byteData[1];
		intBuff[3] = byteData[0];
		System.out.println( ByteBuffer.wrap(intBuff, 0, 4).getInt() );
		data = new double[90*90*180*3];

		byte entryData[] = new byte[8];
		for(int i=0;i<data.length;i++)
		{
			for( int j = 0; j < 8; j++ )
				entryData[j] = byteData[ head + (i+1)*8 - j - 1 ];
			
			double entry = ByteBuffer.wrap(entryData, 0, 8).getDouble(); 
			data[ i ] = entry; 
		}
	}
	
	public Color3f lookupVal( Vector3d wi, Vector3d wo, Vector3d n )
	{
		if ( data == null )
			return null;
		
		Color3f color = new Color3f();
		Vector3d loc_wi = new Vector3d( wi );
		loc_wi.normalize();
		
		Vector3d loc_wo = new Vector3d( wo );
		loc_wo.normalize();
		
		Vector3d loc_n = new Vector3d( n );
		loc_n.normalize();
		
		if( loc_n.dot( loc_wo ) < 0.25 ||  loc_n.dot( loc_wi ) < 0.25 ) //Eliminate grazing angles
			return color;
		
		Vector3d half = new Vector3d( loc_wi );
		half.add( loc_wo );
		half.normalize();

		//Matrix3d rMat = calculateRotationMatrixToAlign( loc_n, new Vector3d( new double[]{ 0,0,1 } ) );
		
		//apply3x3MatToVec( rMat, loc_n );
		//apply3x3MatToVec( rMat, loc_wi );
		//apply3x3MatToVec( rMat, loc_wo );
		
		Vector3d bi 	= new Vector3d( new double[]{ 0.0, 1.0, 0.0 } );
		Vector3d tan 	= new Vector3d( new double[]{ 1.0, 0.0, 0.0 } );
		
		double thetaH 	= Math.acos( saturate( loc_n.dot( half ) ) );
		double thetaD	= Math.acos( saturate( loc_n.dot( loc_wi ) ) );;
		double phiD 	= 0.0;
		
		if( thetaD < 1e-3 )
			phiD = Math.atan2( clamp( -loc_wi.dot( bi ), -1.0, 1.0 ), clamp( loc_wi.dot( tan ), -1.0, 1.0 ));
		else if ( thetaH > 1e-3 )
		{
			Vector3d u = new Vector3d( half );
			u.scale( - n.dot( half ) );
			u.add( loc_n);
			
			Vector3d v = new Vector3d();
			v.cross( half, u );
			
			phiD = Math.atan2( clamp( loc_wi.dot(v), -1, 1), clamp( loc_wi.dot(u), -1, 1));
		}
		else
			thetaH = 0;
		
		int ind = 	getPhiDIndex( phiD ) +
				 	getThetaDIndex( thetaD ) * BRDF_SAMPLING_RES_PHI_D / 2 +
				 	getThetaHIndex( thetaH ) * BRDF_SAMPLING_RES_PHI_D / 2 * BRDF_SAMPLING_RES_THETA_D;
		 
		color.x = (float)( data[ind] * RED_SCALE );
		color.y = (float)( data[ind + BRDF_SAMPLING_RES_THETA_H*BRDF_SAMPLING_RES_THETA_D*BRDF_SAMPLING_RES_PHI_D/2] *GREEN_SCALE );
		color.z = (float)( data[ind + BRDF_SAMPLING_RES_THETA_H*BRDF_SAMPLING_RES_THETA_D*BRDF_SAMPLING_RES_PHI_D] *BLUE_SCALE );

		color.x = (float)saturate( color.x* loc_n.dot( loc_wi ) );
		color.y = (float)saturate( color.y* loc_n.dot( loc_wi ) );
		color.z = (float)saturate( color.z* loc_n.dot( loc_wi ) );
		
		System.out.println( color.x );
		System.out.println( color.y );
		System.out.println( color.z );
		
		return color;
	}

	

	public Color3f lookupValMatRotation( Vector3d wi, Vector3d wo, Vector3d n )
	{
		if ( data == null )
			return null;
		
		Color3f color = new Color3f();
		Vector3d loc_wi = new Vector3d( wi );
		loc_wi.normalize();
		loc_wi.x = 0;
		loc_wi.y = 0;
		loc_wi.z = 1;

		Vector3d loc_wo = new Vector3d( wo );
		loc_wo.normalize();
		loc_wo.x = 0;
		loc_wo.y = 0;
		loc_wo.z = 1;
		
		Vector3d loc_n = new Vector3d( n );
		loc_n.normalize();
		loc_n.x = 0;
		loc_n.y = 0;
		loc_n.z = 1;
		
		Vector3d half = new Vector3d( loc_wi );
		half.add( loc_wo );
		half.normalize();
		

		double thetaH 	= Math.acos( half.z );
		double phiH		= Math.atan2( half.y, half.x );
		
		
		Matrix3d rMat = calculateRotationMatrixToAlign( loc_n, new Vector3d( new double[]{ 0,0,1 } ) );
		
		apply3x3MatToVec( rMat, loc_n );
		apply3x3MatToVec( rMat, loc_wi );
		apply3x3MatToVec( rMat, loc_wo );
		
		Vector3d bi = new Vector3d( new double[]{ 0.0, 1.0, 0.0 } );
		
		Vector3d temp = calculateRotatedVector( loc_wi, loc_n, -phiH );
		Vector3d diff = calculateRotatedVector( temp, bi, -thetaH );
		
		double thetaD 	= Math.acos( diff.z );
		double phiD 	= Math.atan2( diff.y, diff.x );
		
		int ind = 	getPhiDIndex( phiD ) +
				 	getThetaDIndex( thetaD ) * BRDF_SAMPLING_RES_PHI_D / 2 +
				 	getThetaHIndex( thetaH ) * BRDF_SAMPLING_RES_PHI_D / 2 * BRDF_SAMPLING_RES_THETA_D;
		System.out.println( ind );
		
		color.x = (float)( data[ind] * RED_SCALE );
		color.y = (float)( data[ind + BRDF_SAMPLING_RES_THETA_H*BRDF_SAMPLING_RES_THETA_D*BRDF_SAMPLING_RES_PHI_D/2] *GREEN_SCALE );
		color.z = (float)( data[ind + BRDF_SAMPLING_RES_THETA_H*BRDF_SAMPLING_RES_THETA_D*BRDF_SAMPLING_RES_PHI_D] *BLUE_SCALE );

		
		return color;
	}

	private static void apply3x3MatToVec( Matrix3d mat, Vector3d vec )
	{
		Vector3d loc_vec = new Vector3d( vec );
		
		vec.x = mat.m00*loc_vec.x + mat.m01*loc_vec.y + mat.m02*loc_vec.z;
		vec.y = mat.m10*loc_vec.x + mat.m11*loc_vec.y + mat.m12*loc_vec.z;
		vec.z = mat.m20*loc_vec.x + mat.m21*loc_vec.y + mat.m22*loc_vec.z;
		
		vec = loc_vec;
	}
	
	private static double clamp( double val, double low, double high )
	{
		if ( val < low )
			return low;
		if ( val > high )
			return high;
		return val;
	}
	
	private static double saturate( double val )
	{
		return clamp( val, 0.0, 1.0 );
	}
	
	private static Matrix3d calculateRotationMatrixToAlign( Vector3d a, Vector3d b )
	{
		Matrix3d rMat = new Matrix3d();
		
		Vector3d loc_a = new Vector3d();
		loc_a.normalize( a );

		Vector3d loc_b = new Vector3d();
		loc_b.normalize( b );
		
		Vector3d v = new Vector3d();
		v.cross( loc_a, loc_b );
		
		double cos = a.dot( b );
		if( cos == -1.0 )
			return null;

		cos = 1.0 / (1.0 + cos);
		//v.normalize();
		
		Matrix3d skewSymmMat = new Matrix3d();
		skewSymmMat.m00 = 0;
		skewSymmMat.m01 = 0 - v.z;
		skewSymmMat.m02 = 0 + v.y;

		skewSymmMat.m10 = 0 + v.z;
		skewSymmMat.m11 = 0;
		skewSymmMat.m12 = 0 - v.x;

		skewSymmMat.m20 = 0 - v.y;
		skewSymmMat.m21 = 0 + v.x;
		skewSymmMat.m22 = 0;
		
		Matrix3d skewSymmSqrMat = new Matrix3d( skewSymmMat );
		skewSymmSqrMat.mul( skewSymmMat );
		skewSymmSqrMat.mul( cos );
		
		rMat.setIdentity();
		rMat.add( skewSymmMat );
		rMat.add( skewSymmSqrMat );
		
		return rMat;
	}
					
	private static Vector3d calculateRotatedVector( Vector3d vecToRotate, Vector3d axis, double angle )
	{
		Vector3d rotated = new Vector3d();
		
		double temp;
		double cosAngle = Math.cos( angle );
		double sinAngle = Math.sin( angle );
		
		rotated.x = vecToRotate.x * cosAngle;
		rotated.y = vecToRotate.y * cosAngle;
		rotated.z = vecToRotate.z * cosAngle;
		
		temp = vecToRotate.dot( axis );
		temp = temp*( 1.0 - cosAngle );
		
		rotated.x += axis.x * temp;
		rotated.y += axis.y * temp;
		rotated.z += axis.z * temp;
		
		Vector3d crossTemp = new Vector3d();
		crossTemp.cross( axis, vecToRotate );

		rotated.x += crossTemp.x * sinAngle;
		rotated.y += crossTemp.y * sinAngle;
		rotated.z += crossTemp.z * sinAngle;
		
		return rotated;
	}

	private static int getThetaHIndex( double thetaH )
	{
		if( thetaH <= 0.0 )
			return 0;
		
		double thetaHDeg = ( thetaH / (Math.PI/2.0) )*BRDF_SAMPLING_RES_THETA_H;
		
		double temp = thetaHDeg * BRDF_SAMPLING_RES_THETA_H;
		temp = Math.sqrt( temp );
		
		int retVal = (int)temp;
		retVal = ( retVal < 0 ) ? 0 : retVal;
		retVal = ( retVal >= BRDF_SAMPLING_RES_THETA_H ) ? BRDF_SAMPLING_RES_THETA_H - 1 : retVal;
		
		return retVal;
	}
	
	private static int getThetaDIndex( double thetaD )
	{
		int temp = (int)( thetaD / ( Math.PI * 0.5 ) * BRDF_SAMPLING_RES_THETA_D );
		if ( temp < 0 )
			return 0;
		else if( temp < BRDF_SAMPLING_RES_THETA_D - 1 )
			return temp;
		else
			return BRDF_SAMPLING_RES_THETA_D - 1;
	}
	
	private static int getPhiDIndex( double phiD )
	{
		if( phiD < 0.0 )
			phiD += Math.PI;
		
		int temp = (int)( phiD / ( Math.PI ) * BRDF_SAMPLING_RES_PHI_D / 2 );
		if ( temp < 0 )
			return 0;
		else if( temp < BRDF_SAMPLING_RES_PHI_D / 2 - 1 )
			return temp;
		else
			return BRDF_SAMPLING_RES_THETA_D / 2 - 1;
	}
}












