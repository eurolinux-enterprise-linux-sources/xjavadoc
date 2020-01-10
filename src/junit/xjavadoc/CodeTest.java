/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.io.File;
import junit.framework.AssertionFailedError;
import xjavadoc.codeunit.CodeTestCase;

/**
 * This is an example of how to extend CodeTestCase, a very handy extension of
 * JUnit's TestCase class. It is intended to be used to test the output of
 * generators like XDocletImpl, Middlegen, AndroMDA and I'm sure there are
 * more... You want to verify that the code you're generating is ok, don't you?
 *
 * @author    <a href="aslak.hellesoy at bekk.no">Aslak Helles&oslash;y</a>
 * @created   17. oktober 2002
 */
public final class CodeTest extends CodeTestCase
{    
	// the classes are under xjavadoc/build/regular/classes or
	// xjavadoc/build/unicode/classes , so we walk two dirs up.

	private final File t1 = new File( getRootDir().getParentFile().getParentFile(), "test/codeunit/CodeUnit1.java" );
	private final File t2 = new File( getRootDir().getParentFile().getParentFile(), "test/codeunit/CodeUnit2.java" );
	private final File t3 = new File( getRootDir().getParentFile().getParentFile(), "test/codeunit/CodeUnit3.java" );
	private final File t4 = new File( getRootDir().getParentFile().getParentFile(), "test/codeunit/CodeUnit4.java" );

	public void testT1SameApiAsT2() throws Exception
	{
		assertApiEquals( t1, t2 );
	}

	public void testT1DifferentAstFromT2() throws Exception
	{
		try
		{
			assertAstEquals( t1, t2 );
			fail( "The ASTs should not be equal" );
		}
		catch( AssertionFailedError e )
		{
			// ok
		}
	}

	public void testT3SameApiAsT4() throws Exception
	{
		assertApiEquals( t3, t4 );
	}
}
