/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import junit.framework.*;

/**
 * Describe what this class does
 *
 * @author    <a href="mailto:aslak.hellesoy@bekk.no">Aslak Hellesøy</a>
 * @created   8. januar 2002
 */
public class XTagTest extends TestCase
{
	/**
	 * Tests XTag.
	 */
	public void testSimpleOne() throws Exception
	{
		String text = "one=\"en\" two=\"to\" fiftysix=\"femti seks\"";
		XTag tag = new XTagFactory().createTag( "test", text, null, -1 );

		assertEquals( "en", tag.getAttributeValue( "one" ) );
		assertEquals( "to", tag.getAttributeValue( "two" ) );
		assertEquals( "femti seks", tag.getAttributeValue( "fiftysix" ) );
	}

}
