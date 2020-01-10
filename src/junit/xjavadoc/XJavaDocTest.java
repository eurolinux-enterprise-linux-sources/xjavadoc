/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import junit.framework.*;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;

import xjavadoc.filesystem.FileSourceSet;

/**
 * @created   25. mars 2003
 */
public class XJavaDocTest extends TestCase
{
    private final XJavaDoc _xJavaDoc = new XJavaDoc();

	public void setUp() throws Exception
	{
		File dir = new File( System.getProperty( "basedir" ) + File.separator + "test" );
		HashMap propertyMap = new HashMap();

		propertyMap.put( "name", "xjavadoc" );
		propertyMap.put( "version", "1.0" );

		_xJavaDoc.reset(true);
		_xJavaDoc.setPropertyMap( propertyMap );
		_xJavaDoc.addSourceSet( new FileSourceSet( dir ) );
	}

	/**
	 * A unit test for JUnit
	 */
	public void testReadHello()
	{
		XClass clazz = _xJavaDoc.getXClass( "Hello" );

		XDoc doc = clazz.getDoc();

		assertEquals( "Bla bla yadda yadda", doc.getFirstSentence() );

		XTag tag = (XTag) doc.getTags().get(0);

		assertEquals( "foo.bar", tag.getName() );
		assertEquals( "beer=\"good\" tea=\"bad\"", tag.getValue() );

		doc = clazz.getMethod( "getNonsense()" ).getDoc();
		tag = (XTag) doc.getTags().get(0);
		assertEquals( "This is getNonsense.", doc.getFirstSentence() );
		assertEquals( "star.wars", tag.getName() );
		assertEquals( "is=\"a crappy movie\" but=\"I went to see it anyway\"", tag.getValue() );

		tag = (XTag) doc.getTags().get(1);
		assertEquals( "empty.tag", tag.getName() );
		assertEquals( "", tag.getValue() );

		doc = clazz.getMethod( "whatever(java.lang.String[][],int)" ).getDoc();
		tag = (XTag) doc.getTags().get(0);
		assertEquals( "Mr.", doc.getFirstSentence() );
		assertEquals( "more", tag.getName() );
		assertEquals( "testdata, bla bla", tag.getValue() );

		tag = (XTag) doc.getTags().get(1);
		assertEquals( "maybe", tag.getName() );
		assertEquals( "this=\"is\" only=\"testdata\"", tag.getValue() );
	}

	/**
	 * A unit test for JUnit
	 */
	public void testPrivateField()
	{

		XClass clazz = _xJavaDoc.getXClass( "Hello" );
		XField privateField = clazz.getField( "privateField" );
		XDoc doc = privateField.getDoc();

		assertEquals( "Braba papa, barba mama, baraba brother, barba sister", doc.getFirstSentence() );
		assertEquals( "privateField", privateField.getName() );
		assertEquals( 0, privateField.getDimension() );
		assertEquals( "java.lang.String", privateField.getType().getQualifiedName() );
		assertEquals( "java.lang.String", privateField.getType().getTransformedQualifiedName() );
		assertEquals( "String", privateField.getType().getTransformedName() );
		assertTrue( privateField.isPrivate() );
	}

	public void testInheritedFields()
	{
		XClass clazz = _xJavaDoc.getXClass( "Goodbye" );
		// get fields from superclass too
		Collection fields = clazz.getFields( true );

		// one from Goodbye and two from Hello
		assertEquals( 3, fields.size() );

		for( Iterator i = fields.iterator(); i.hasNext();  )
		{
			XField field = ( XField ) i.next();

			// The field name should not be "privateField"
			assertTrue( !"privateField".equals( field.getName() ) );
		}
	}

	public void testFirstSentence()
	{
		XClass clazz = _xJavaDoc.getXClass( "Hello" );
		XMethod method = clazz.getMethod( "firstMethod()" );
		XDoc doc = method.getDoc();

		assertEquals( "This shouldn't be the first sentence.This one should be.", doc.getFirstSentence() );
	}

	/**
	 * Read a class, get a method, modify a tag parameter, reread it and see if
	 * change was successful.
	 *
	 * @exception Exception
	 */
	public void testGetMethodParameterValue() throws Exception
	{
		_xJavaDoc.setUseNodeParser( true );

		XClass clazz = _xJavaDoc.getXClass( "Hello" );
		XMethod method = clazz.getMethod( "whatever(java.lang.String[][],int)" );
		XDoc doc = method.getDoc();

		assertEquals( "is", doc.getTagAttributeValue( "maybe", "this", true ) );

		File testDir = new File( "target/saved/testGetMethodParameterValue" );
		String fileName = clazz.save( testDir );
	}

	public void testModifyMethodParameterValue() throws Exception
	{
		_xJavaDoc.setUseNodeParser( true );

		XClass clazz = _xJavaDoc.updateMethodTag(
			"Hello",
			"whatever(java.lang.String[],int)",
			"numbers",
			"three",
			"trois",
			0
			 );
		File testDir = new File( "target/saved/testModifyMethodParameterValue" );
		String fileName = clazz.save( testDir );
	}

	public void testAddCommentToCommentedClass() throws Exception
	{
		_xJavaDoc.setUseNodeParser( true );

		XClass clazz = _xJavaDoc.updateClassTag( "Hello", "here", "is", "a tag for ya", 0 );
		File testDir = new File( "target/saved/testAddCommentToCommentedClass" );
		String fileName = clazz.save( testDir );
	}

	public void testInnerClass() throws Exception
	{
		XClass clazz = _xJavaDoc.getXClass( "Hello" );
		Collection innerClasses = clazz.getInnerClasses();

		assertEquals( 2, innerClasses.size() );

		Iterator i = innerClasses.iterator();

		XClass innerClass = ( XClass ) i.next();

		assertEquals( "Hello.InnerClass", innerClass.getQualifiedName() );
                assertEquals( "Hello$InnerClass", innerClass.getTransformedName() );
                assertEquals( "Hello$InnerClass", innerClass.getTransformedQualifiedName() );

		XClass methodInnerClass = ( XClass ) i.next();

		assertEquals( "Hello.MethodInnerClass", methodInnerClass.getQualifiedName() );
                assertEquals( "Hello$MethodInnerClass", methodInnerClass.getTransformedName() );
                assertEquals( "Hello$MethodInnerClass", methodInnerClass.getTransformedQualifiedName() );
	}

	public void testIsA() throws Exception
	{
		XClass clazz = _xJavaDoc.getXClass( "Hello" );

		assertTrue( "Hello is Serializable", clazz.isImplementingInterface( "java.io.Serializable" ) );
		assertTrue( "Hello is MouseListener", clazz.isA( "java.awt.event.MouseListener" ) );
		assertTrue( "Hello is Action", clazz.isA( "javax.swing.Action" ) );
		assertTrue( "Hello is AbstractAction", clazz.isA( "javax.swing.AbstractAction" ) );
		assertTrue( "Hello is TextAction", clazz.isA( "javax.swing.text.TextAction" ) );
	}

	public void testDereferenceProperty() throws Exception
	{
		XClass clazz = _xJavaDoc.getXClass( "Hello" );

		String tagValue = clazz.getDoc().getTag( "my:name" ).getValue();
		String attributeValue = clazz.getDoc().getTag( "my:version" ).getAttributeValue( "version" );

		assertTrue( "Tag Value dereferencing Failed: " + tagValue, "this program is called xjavadoc guess why".equals( tagValue ) );
		assertTrue( "Tag Attribute Value dereferencing Failed: " + attributeValue, "xjavadoc version is 1.0".equals( attributeValue ) );
	}

	public void testSupertags() throws Exception
	{
		XClass clazz = _xJavaDoc.getXClass( "Goodbye" );

		assertEquals( "xjavadoc.SourceClass", clazz.getClass().getName() );

		XMethod getNonsense = clazz.getMethod( "getNonsense()" );
		Collection tags = getNonsense.getDoc().getTags( true );

		assertEquals( 3, tags.size() );
	}

	public void testSupermethodsInSource() throws Exception
	{
		XClass clazz = _xJavaDoc.getXClass( "Goodbye" );
		XMethod whatever = clazz.getMethod( "whatever(java.lang.String[],int)", false );

		assertEquals( null, whatever );
		whatever = clazz.getMethod( "whatever(java.lang.String[],int)", true );
		assertNotNull( whatever );
	}

	public void testSupermethodsInBinary() throws Exception
	{
		// Turn on super methods
		System.setProperty( "xjavadoc.compiledmethods", "true" );

		XClass clazz = _xJavaDoc.getXClass( "Goodbye" );
		XMethod getClass = clazz.getMethod( "getClass()", true );

		System.setProperty( "xjavadoc.compiledmethods", "false" );

		assertNotNull( getClass );
	}

	public void testNumberOfMethodsInSource() throws Exception
	{
		XClass hello = _xJavaDoc.getXClass( "Hello" );
		XClass goodbye = _xJavaDoc.getXClass( "Goodbye" );

		Collection helloMethods = hello.getMethods();
		Collection goodbyeMethods = goodbye.getMethods();
		Collection helloAndGoodbyeMethods = goodbye.getMethods( true );

		assertEquals( 7, helloMethods.size() );
		assertEquals( 3, goodbyeMethods.size() );
		// we don't inherit the one private method, and one is overridden.
		assertEquals( 8, helloAndGoodbyeMethods.size() );
	}

	public void testSupermethods2() throws Exception
	{
		XClass hello = _xJavaDoc.getXClass( "Hello" );
		XClass goodbye = _xJavaDoc.getXClass( "Goodbye" );

		Collection methods = goodbye.getMethods( true );
		int helloCount = 0;
		int goodbyeCount = 0;

		for( Iterator m = methods.iterator(); m.hasNext();  )
		{
			XMethod method = (XMethod) m.next();

			if( method.getContainingClass() == hello )
			{
				helloCount++;
				if( method.getName().equals( "getNonsense" ) )
				{
					fail( "getNonsense is overridden in Goodbye. Shouldn't get it from Hello too." );
				}
			}
			else if( method.getContainingClass() == goodbye )
			{
				goodbyeCount++;
			}
			else
			{
				String message = "The method " + method.toString() + " was declared in the class " +
					method.getContainingClass().getName() + " " +
					"which is an instance of " + method.getContainingClass().getClass().getName() +
					"@" + method.getContainingClass().hashCode() + ". " +
					"The method should either come from Hello or Goodbye. " +
					"Hello : " + hello.getClass().getName() + "@" + hello.hashCode() + " " +
					"Goodbye : " + goodbye.getClass().getName() + "@" + goodbye.hashCode();

				fail( message );

			}
		}
		assertEquals( 3, goodbyeCount );
		// We inherit 6, but one is overridden, so that makes 5 from hello
		assertEquals( 5, helloCount );
	}

	public void testAddCommentToUncommentedMethod() throws Exception
	{
		_xJavaDoc.setUseNodeParser( true );

		XClass clazz = _xJavaDoc.updateMethodTag(
			"Hello",
			"noComment()",
			"here",
			"is",
			"a tag for ya",
			0 );

		File testDir = new File( "target/saved/testAddCommentToUncommentedMethod" );
		String fileName = clazz.save( testDir );
	}

	public void testBinaryClassHasNoMembersBecauseOfSpeedOptimisation() throws Exception
	{
		XClass collection = _xJavaDoc.getXClass( "java.util.Collection" );

		assertEquals( 0, collection.getFields().size() );
		assertEquals( 0, collection.getConstructors().size() );
		assertEquals( 0, collection.getMethods().size() );
	}

	public void testInnerInterface() throws Exception
	{
		XClass processor = _xJavaDoc.getXClass( "hanoi.Processor" );
		Iterator innerClasses = processor.getInnerClasses().iterator();
		XClass next = ( XClass ) innerClasses.next();

		assertEquals( "hanoi.Processor.Next", next.getQualifiedName() );
		assertEquals( "hanoi.Processor$Next", next.getTransformedQualifiedName() );
		assertEquals( "Processor$Next", next.getTransformedName() );
		assertTrue( next.isA( "java.io.Serializable" ) );

		XClass anonClassImplements = ( XClass ) innerClasses.next();

		assertEquals( "java.lang.Object", anonClassImplements.getSuperclass().getQualifiedName() );
		assertEquals( "java.lang.Object", anonClassImplements.getSuperclass().getTransformedQualifiedName() );
		assertEquals( "Object", anonClassImplements.getSuperclass().getTransformedName() );
		assertTrue( anonClassImplements.isA( "hanoi.Processor.Next" ) );

		XClass anonClassExtends = ( XClass ) innerClasses.next();

		assertEquals( "java.lang.Exception", anonClassExtends.getSuperclass().getQualifiedName() );
		assertEquals( "java.lang.Exception", anonClassExtends.getSuperclass().getTransformedQualifiedName() );
		assertEquals( "Exception", anonClassExtends.getSuperclass().getTransformedName() );
		assertTrue( anonClassExtends.isA( "java.io.Serializable" ) );
	}

	public void testNoNeedToImportInnerClass() throws Exception
	{
		XClass processor = _xJavaDoc.getXClass( "Goodbye" );
		XMethod gotThis = processor.getMethod( "gotThis()" );
		XClass processorNext = gotThis.getReturnType().getType();

		assertEquals( "hanoi.Processor.Next", processorNext.getQualifiedName() );
		assertEquals( "hanoi.Processor$Next", processorNext.getTransformedQualifiedName() );
		assertEquals( "Processor$Next", processorNext.getTransformedName() );
	}

	public void testPackageHasInnerClasses() throws Exception
	{
		XPackage pakkage = _xJavaDoc.getSourcePackage( "hanoi" );

		assertNotNull( pakkage );
		assertEquals( 8, pakkage.getClasses().size() );
	}

	public void testUnicode() throws Exception
	{
        // Should be able to parse a unicode file
        XClass unicode = _xJavaDoc.getXClass( "Unicode" );
	}

	public void testOldFashioned() throws Exception
	{
		// We have to tell xjavadoc to get all source classes. otherwise it will think
		// OldFashioned doesn't exist and return an UnknownClass
		_xJavaDoc.getSourceClasses();

		XClass oldFashioned = _xJavaDoc.getXClass( "OldFashioned" );
		XClass hello = _xJavaDoc.getXClass( "Hello" );

		assertEquals( SourceClass.class, oldFashioned.getClass() );
		assertTrue( "OldFashioned isn't an inner class", !oldFashioned.isInner() );

		assertEquals( hello.lastModified(), oldFashioned.lastModified() );
	}

	public void testPropertyMethods1() throws Exception
	{
		XClass hello = _xJavaDoc.getXClass( "Hello" );

		XMethod getNonsense = hello.getMethod( "getNonsense()" );

		assertEquals( "nonsense", getNonsense.getPropertyName() );
		assertTrue( getNonsense.isPropertyAccessor() );
		assertTrue( !getNonsense.isPropertyMutator() );

		XMethod setInner = hello.getMethod( "setInner(Hello.InnerClass)" );

		assertEquals( "inner", setInner.getPropertyName() );
		assertTrue( !setInner.isPropertyAccessor() );
		assertTrue( setInner.isPropertyMutator() );

		XMethod whatever = hello.getMethod( "whatever(java.lang.String[][],int)" );

		assertNull( whatever.getPropertyName() );
		assertTrue( !whatever.isPropertyAccessor() );
		assertTrue( !whatever.isPropertyMutator() );
	}

	public void testPropertyMethods2()
	{
		// Get the enclosing class first, just to resolve the rest.
        XClass abAB = _xJavaDoc.getXClass( "ab.AB" );
		XClass abABC = _xJavaDoc.getXClass( "ab.AB.C" );

		XMethod getFoo = abABC.getMethod( "getFoo()" );

		assertTrue( getFoo.isPropertyAccessor() );

		XMethod setFoo = abABC.getMethod( "setFoo(java.lang.String)" );

		assertTrue( setFoo.isPropertyMutator() );

		assertSame( getFoo, setFoo.getAccessor() );
		assertSame( setFoo, getFoo.getMutator() );

		XMethod getBar = abABC.getMethod( "getBar()" );

		assertTrue( getBar.isPropertyAccessor() );

		XMethod setBar = abABC.getMethod( "setBar(int)" );

		assertTrue( setBar.isPropertyMutator() );

		assertNull( getBar.getMutator() );
		assertNull( setBar.getAccessor() );

		XMethod isThisIsNotAnAccessor = abABC.getMethod( "isThisIsNotAnAccessor()" );

		assertTrue( !isThisIsNotAnAccessor.isPropertyAccessor() );

		XMethod setThisIsAMutator = abABC.getMethod( "setThisIsAMutator(boolean[])" );

		assertTrue( setThisIsAMutator.isPropertyMutator() );

	}

	/**
	 * Test for bugfix for XJD-17
	 */
	public void testXJD17()
	{
		XClass abAB = _xJavaDoc.getXClass( "ab.AB" );
		XClass abB = _xJavaDoc.getXClass( "ab.B" );

		assertEquals( xjavadoc.SourceClass.class, abAB.getClass() );
		assertEquals( xjavadoc.SourceClass.class, abB.getClass() );
		assertTrue( !abAB.isInterface() );
		assertTrue( abB.isInterface() );
		assertTrue( !abAB.isInner() );
		assertTrue( !abB.isInner() );
		assertTrue( !abAB.isAnonymous() );
		assertTrue( !abB.isAnonymous() );
		assertEquals( "ab", abAB.getContainingPackage().getName() );
		assertEquals( "ab", abB.getContainingPackage().getName() );

		// Do some more funny far fetched checking
		XClass abABC = _xJavaDoc.getXClass( "ab.AB.C" );
		XClass abBD = _xJavaDoc.getXClass( "ab.B.D" );

		assertEquals( xjavadoc.SourceClass.class, abABC.getClass() );
		assertEquals( xjavadoc.SourceClass.class, abBD.getClass() );
		assertTrue( abABC.isInterface() );
		assertTrue( !abBD.isInterface() );
		assertTrue( abABC.isInner() );
		assertTrue( abBD.isInner() );
		assertTrue( !abABC.isAnonymous() );
		assertTrue( !abBD.isAnonymous() );
		assertEquals( "ab", abABC.getContainingPackage().getName() );
		assertEquals( "ab", abBD.getContainingPackage().getName() );
	}

}
