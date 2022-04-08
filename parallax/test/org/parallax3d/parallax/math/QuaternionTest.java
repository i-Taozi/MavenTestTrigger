/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution 
 * 3.0 Unported License. for more details.
 * 
 * You should have received a copy of the the Creative Commons Attribution 
 * 3.0 Unported License along with Parallax. 
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */

package org.parallax3d.parallax.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class QuaternionTest
{

	private static double DELTA = 0.0;

	private static double X = 2;
	private static double Y = 3;
	private static double Z = 4;
	private static double W = 5;

	private static Vector3 eulerAngles = new Vector3( 0.1, -0.3, 0.25 );
//	private static List<Euler> orders = Arrays.asList( Euler.XYZ, Euler.YXZ, Euler.ZXY, Euler.ZYX, Euler.YZX, Euler.XZY );

	@Test
	public void testQuaternion()
	{
		Quaternion a = new Quaternion();
		assertEquals( 0.0, a.x, DELTA );
		assertEquals( 0.0, a.y, DELTA );
		assertEquals( 0.0, a.z, DELTA );
		assertEquals( 1.0, a.w, DELTA );

		a = new Quaternion( X, Y, Z, W );
		assertEquals( X, a.x, DELTA );
		assertEquals( Y, a.y, DELTA );
		assertEquals( Z, a.z, DELTA );
		assertEquals( W, a.w, DELTA );
	}

	@Test
	public void testCopy()
	{
		Quaternion a = new Quaternion( X, Y, Z, W );
		Quaternion b = new Quaternion().copy( a );
		assertEquals( X, b.x, DELTA );
		assertEquals( Y, b.y, DELTA );
		assertEquals( Z, b.z, DELTA );
		assertEquals( W, b.w, DELTA );

		// ensure that it is a true copy
		a.x = 0;
		a.y = -1;
		a.z = 0;
		a.w = -1;
		assertEquals( X, b.x, DELTA );
		assertEquals( Y, b.y, DELTA );
	}

	@Test
	public void testSet()
	{
		Quaternion a = new Quaternion();
		assertEquals( 0.0, a.x, DELTA );
		assertEquals( 0.0, a.y, DELTA );
		assertEquals( 0.0, a.z, DELTA );
		assertEquals( 1.0, a.w, DELTA );

		a.set( X, Y, Z, W );
		assertEquals( X, a.x, DELTA );
		assertEquals( Y, a.y, DELTA );
		assertEquals( Z, a.z, DELTA );
		assertEquals( W, a.w, DELTA );
	}

//	@Test
//	public void testSetFromEulerSetEulerFromQuaternion()
//	{
//		List<Vector3> angles = Arrays.asList( new Vector3( 1, 0, 0 ), new Vector3( 0, 1, 0 ), new Vector3( 0, 0, 1 ) );
//
//		// ensure euler conversion to/from Quaternion matches.
//		for( int i = 0; i < orders.size(); i ++ )
//		{
//			for( int j = 0; j < angles.size(); j ++ )
//			{
//				Vector3 eulers2 = new Vector3().setEulerFromQuaternion( new Quaternion().setFromEuler( angles.get(j), orders.get(i) ), orders.get(i) );
//
//				assertTrue( eulers2.distanceTo( angles.get(j) ) < 0.001);
//			}
//		}
//
//	}
//
//  @Test
//	public void testSetFromEulerSetFromRotationMatrix()
//	{
//		// ensure euler conversion for Quaternion matches that of Matrix4
//		for( int i = 0; i < orders.size(); i ++ )
//		{
//			Quaternion q = new Quaternion().setFromEuler( eulerAngles, orders.get(i) );
//			Matrix4 m = new Matrix4().setRotationFromEuler( eulerAngles, orders.get(i) );
//			Quaternion q2 = new Quaternion().setFromRotationMatrix( m );
//
//			assertTrue( qSub( q, q2 ).length() < 0.001);
//		}
//	}

	@Test
	public void testSetFromAxisAngle()
	{
		// TODO: find cases to validate.
		assertTrue( true);

		Quaternion zero = new Quaternion();

		Quaternion a = new Quaternion().setFromAxisAngle( new Vector3( 1, 0, 0 ), 0 );
		assertTrue( a.equals( zero ));
		a = new Quaternion().setFromAxisAngle( new Vector3( 0, 1, 0 ), 0 );
		assertTrue( a.equals( zero ));
		a = new Quaternion().setFromAxisAngle( new Vector3( 0, 0, 1 ), 0 );
		assertTrue( a.equals( zero ));

		Quaternion b1 = new Quaternion().setFromAxisAngle( new Vector3( 1, 0, 0 ), (double)Math.PI );
		assertTrue( ! a.equals( b1 ));
		Quaternion b2 = new Quaternion().setFromAxisAngle( new Vector3( 1, 0, 0 ), (double)-Math.PI );
		assertTrue( ! a.equals( b2 ));

		b1.multiply( b2 );
		assertTrue( a.equals( b1 ));
	}

	@Test
	public void testNormalize()
	{
		Quaternion a = new Quaternion( X, Y, Z, W );
		Quaternion b = new Quaternion( -X, -Y, -Z, -W );

		assertTrue( a.length() != 1.0);
		assertTrue( a.lengthSq() != 1.0);
		a.normalize();
		assertEquals( 1.0, a.length(), DELTA );
		assertEquals( 1.0, a.lengthSq(), DELTA );

		a.set( 0, 0, 0, 0 );
		assertEquals( 0.0, a.lengthSq(), DELTA );
		assertEquals( 0.0, a.length(), DELTA );

		b.normalize();
		assertEquals( 1.0, b.lengthSq(), DELTA );
		assertEquals( 1.0, b.length(), DELTA );
	}

	@Test
	public void testInverse()
	{
		Quaternion a = new Quaternion( X, Y, Z, W );

		// TODO: add better validation here.

		Quaternion b = a.clone().conjugate();

		assertEquals( -b.x, a.x, DELTA );
		assertEquals( -b.y, a.y, DELTA );
		assertEquals( -b.z, a.z, DELTA );
		assertEquals( b.w, a.w, DELTA );
	}

//	@Test
//	public void testMultiplyQuaternion()
//	{
//		List<Vector3> angles = Arrays.asList( new Vector3( 1, 0, 0 ), new Vector3( 0, 1, 0 ), new Vector3( 0, 0, 1 ) );
//
//		Quaternion q1 = new Quaternion().setFromEuler( angles.get(0), Euler.XYZ );
//		Quaternion q2 = new Quaternion().setFromEuler( angles.get(1), Euler.XYZ );
//		Quaternion q3 = new Quaternion().setFromEuler( angles.get(2), Euler.XYZ );
//
//		Quaternion q = new Quaternion().multiply( q1, q2 ).multiply( q3 );
//
//		Matrix4 m1 = new Matrix4().setRotationFromEuler( angles.get(0), Euler.XYZ );
//		Matrix4 m2 = new Matrix4().setRotationFromEuler( angles.get(1), Euler.XYZ );
//		Matrix4 m3 = new Matrix4().setRotationFromEuler( angles.get(2), Euler.XYZ );
//
//		Matrix4 m = new Matrix4().multiply( m1, m2 ).multiply( m3 );
//
//		Quaternion qFromM = new Quaternion().setFromRotationMatrix( m );
//
//		assertTrue( qSub( q, qFromM ).length() < 0.001);
//	}
//
//	@Test
//	public void testMultiplyVector3Vector3()
//	{
//		List<Vector3> angles = Arrays.asList( new Vector3( 1, 0, 0 ), new Vector3( 0, 1, 0 ), new Vector3( 0, 0, 1 ) );
//
//		// ensure euler conversion for Quaternion matches that of Matrix4
//		for( int i = 0; i < orders.size(); i ++ )
//		{
//			for( int j = 0; j < angles.size(); j ++ )
//			{
//				Quaternion q = new Quaternion().setFromEuler( angles.get(j), orders.get(i) );
//				Matrix4 m = new Matrix4().setRotationFromEuler( angles.get(j), orders.get(i) );
//
//				Vector3 v0 = new Vector3(1, 0, 0);
//				Vector3 qv = v0.clone().apply( q );
//				Vector3 mv = v0.clone().apply( m );
//
//				assertTrue( qv.distanceTo( mv ) < 0.001);
//			}
//		}
//	}

	@Test
	public void testEquals()
	{
		Quaternion a = new Quaternion( X, Y, Z, W );
		Quaternion b = new Quaternion( -X, -Y, -Z, -W );
		
		assertTrue( a.x != b.x);
		assertTrue( a.y != b.y);

		assertTrue( ! a.equals( b ));
		assertTrue( ! b.equals( a ));

		a.copy( b );
		assertEquals( b.x, a.x, DELTA );
		assertEquals( b.y, a.y, DELTA );

		assertTrue( a.equals( b ));
		assertTrue( b.equals( a ));
	}

	private Quaternion qSub( Quaternion a, Quaternion b ) 
	{
		Quaternion result = new Quaternion();
		result.copy( a );

		result.x -= b.x;
		result.y -= b.y;
		result.z -= b.z;
		result.w -= b.w;

		return result;
	}
}
