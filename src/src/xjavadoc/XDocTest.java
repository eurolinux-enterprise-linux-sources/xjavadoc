/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.io.*;
import junit.framework.*;
import xjavadoc.Token;

/**
 * JUnit test for XDoc.
 *
 * @author      Aslak Hellesøy
 * @created     3. januar 2002
 * @ejb:bla     bla
 * @param what  about this one? Or this one?
 * @oh          dear="we" should="go to bed"
 */
public class XDocTest extends TestCase
{

	private XDoc       doc;
	/**
	 * @param name     name of the test
	 */
	public XDocTest( String name )
	{
		super( name );
	}

	/**
	 * setup doc for testing
	 *
	 * @exception IOException
	 */
	public void setUp() throws IOException
	{
		String javadoc =
			"/********************** This is in the doc too      \n" +
			" * JUnit test for\n" +
			"   * JavaDocReader.    \n" +
			"   * This is sentence number two.   \n" +
			"     * @ejb:bla * bla\n" +
			" *   @param what about\n" +
			" * this one?\n" +
			" *Or this\n" +
			"     one?\n" +
			" * @oh dear=\"we\" should=\"go to bed\"\n" +
			" */";

		Token token = Token.newToken( 0 );

		token.image = javadoc;
		doc = new XDoc( token, null, null );

	}

	/**
	 * test comment text and first sentence
	 *
	 * @exception IOException  Describe the exception
	 */
	public void testFirstSentence() throws IOException
	{

		assertEquals( "This is in the doc too JUnit test for JavaDocReader.", doc.getFirstSentence() );
		assertEquals( "This is in the doc too JUnit test for JavaDocReader. This is sentence number two.", doc.getCommentText() );

	}

	public void testCommentChange() throws IOException
	{
		doc.setCommentText( "foo bar baz blurge. And this is second sentence" );

		assertEquals( "foo bar baz blurge.", doc.getFirstSentence() );
		assertEquals( "foo bar baz blurge. And this is second sentence", doc.getCommentText() );

	}

	public void testTagCreationAndRemoval() throws IOException
	{
		//check tag creation
		doc.addTag( "foo:bar", "blurge=\"bang\" baz=\"blabla\" what's up?" );

		assertTrue( doc.hasTag( "foo:bar" ) );

		// and removal
		doc.removeTag( doc.getTag( "foo:bar" ) );

		assertTrue( !doc.hasTag( "foo:bar" ) );

	}

	public void testTagChange() throws IOException
	{

		doc.addTag( "foo:bar", "blurge=\"bang\" baz=\"blabla\" what's up?" );

		assertEquals( doc.getTagAttributeValue( "foo:bar", "blurge" ), "bang" );

		XTag tag = doc.getTag( "foo:bar" );

		tag.setAttribute( "blurge", "foo" );
		assertEquals( tag.getAttributeValue( "blurge" ), "foo" );

		tag.setAttribute( "foo", "bar" );

		assertEquals( doc.getTagAttributeValue( "foo:bar", "blurge" ), "foo" );
		assertEquals( doc.getTagAttributeValue( "foo:bar", "foo" ), "bar" );

	}
}
