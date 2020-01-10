/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.codeunit;

import junit.framework.TestCase;
import xjavadoc.*;
import xjavadoc.filesystem.ReaderFile;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.StringWriter;
import java.io.File;
import java.io.Reader;

/**
 * CodeTestCase is a JUnit extension that will let you compare two sources
 * (typically one we keep as test data and a generated one) on the API level or
 * on the abstract syntax tree (AST) level. This is a lot more powerful than
 * comparing on a character by character basis, because it's only "what matters"
 * that is compared.
 *
 * @author    Aslak Hellesøy
 * @created   24. februar 2003
 */
public abstract class CodeTestCase extends TestCase
{
    public CodeTestCase() {
        super();
    }

    public static void assertAstEqualsDir( File expectedDir, File actualDir ) {
        if( !expectedDir.isDirectory() ) {
            fail(expectedDir.getAbsolutePath() + " - should have been a directory");
        }

        if( !actualDir.isDirectory() ) {
            fail(actualDir.getAbsolutePath() + " - should have been a directory");
        }

        File[] expectedChildren =  expectedDir.listFiles();

        for (int i = 0; i < expectedChildren.length; i++) {
            File expectedChild = expectedChildren[i];
            File actualChild = getActualChild(actualDir, expectedChild );

            if( !actualChild.exists() ) {
                fail("File should have existed: " + actualChild.getAbsolutePath());
            }
            if( (expectedChild.isDirectory() && !actualChild.isDirectory()) || (!expectedChild.isDirectory() && actualChild.isDirectory()) ) {
                fail("Incompatible file types: " + expectedChild.getAbsolutePath() + "," + actualChild.getAbsolutePath());
            }

            if( expectedChild.isDirectory() ) {
                assertAstEqualsDir(expectedChild, actualChild );
            } else if( expectedChild.getName().endsWith(".java") ) {
                System.out.println("Comparing " + expectedChild.getAbsolutePath());
                assertAstEquals(expectedChild, actualChild);
            } else {
                System.out.println("Ignoring non java file: " + expectedChild.getAbsolutePath());
            }
        }
    }

    private static File getActualChild(File actualDir, File expectedChild) {
        return new File(actualDir, expectedChild.getName());
    }


    /**
	 * Compares both API and AST. Equivalent to calling {@link #assertAstEquals}
	 * and {@link #assertApiEquals}.
	 *
	 * @param expected  the expected source
	 * @param actual    the actual source
	 */
	public static void assertEquals( File expected, File actual )
	{
		assertAstEquals( expected, actual );
		assertApiEquals( expected, actual );
	}

	public static void assertEquals( Reader expected, Reader actual )
	{
		assertAstEquals( expected, actual );
		assertApiEquals( expected, actual );
	}

	/**
	 * Asserts (tests) that the ASTs of two sources are equal. Does not compare the
	 * contents (tokens) of the nodes, and is forgiving with respect to those.
	 *
	 * @param expected  the expected source
	 * @param actual    the actual source
	 */
	public static void assertAstEquals( File expected, File actual )
	{
        checkNotDir(expected, actual);

        SourceClass expectedClass = new SourceClass( new XJavaDoc(), expected, true, new XTagFactory() );
		SourceClass actualClass = new SourceClass( new XJavaDoc(), actual, true, new XTagFactory() );

		assertAstEquals( expectedClass.getCompilationUnit(), actualClass.getCompilationUnit() );
	}

	public static void assertAstEquals( Reader expected, Reader actual )
	{
        SourceClass expectedClass = new SourceClass( new XJavaDoc(), new ReaderFile(expected), true, new XTagFactory(), null );
		SourceClass actualClass = new SourceClass( new XJavaDoc(), new ReaderFile(actual), true, new XTagFactory(), null );

		assertAstEquals( expectedClass.getCompilationUnit(), actualClass.getCompilationUnit() );
	}
    /**
	 * Asserts (tests) that the APIs of two sources are equal. Does not go into the
	 * method bodies to see if the implementation is equal, and is therefore more
	 * relaxed than assertAstEquals.
	 *
	 * @param expected  the expected source
	 * @param actual    the actual source
	 */
	public static void assertApiEquals( File expected, File actual )
	{
        checkNotDir(expected, actual);
		SourceClass expectedClass = new SourceClass( new XJavaDoc(), expected, false, new XTagFactory() );
		SourceClass actualClass = new SourceClass( new XJavaDoc(), actual, false, new XTagFactory() );

		assertApiEquals( expectedClass, actualClass );
	}

	public static void assertApiEquals( Reader expected, Reader actual )
	{
		SourceClass expectedClass = new SourceClass( new XJavaDoc(), new ReaderFile(expected), false, new XTagFactory(), null );
		SourceClass actualClass = new SourceClass( new XJavaDoc(), new ReaderFile(actual), false, new XTagFactory(), null );

		assertApiEquals( expectedClass, actualClass );
	}

    private static void checkNotDir(File expected, File actual) {
        if( expected.isDirectory() ) {
            fail(expected.getAbsolutePath() + " - should not have been a directory");
        }

        if( actual.isDirectory() ) {
            fail(actual.getAbsolutePath() + " - should not have been a directory");
        }
    }

	private static void assertAstEquals( SimpleNode expected, SimpleNode actual )
	{
		// Verify that we have the same AST type
		boolean sameNodeType = expected.getType().equals( actual.getType() );
		// Verify that we have the same number of children
		boolean sameNumberOfChildren = expected.jjtGetNumChildren() == actual.jjtGetNumChildren();

		if( !sameNodeType || !sameNumberOfChildren )
		{
			// Something is not equal...
			StringWriter expectedWriter = new StringWriter();

			NodePrinter.print( expected, expectedWriter );

			StringWriter actualWriter = new StringWriter();

			NodePrinter.print( actual, actualWriter );

			// This will always fail. -But we get a nice filtered diff.
			assertEquals( expectedWriter.toString(), expectedWriter.toString(), actualWriter.toString() );
		}

		for( int i = 0; i < expected.jjtGetNumChildren(); i++ )
		{
			xjavadoc.SimpleNode expectedChild = ( xjavadoc.SimpleNode ) expected.jjtGetChild( i );
			xjavadoc.SimpleNode actualChild = ( xjavadoc.SimpleNode ) actual.jjtGetChild( i );

			assertAstEquals( expectedChild, actualChild );
		}
	}

	private static void assertApiEquals( SourceClass expected, SourceClass actual )
	{
		assertEquals( "Package names should be equal", expected.getContainingPackage().getName(), actual.getContainingPackage().getName() );
		assertModifiersEqual( "Class modifiers should be equal", expected, actual );
		assertNameEquals( "Class names should be equal", expected, actual );
		assertSuperclassEquals( expected, actual );
		assertInterfacesEqual( expected, actual );
		assertFieldsEqual( expected, actual );
		assertConstructorsEqual( expected, actual );
		assertMethodsEqual( expected, actual );
	}

	private static void assertFieldsEqual( XClass expected, XClass actual )
	{
		assertEquals( "Number of fields should be equal", expected.getFields().size(), actual.getFields().size() );

		Iterator expectedFields = expected.getFields().iterator();
		Iterator actualFields = actual.getFields().iterator();

		while( expectedFields.hasNext() )
		{
			XField expectedField = ( XField ) expectedFields.next();
			XField actualField = ( XField ) actualFields.next();

			assertFieldEquals( expectedField, actualField );
		}
	}

	private static void assertConstructorsEqual( XClass expected, XClass actual )
	{
		assertEquals( "Number of constructors should be equal", expected.getConstructors().size(), actual.getConstructors().size() );

		Iterator expectedConstructors = expected.getConstructors().iterator();
		Iterator actualConstructors = actual.getConstructors().iterator();

		while( expectedConstructors.hasNext() )
		{
			XConstructor expectedConstructor = ( XConstructor ) expectedConstructors.next();
			XConstructor actualConstructor = ( XConstructor ) actualConstructors.next();

			assertConstructorEquals( expectedConstructor, actualConstructor );
		}
	}

	private static void assertMethodsEqual( XClass expected, XClass actual )
	{
		assertEquals( "Number of methods should be equal", expected.getMethods().size(), actual.getMethods().size() );

		Iterator expectedMethods = expected.getMethods().iterator();
		Iterator actualMethods = actual.getMethods().iterator();

		while( expectedMethods.hasNext() )
		{
			XMethod expectedMethod = ( XMethod ) expectedMethods.next();
			XMethod actualMethod = ( XMethod ) actualMethods.next();

			assertMethodEquals( expectedMethod, actualMethod );
		}
	}

	private static void assertFieldEquals( XField expected, XField actual )
	{
		assertTypeEquals( "Field types should be equal", expected, actual );
		assertNameEquals( "Field names should be equal", expected, actual );
		assertModifiersEqual( "Field modifiers should be equal", expected, actual );
	}

	private static void assertConstructorEquals( XConstructor expected, XConstructor actual )
	{
		assertNameEquals( "Constructor names should be equal", expected, actual );
		assertModifiersEqual( "Constructor modifiers should be equal", expected, actual );
		assertNameWithSignatureEquals( "Constructor signatures should be equal", expected, actual );
		assertParametersEqual( "Constructor parameters should be equal", expected, actual );
		assertThrownExceptionsEqual( "Constructor exceptions should be equal", expected, actual );
	}

	private static void assertMethodEquals( XMethod expected, XMethod actual )
	{
		assertTypeEquals( "Method types should be equal", expected.getReturnType(), actual.getReturnType() );
		assertNameEquals( "Method names should be equal", expected, actual );
		assertModifiersEqual( "Method modifiers should be equal", expected, actual );
		assertNameWithSignatureEquals( "Method signatures should be equal", expected, actual );
		assertParametersEqual( "Method parameters should be equal", expected, actual );
		assertThrownExceptionsEqual( "Method exceptions should be equal", expected, actual );
	}

	private static void assertParameterEquals( XParameter expected, XParameter actual )
	{
		assertTypeEquals( "Parameter types should be equal", expected, actual );
		assertNameEquals( "Parameter names should be equal", expected, actual );
	}

	private static void assertTypeEquals( String msg, Type expected, Type actual )
	{
		assertEquals( msg, expected.getType().getQualifiedName(), actual.getType().getQualifiedName() );
		assertEquals( msg, expected.getDimensionAsString(), actual.getDimensionAsString() );
	}

	private static void assertNameEquals( String msg, Named expected, Named actual )
	{
		assertEquals( msg, expected.getName(), actual.getName() );
	}

	private static void assertSuperclassEquals( SourceClass expected, SourceClass actual )
	{
		String expectedSuperclass = expected.getSuperclass() != null ? expected.getSuperclass().getQualifiedName() : null;
		String actualSuperclass = actual.getSuperclass() != null ? actual.getSuperclass().getQualifiedName() : null;

		assertEquals( "Superclass is equal", expectedSuperclass, actualSuperclass );
	}

	private static void assertInterfacesEqual( SourceClass expected, SourceClass actual )
	{
		assertEquals( "Implemented interfaces should be equal", expected.getDeclaredInterfaces().size(), actual.getDeclaredInterfaces().size() );

		Iterator declaredInterfaces = expected.getDeclaredInterfaces().iterator();

		while( declaredInterfaces.hasNext() )
		{
			XClass declaredInterface = ( XClass ) declaredInterfaces.next();

			assertTrue( "Implements " + declaredInterface.getQualifiedName(), actual.isA( declaredInterface.getQualifiedName() ) );
		}
	}

	private static void assertModifiersEqual( String msg, XProgramElement expected, XProgramElement actual )
	{
		assertEquals( msg, expected.getModifiers(), actual.getModifiers() );
	}

	private static void assertNameWithSignatureEquals( String msg, XExecutableMember expected, XExecutableMember actual )
	{
		assertEquals( msg, expected.getNameWithSignature( false ), actual.getNameWithSignature( false ) );
	}

	private static void assertParametersEqual( String msg, XExecutableMember expected, XExecutableMember actual )
	{
		assertEquals( msg, expected.getParameters().size(), actual.getParameters().size() );

		Iterator expectedParameters = expected.getParameters().iterator();
		Iterator actualParameters = actual.getParameters().iterator();

		while( expectedParameters.hasNext() )
		{
			XParameter expectedParameter = ( XParameter ) expectedParameters.next();
			XParameter actualParameter = ( XParameter ) actualParameters.next();

			assertParameterEquals( expectedParameter, actualParameter );
		}
	}

	private static void assertThrownExceptionsEqual( String msg, XExecutableMember expected, XExecutableMember actual )
	{
		assertEquals( msg, expected.getThrownExceptions().size(), actual.getThrownExceptions().size() );

		Iterator expectedThrownExceptions = expected.getThrownExceptions().iterator();

		while( expectedThrownExceptions.hasNext() )
		{
			XClass expectedThrownException = ( XClass ) expectedThrownExceptions.next();

			assertTrue( "Throws " + expectedThrownException.getQualifiedName(), actual.throwsException( expectedThrownException.getQualifiedName() ) );
		}
	}

	/**
	 * Returns the directory where this class is located, provided that it's not in
	 * a jar. This is very useful for accessing the files you want to compare.
	 *
	 * @return   the directory where this class is located.
	 */
	protected File getDir()
	{
		return new File( getClass().getResource( "/" + getClass().getName().replace( '.', '/' ) + ".class" ).getFile() ).getParentFile();
	}

	/**
	 * Returns the root directory of the package hierarchy where this class is
	 * located, provided that it's not in a jar. This is very useful for accessing
	 * the files you want to compare.
	 *
	 * @return   the root directory.
	 */
	protected File getRootDir()
	{
		File dir = getDir();
		StringTokenizer st = new StringTokenizer( getClass().getName(), "." );

		// foo.bar.Baz = 3 tokens, but only 2 "directories"
		for( int i = 0; i < st.countTokens() - 1; i++ )
		{
			dir = dir.getParentFile();
		}
		return dir;
	}

    protected XJavaDoc getXJavaDoc() {
        return new XJavaDoc();
    }
}
